import openai
import traceback
from kiwipiepy import Kiwi
from quart import Quart, request, jsonify
import asyncio
import sqlite3  # SQLite 데이터베이스 처리용

openai.api_key_path = r"D:\Code\api_key.txt"

kiwi = Kiwi()

app = Quart(__name__)

# SQLite 데이터베이스 초기화
def init_db():
    conn = sqlite3.connect('userDatabase.db')  # 데이터베이스 이름 설정
    c = conn.cursor()
    # 테이블 생성 (존재하지 않을 경우)
    # chat_messages 테이블 생성
    c.execute('''CREATE TABLE IF NOT EXISTS chat_messages
                 (user_input TEXT, ai_response TEXT, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)''')

    # answers 테이블 생성
    c.execute('''CREATE TABLE IF NOT EXISTS answers
                 (id INTEGER PRIMARY KEY AUTOINCREMENT,
                  question_title TEXT,
                  answer TEXT,
                  userID TEXT,
                  server_code TEXT,
                  timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)''')

    conn.commit()
    conn.close()

def save_questions_to_db(questions, server_code, user_id):
    try:
        # 데이터베이스 연결
        conn = sqlite3.connect('userDatabase.db')
        c = conn.cursor()

        # 각 질문을 answers 테이블에 저장
        for question in questions:
            c.execute("""
                INSERT INTO answers (question_title, answer, userID, server_code) 
                VALUES (?, ?, ?, ?)
            """, (question, "", user_id, server_code))

        # 변경사항 커밋 및 연결 종료
        conn.commit()
        conn.close()
        print(f"{len(questions)}개의 질문이 answers 테이블에 저장되었습니다.")

    except sqlite3.Error as e:
        print(f"질문을 answers 테이블에 저장하는 중 오류 발생: {e}")

# 1. 질문 생성 엔드포인트
@app.route('/generate_questions', methods=['POST'])
async def generate_questions():
    try:
        data = await request.get_json()
        if not data or 'user_input' not in data:
            return jsonify({"error": "'user_input' 필드가 필요합니다."}), 400

        user_input = data.get('user_input', '').strip()
        server_code = data.get('server_code', 'default_server_code').strip() or "default_server_code"
        user_id = data.get('user_id', 'anonymous')  # userID 필드 추가 (필요시)

        if not user_input:
            return jsonify({"error": "입력 메시지가 비어있습니다. 다시 시도해주세요."}), 400

        tokens = kiwi.tokenize(user_input)
        nouns = [token.form for token in tokens if token.tag.startswith('NN') and len(token.form) > 1]
        verbs = [token.form + '다' for token in tokens if token.tag.startswith('VV')]
        keywords = nouns + verbs

        prompt = f"{', '.join(keywords)} - 가족과 관련된 질문 3개를 생성해줘. 1. 인종, 종교, 폭력, 성차별과 관련되지 않게. 2. 재미있고 정중하게, 질문은 각각 20자 이내로, 번호와 인용부호 없이."

        try:
            response = await asyncio.wait_for(
                openai.ChatCompletion.acreate(
                    model="gpt-4",
                    messages=[{"role": "user", "content": prompt}],
                    max_tokens=400,
                    temperature=0.7
                ),
                timeout=10
            )
        except asyncio.TimeoutError:
            return jsonify({"error": "API 호출 시간이 초과되었습니다. 다시 시도해주세요."}), 504

        generated_text = response['choices'][0]['message']['content'].strip()
        questions = [line.strip() for line in generated_text.split('\n') if line.strip()]

        # 생성된 질문들을 answers 테이블에 저장
        save_questions_to_db(questions, server_code, user_id)

        return jsonify(questions), 200

    except Exception as api_error:
        traceback.print_exc()  # 전체 스택 트레이스를 출력하여 문제 확인
        return jsonify({"error": f"API 호출 중 오류 발생: {str(api_error)}"}), 500


@app.route('/chat', methods=['POST'])
async def chat():
    try:
        data = await request.get_json()
        if not data or 'user_input' not in data:
            return "'user_input' 필드가 필요합니다.", 400  # 평문 문자열 반환

        user_input = data.get('user_input', '').strip()

        if not user_input:
            return "입력 메시지가 비어있습니다. 다시 시도해주세요.", 400  # 평문 문자열 반환

        # 프롬프트에 제한 조건 추가
        prompt = f"{user_input}\n\n응답은 친절하면서 150자 이내로 작성해줘."

        try:
            response = await openai.ChatCompletion.acreate(
                model="gpt-4",
                messages=[{"role": "user", "content": prompt}],
                max_tokens=400,
                temperature=0.7
            )
            gpt_response = response['choices'][0]['message']['content'].strip()

            # 응답 길이 확인 (혹시 300자를 넘길 경우 잘라냄)
            if len(gpt_response) > 500:
                gpt_response = gpt_response[:500]

            # 데이터베이스에 저장
            save_chat_to_db(user_input, gpt_response)
            return gpt_response, 200  # 평문 문자열 반환

        except asyncio.TimeoutError:
            return "API 호출 시간이 초과되었습니다. 다시 시도해주세요.", 504  # 평문 문자열 반환

    except Exception as api_error:
        # 스택 트레이스 출력
        traceback.print_exc()  
        return f"서버 오류 발생: {api_error}", 500  # 평문 문자열 반환

# 사용자 입력과 AI 응답을 데이터베이스에 저장하는 함수
def save_chat_to_db(user_input, gpt_response):
    try:
        conn = sqlite3.connect('userDatabase.db')
        c = conn.cursor()
        c.execute("INSERT INTO chat_messages (user_input, ai_response) VALUES (?, ?)", (user_input, gpt_response))
        conn.commit()
        conn.close()
    except sqlite3.Error as e:
        print(f"채팅을 데이터베이스에 저장하는 중 오류 발생: {e}")

if __name__ == '__main__':
    init_db()  # 앱 시작 시 데이터베이스 초기화
    app.run(host='192.168.70.138', port=5000, debug=True)


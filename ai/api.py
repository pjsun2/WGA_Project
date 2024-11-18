import os
import openai

openai.api_key = os.getenv("OPENAI_API_KEY")
if not openai.api_key:
    print("API 키가 설정되지 않았습니다.")

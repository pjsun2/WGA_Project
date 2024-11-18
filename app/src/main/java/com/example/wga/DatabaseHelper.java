package com.example.wga;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String USER_DATABASE_NAME = "userDatabase.db";
    private static final int DATABASE_VERSION = 19;  // 버전 업데이트

    public DatabaseHelper(Context context) {
        super(context, USER_DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DatabaseHelper", "DatabaseHelper 생성자 호출");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d("DatabaseHelper", "데이터베이스 onCreate 시작");

            // users 테이블 생성
            String createUserTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "userID TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "nickname TEXT NOT NULL, " +
                    "phone TEXT, " +
                    "gender TEXT, " +
                    "birthdate TEXT, " +
                    "server_code TEXT)";
            db.execSQL(createUserTable);

            // chat_messages 테이블 생성
            String createChatMessagesTable = "CREATE TABLE IF NOT EXISTS chat_messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_input TEXT, " +
                    "gpt_response TEXT, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            db.execSQL(createChatMessagesTable);

            // generated_questions 테이블 생성
            String createGeneratedQuestionsTable = "CREATE TABLE IF NOT EXISTS generated_questions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "server_code TEXT, " +
                    "generated_question TEXT, " +
                    "user_input TEXT, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            db.execSQL(createGeneratedQuestionsTable);

            // questions 테이블 생성
            String createQuestionsTable = "CREATE TABLE IF NOT EXISTS questions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "question TEXT NOT NULL, " +
                    "server_code TEXT)";
            db.execSQL(createQuestionsTable);

            // answers 테이블 생성
            String createAnswersTable = "CREATE TABLE IF NOT EXISTS answers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "question_title TEXT NOT NULL, " +
                    "answer TEXT NOT NULL, " +
                    "userID TEXT NOT NULL, " +
                    "question_id INTEGER, " +  // question_id 추가
                    "server_code TEXT, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            db.execSQL(createAnswersTable);

            insertDefaultQuestions(db);

        } catch (Exception e) {
            Log.e("DatabaseHelper", "테이블 생성 중 오류: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 18) {
            try {
                db.execSQL("ALTER TABLE answers ADD COLUMN question_id INTEGER");
                Log.d("DatabaseHelper", "answers 테이블에 question_id 열 추가 완료");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "answers 테이블 업데이트 중 오류 발생: " + e.getMessage());
            }
        }

        if (oldVersion < 19) {
            try {
                db.execSQL("ALTER TABLE generated_questions ADD COLUMN user_input TEXT");
                Log.d("DatabaseHelper", "generated_questions 테이블에 user_input 열 추가 완료");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "generated_questions 테이블 업데이트 중 오류 발생: " + e.getMessage());
            }
        }

        // 향후 새로운 테이블 수정이 필요하면 여기에 추가
    }


    // 기본 질문 삽입
    private void insertDefaultQuestions(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM questions WHERE question = ?", new String[]{"가족들과 무엇을 할 때 즐거웠나요?"});
        if (cursor != null && cursor.moveToFirst() && cursor.getInt(0) > 0) {
            Log.d("DatabaseHelper", "기본 질문이 이미 존재합니다.");
            cursor.close();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("question", "가족들과 무엇을 할 때 즐거웠나요?");
        values.put("server_code", "default_server_code");
        long result = db.insert("questions", null, values);
        if (result != -1) {
            Log.d("DatabaseHelper", "기본 질문 삽입 완료.");
        } else {
            Log.e("DatabaseHelper", "기본 질문 삽입 실패.");
        }
        cursor.close();
    }

    // 사용자 메시지와 GPT 응답을 데이터베이스에 저장
    public void saveChatMessageToDatabase(String userInput, String aiMessage) {
        if (!TextUtils.isEmpty(aiMessage)) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("user_input", userInput);
            values.put("gpt_response", aiMessage);

            long result = db.insert("chat_messages", null, values);
            if (result != -1) {
                Log.d("DatabaseHelper", "AI response saved to database.");
            } else {
                Log.e("DatabaseHelper", "Failed to save AI response.");
            }
            db.close();
        }
    }

    // 데이터베이스에서 GPT 응답을 불러오는 메서드
    public Cursor getChatMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT user_input, gpt_response FROM chat_messages", null);
    }

    // generated_questions 테이블에서 질문 가져오기
    public Cursor getGeneratedQuestions(String serverCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT generated_question FROM generated_questions WHERE server_code = ?", new String[]{serverCode});
            return cursor;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error fetching generated questions: " + e.getMessage());
        }
        return cursor;
    }


    // generated_questions 테이블에 질문 저장
    public void saveGeneratedQuestion(String serverCode, String question, String userInput) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("server_code", serverCode);
        values.put("generated_question", question);
        values.put("user_input", userInput);  // user_input 추가

        long result = db.insert("generated_questions", null, values);
        if (result != -1) {
            Log.d("DatabaseHelper", "Generated question saved to database.");
        } else {
            Log.e("DatabaseHelper", "Failed to save generated question.");
        }
        db.close();
    }
}

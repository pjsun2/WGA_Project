package com.example.wga

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.wga.network.AiService
import com.example.wga.data.UserInput
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WgahistoryClickActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var questionText: TextView
    private lateinit var answerInput: EditText
    private lateinit var answerTextView: TextView
    private lateinit var saveButton: Button
    private lateinit var editButton: Button
    private lateinit var userNameText: TextView
    private lateinit var otherAnswersLayout: LinearLayout
    private var serverCode: String = ""
    private var loggedInUserID: String = ""
    private var isOwnAnswer: Boolean = false
    private lateinit var retrofit: Retrofit
    private lateinit var aiService: AiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wgahistory_click)

        dbHelper = DatabaseHelper(this)

        // 로그인된 사용자 정보 가져오기
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        loggedInUserID = sharedPreferences.getString("loggedInUserID", null) ?: ""
        if (loggedInUserID.isEmpty()) {
            Toast.makeText(this, "로그인된 사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 서버 코드 초기화
        serverCode = getServerCode()

        // UI 요소 초기화
        questionText = findViewById(R.id.question_text)
        answerInput = findViewById(R.id.answer_input)
        answerTextView = findViewById(R.id.answer_text_view)
        saveButton = findViewById(R.id.save_button)
        editButton = findViewById(R.id.edit_button)
        userNameText = findViewById(R.id.id)
        otherAnswersLayout = findViewById(R.id.other_answers_layout)

        findViewById<ImageButton>(R.id.back_button).setOnClickListener { finish() }

        // 닉네임 설정
        userNameText.text = getLoggedInUserNickname()

        // Retrofit 초기화
        retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.70.138:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        aiService = retrofit.create(AiService::class.java)

        // 질문 제목 받아오기
        val questionTitle = intent.getStringExtra("question_title") ?: "질문을 확인할 수 없습니다."
        questionText.text = questionTitle

        // 저장된 답변 불러오기
        loadSavedAnswer(questionTitle)
        loadOtherUsersAnswers(questionTitle)

        // 저장 버튼 클릭 시 처리
        saveButton.setOnClickListener {
            val answer = answerInput.text.toString().trim()
            if (answer.isNotEmpty()) {
                saveOrUpdateAnswerInDatabase(answer, questionTitle)
                displaySavedAnswer(answer)
                sendAnswerToServer(answer)  // 서버로 답변 전송
            } else {
                Toast.makeText(this, "답변을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 수정 버튼 클릭 시 처리
        editButton.setOnClickListener {
            updateButtonVisibility(isEditing = true)
        }
    }

    // 서버 코드 가져오기
    private fun getServerCode(): String {
        val db = dbHelper.readableDatabase
        var serverCode = "default_server_code"
        db.rawQuery("SELECT server_code FROM users WHERE userID = ?", arrayOf(loggedInUserID)).use { cursor ->
            if (cursor.moveToFirst()) {
                serverCode = cursor.getString(0)
            }
        }
        return serverCode
    }

    // 로그인된 사용자의 닉네임 가져오기
    private fun getLoggedInUserNickname(): String {
        val db = dbHelper.readableDatabase
        var nickname = "닉네임 없음"
        db.rawQuery("SELECT nickname FROM users WHERE userID = ?", arrayOf(loggedInUserID)).use { cursor ->
            if (cursor.moveToFirst()) {
                nickname = cursor.getString(0)
            }
        }
        return nickname
    }

    // 저장된 답변 불러오기
    private fun loadSavedAnswer(questionTitle: String) {
        val db = dbHelper.readableDatabase
        db.rawQuery("SELECT answer FROM answers WHERE question_title = ? AND userID = ?", arrayOf(questionTitle, loggedInUserID)).use { cursor ->
            if (cursor.moveToFirst()) {
                val answer = cursor.getString(0)
                displaySavedAnswer(answer)
                isOwnAnswer = true
                editButton.visibility = View.VISIBLE
            }
        }
    }

    // 다른 사용자의 답변 불러오기
    private fun loadOtherUsersAnswers(questionTitle: String) {
        val db = dbHelper.readableDatabase
        db.rawQuery("SELECT answer, userID FROM answers WHERE question_title = ? AND userID != ?", arrayOf(questionTitle, loggedInUserID)).use { cursor ->
            while (cursor.moveToNext()) {
                val answer = cursor.getString(0)
                val userID = cursor.getString(1)
                val nickname = getUserNickname(userID)

                val answerView = createOtherAnswerView(nickname, answer)
                otherAnswersLayout.addView(answerView)
            }
        }
    }

    private fun createOtherAnswerView(nickname: String, answer: String): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 10, 10, 10)
            setBackgroundColor(ContextCompat.getColor(this@WgahistoryClickActivity, android.R.color.white))
        }

        val nicknameTextView = TextView(this).apply {
            text = nickname
            setTextColor(ContextCompat.getColor(this@WgahistoryClickActivity, android.R.color.black))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val answerTextView = TextView(this).apply {
            text = answer
            setTextColor(ContextCompat.getColor(this@WgahistoryClickActivity, android.R.color.darker_gray))
            textSize = 16f
            setPadding(0, 5, 0, 10)
        }

        layout.addView(nicknameTextView)
        layout.addView(answerTextView)

        return layout
    }

    // 사용자의 닉네임 가져오기
    private fun getUserNickname(userID: String): String {
        val db = dbHelper.readableDatabase
        db.rawQuery("SELECT nickname FROM users WHERE userID = ?", arrayOf(userID)).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return "알 수 없는 사용자"
    }

    // 답변 저장 또는 업데이트
    private fun saveOrUpdateAnswerInDatabase(answer: String, questionTitle: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("question_title", questionTitle)
            put("answer", answer)
            put("userID", loggedInUserID)
            put("server_code", serverCode)
        }

        val result = db.update("answers", values, "question_title = ? AND userID = ?", arrayOf(questionTitle, loggedInUserID))
        if (result == 0) {
            db.insert("answers", null, values)
            Toast.makeText(this, "답변이 저장되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "답변이 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
        }

        updateButtonVisibility(false)
    }

    // 서버에 답변 전송
    private fun sendAnswerToServer(answer: String) {
        val userInput = UserInput(answer, server_code = serverCode)

        aiService.sendUserInput(userInput).enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    response.body()?.forEach { question ->
                        saveGeneratedQuestionToDatabase(question)
                        Toast.makeText(this@WgahistoryClickActivity, "새 질문: $question", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Toast.makeText(this@WgahistoryClickActivity, "서버 연결 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 서버에서 받은 질문 저장
    private fun saveGeneratedQuestionToDatabase(question: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("generated_question", question)
            put("server_code", serverCode)
        }
        db.insert("generated_questions", null, values)
    }

    // 답변 표시
    private fun displaySavedAnswer(answer: String) {
        answerInput.visibility = View.GONE
        answerTextView.visibility = View.VISIBLE
        answerTextView.text = answer
    }

    // 버튼 표시 상태 업데이트
    private fun updateButtonVisibility(isEditing: Boolean) {
        answerInput.visibility = if (isEditing) View.VISIBLE else View.GONE
        answerTextView.visibility = if (isEditing) View.GONE else View.VISIBLE
        saveButton.visibility = if (isEditing) View.VISIBLE else View.GONE
        editButton.visibility = if (isEditing) View.GONE else View.VISIBLE
    }
}

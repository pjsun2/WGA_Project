package com.example.wga

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class HistoryActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var questionText: TextView
    private lateinit var generatedQuestionsLayout: LinearLayout
    private var serverCode: String = "default_server_code"
    private val iconThree: ImageView? = null
    private val iconFour: ImageView? = null
    private val iconOne: ImageView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wgahistory)

        dbHelper = DatabaseHelper(this)
        questionText = findViewById(R.id.question_text)
        generatedQuestionsLayout = findViewById(R.id.generated_questions_layout)


        // 서버 코드 초기화
        serverCode = getServerCode()
        Log.d("HistoryActivity", "서버 코드 확인: $serverCode")

        questionText.setOnClickListener {
            val intent = Intent(this@HistoryActivity, WgahistoryClickActivity::class.java)
            intent.putExtra("server_code", serverCode)
            intent.putExtra("question_title", questionText.text.toString())
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.icon_one).setOnClickListener {
            // AIConsultingActivity로 이동
            val intent = Intent(this@HistoryActivity, SendLetterActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.icon_three).setOnClickListener {
            // AIConsultingActivity로 이동
            val intent = Intent(this@HistoryActivity, calActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.icon_four).setOnClickListener {
            // AIConsultingActivity로 이동
            val intent = Intent(this@HistoryActivity, AIConsultingActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadQuestions()
    }

    private fun getServerCode(): String {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("server_code", "default_server_code") ?: "default_server_code"
    }

    private fun loadQuestions() {
        if (!loadGeneratedQuestions()) {
            loadFirstQuestion()
        }
    }

    private fun loadFirstQuestion() {
        dbHelper.readableDatabase.use { db ->
            db.rawQuery("SELECT question FROM questions LIMIT 1", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    val question = cursor.getString(0)
                    questionText.text = "Q1. $question"

                    questionText.setOnClickListener {
                        val intent = Intent(this@HistoryActivity, WgahistoryClickActivity::class.java)
                        intent.putExtra("server_code", serverCode)
                        intent.putExtra("question_title", questionText.text.toString())
                        intent.putExtra("question_text", question)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun loadGeneratedQuestions(): Boolean {
        var hasGeneratedQuestions = false

        dbHelper.readableDatabase.use { db ->
            db.rawQuery(
                "SELECT g.generated_question, a.answer FROM generated_questions g " +
                        "LEFT JOIN answers a ON g.generated_question = a.question_title " +
                        "WHERE g.server_code = ?",
                arrayOf(serverCode)
            ).use { cursor ->
                generatedQuestionsLayout.removeAllViews()
                Log.d("HistoryActivity", "생성된 질문과 답변 로드 시작 (서버 코드: $serverCode)")

                if (cursor.count > 0) {
                    hasGeneratedQuestions = true
                    var questionNumber = 1

                    while (cursor.moveToNext()) {
                        val question = cursor.getString(0)  // generated_question
                        val answer = cursor.getString(1) ?: "답변이 아직 없습니다."  // answer

                        Log.d("HistoryActivity", "로드된 질문: Q$questionNumber. $question, 답변: $answer")

                        // 질문 버튼 생성
                        val questionButton = Button(this).apply {
                            text = "Q$questionNumber. $question"
                            setTextColor(ContextCompat.getColor(this@HistoryActivity, android.R.color.black))
                            textSize = 16f
                            setPadding(0, 5, 0, 10)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(10, 10, 10, 10)
                            }
                            setBackgroundResource(R.drawable.border)
                        }

                        // 질문 버튼 클릭 시 WgahistoryClickActivity로 이동
                        questionButton.setOnClickListener {
                            val intent = Intent(this@HistoryActivity, WgahistoryClickActivity::class.java)
                            intent.putExtra("server_code", serverCode)
                            intent.putExtra("question_title", question)
                            startActivity(intent)
                        }

                        // 답변을 표시할 TextView 생성
                        val answerTextView = TextView(this).apply {
                            text = "답변: $answer"
                            textSize = 14f
                            setPadding(10, 5, 10, 10)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setTextColor(ContextCompat.getColor(this@HistoryActivity, android.R.color.darker_gray))
                            visibility = TextView.GONE
                        }

                        // 질문 버튼 클릭 시 답변 표시/숨김 토글
                        // 질문 버튼 클릭 시 WgahistoryClickActivity로 이동
                        questionButton.setOnClickListener {
                            val intent = Intent(this@HistoryActivity, WgahistoryClickActivity::class.java)
                            intent.putExtra("server_code", serverCode)  // 서버 코드 전달
                            intent.putExtra("question_title", question) // 질문 제목 전달
                            startActivity(intent)
                        }


                        generatedQuestionsLayout.addView(questionButton)
                        generatedQuestionsLayout.addView(answerTextView)
                        questionNumber++
                    }
                } else {
                    Log.d("HistoryActivity", "생성된 질문이 없습니다.")
                }
            }
        }

        return hasGeneratedQuestions
    }

    private fun showAnswerPopup(question: String, answer: String) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(question)
            .setMessage(answer)
            .setPositiveButton("확인", null)
            .create()
        dialog.show()
    }
}

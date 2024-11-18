package com.example.wga

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences

class FCodeActivity : AppCompatActivity() {

    private lateinit var inputCode: EditText
    private lateinit var completeButton: Button
    private lateinit var nextButton: Button  // 추가
    private lateinit var errorMessage: TextView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family_code)

        inputCode = findViewById(R.id.textView_input_prompt)
        completeButton = findViewById(R.id.CompleteButton)
        nextButton = findViewById(R.id.next_button_background)  // next_button_background 버튼 연결
        errorMessage = findViewById(R.id.textView_get_code)

        dbHelper = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Complete 버튼 클릭 시 처리
        completeButton.setOnClickListener {
            val enteredCode = inputCode.text.toString().trim()

            if (enteredCode.isNotEmpty()) {
                if (checkCodeInDatabase(enteredCode)) {
                    // 코드가 존재하면 로그인된 계정의 DB에 코드 업데이트 및 NotiPermiActivity로 이동
                    saveCodeToUserAccount(enteredCode)

                    val intent = Intent(this@FCodeActivity, NotiPermiActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // 코드가 존재하지 않으면 오류 메시지 표시
                    errorMessage.text = "코드가 존재하지 않습니다"
                }
            } else {
                Toast.makeText(this, "코드를 입력해 주세요", Toast.LENGTH_SHORT).show()
            }
        }

        // next_button_background 클릭 시 FNewActivity로 이동
        nextButton.setOnClickListener {
            val intent = Intent(this@FCodeActivity, FNewActivity::class.java)
            startActivity(intent)
        }
    }

    // 데이터베이스에서 코드가 존재하는지 확인하는 메서드
    private fun checkCodeInDatabase(code: String): Boolean {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val query = "SELECT * FROM users WHERE server_code = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(code))

        val codeExists = cursor.count > 0
        cursor.close()
        return codeExists
    }

    // 로그인된 계정의 DB에 코드를 업데이트하는 메서드
    private fun saveCodeToUserAccount(code: String) {
        val db = dbHelper.writableDatabase

        // 로그인된 사용자 ID를 SharedPreferences에서 가져옴
        val loggedInUserId = sharedPreferences.getString("userID", null)

        if (loggedInUserId != null) {
            val query = "UPDATE users SET server_code = ? WHERE userID = ?"
            db.execSQL(query, arrayOf(code, loggedInUserId))

            Toast.makeText(this, "코드가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "로그인된 사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
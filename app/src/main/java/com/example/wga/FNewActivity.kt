package com.example.wga

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FNewActivity : AppCompatActivity() {

    private lateinit var generatedCodeText: TextView
    private lateinit var generateCodeButton: Button
    private lateinit var copyCodeButton: Button
    private lateinit var nextButton: Button
    private var generatedCode = ""
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private var loggedInUserID: String? = null  // loggedInUserID 변수를 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family_newcode)

        generatedCodeText = findViewById(R.id.family_code)
        generateCodeButton = findViewById(R.id.button_generate_code)
        copyCodeButton = findViewById(R.id.button_copy_code)
        nextButton = findViewById(R.id.button)  // '다음' 버튼

        dbHelper = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // 로그인된 사용자 ID를 SharedPreferences에서 가져옴
        loggedInUserID = sharedPreferences.getString("loggedInUserID", null)

        // 로그인된 사용자가 없는 경우 오류 처리
        if (loggedInUserID == null) {
            Toast.makeText(this, "로그인된 사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            Log.e("FNewActivity", "User ID not found in SharedPreferences")
            return
        }

        // 초기 상태에서 복사 및 다음 버튼 숨기기
        copyCodeButton.visibility = View.GONE
        nextButton.visibility = View.GONE

        // 코드 생성 버튼 클릭 시 무작위 코드 생성
        generateCodeButton.setOnClickListener {
            generatedCode = generateRandomCode()
            generatedCodeText.text = generatedCode

            // 복사 및 다음 버튼 보이기
            copyCodeButton.visibility = View.VISIBLE
            nextButton.visibility = View.VISIBLE

            // 로그인된 사용자와 연동하여 데이터베이스 업데이트
            saveCodeToDatabase(generatedCode)
        }

        // 코드 복사 버튼 클릭 시 클립보드에 복사
        copyCodeButton.setOnClickListener {
            copyCodeToClipboard(generatedCode)
        }

        // 다음 버튼 클릭 시 FCodeActivity로 이동
        nextButton.setOnClickListener {
            val intent = Intent(this@FNewActivity, FCodeActivity::class.java)
            startActivity(intent)
        }
    }

    // 무작위 코드 생성 메서드 (영문자와 숫자 혼합, 8자리)
    private fun generateRandomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..8)
            .map { chars.random() }
            .joinToString("")
    }

    // 생성된 코드를 로그인된 사용자와 연동해 데이터베이스에 저장
    private fun saveCodeToDatabase(code: String) {
        val db = dbHelper.writableDatabase

        // 로그인된 사용자 ID를 확인하고 저장
        if (loggedInUserID != null) {
            try {
                Log.d("FNewActivity", "Saving code for userID: $loggedInUserID")
                val query = "UPDATE users SET server_code = ? WHERE userID = ?"
                db.execSQL(query, arrayOf(code, loggedInUserID))
                Toast.makeText(this, "코드가 데이터베이스에 저장되었습니다.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("DatabaseError", "Error updating database: ${e.message}")
                Toast.makeText(this, "데이터베이스 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                db.close()
            }
        } else {
            Log.e("FNewActivity", "User ID not found in SharedPreferences")
            Toast.makeText(this, "로그인된 사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 생성된 코드를 클립보드에 복사하는 메서드
    private fun copyCodeToClipboard(code: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("server_code", code)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "코드가 클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
    }
}

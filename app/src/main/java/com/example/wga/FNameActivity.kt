package com.example.wga

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class FNameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family_name) // activity_family_name.xml 사용

        // '다음' 버튼 클릭 시 NotiPermiActivity로 이동
        val nextButton = findViewById<Button>(R.id.view_beige)
        nextButton.setOnClickListener {
            val intent = Intent(this, NotiPermiActivity::class.java)
            startActivity(intent)
        }
    }
}

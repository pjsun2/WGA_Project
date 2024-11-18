package com.example.wga

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // activity_main.xml과 연결

        // View를 클릭하면 LoginActivity로 이동하는 코드
        val view = findViewById<View>(R.id.view)  // activity_main.xml의 View ID
        view.setOnClickListener {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}

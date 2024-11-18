package com.example.wga

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.successful_subscription)

        // start_button 클릭 시 HistoryActivity로 이동
        val startButton = findViewById<Button>(R.id.start_button)
        startButton.setOnClickListener {
            val intent = Intent(this@SuccessActivity, HistoryActivity::class.java)
            startActivity(intent)
        }
    }
}

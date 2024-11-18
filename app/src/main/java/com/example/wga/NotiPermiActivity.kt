package com.example.wga

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class NotiPermiActivity : AppCompatActivity() {

    private val channelId = "example_channel"
    private val notificationRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_permission)

        // 알림 권한 요청을 위한 버튼 설정
        val allowNotiButton = findViewById<Button>(R.id.allow_noti_button)
        allowNotiButton.setOnClickListener {
            checkAndRequestNotificationPermission()
        }
        val nextButton = findViewById<Button>(R.id.next_button)
        nextButton.setOnClickListener {
            // successful_subscription로 이동
            val intent = Intent(this, SuccessActivity::class.java)
            startActivity(intent)
        }
    }

    // 알림 권한 확인 및 요청
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), notificationRequestCode)
            } else {
                createNotificationChannel()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            // 안드로이드 8.0 이하 버전에서는 알림 권한 요청 불필요
            Toast.makeText(this, "알림 권한이 필요하지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = notificationManager.getNotificationChannel(channelId)

            if (notificationChannel == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Example Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Example Notification Channel"
                }
                notificationManager.createNotificationChannel(channel)
                Toast.makeText(this, "알림 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "알림 권한이 이미 허용되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

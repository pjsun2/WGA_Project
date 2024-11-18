package com.example.wga;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SendLetterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_message); // XML 파일 이름 확인

        // 아이콘 초기화
        ImageView iconTwo = findViewById(R.id.icon_two);
        ImageView iconThree = findViewById(R.id.icon_three);
        ImageView iconFour = findViewById(R.id.icon_four);

        // 아이콘 2 클릭 시 HistoryActivity로 이동
        iconTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SendLetterActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        // 아이콘 3 클릭 시 calActivity로 이동
        iconThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SendLetterActivity.this, calActivity.class);
                startActivity(intent);
            }
        });

        // 아이콘 4 클릭 시 AIConsultingActivity로 이동
        iconFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SendLetterActivity.this, AIConsultingActivity.class);
                startActivity(intent);
            }
        });
    }
}

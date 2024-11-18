package com.example.wga;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class calActivity extends AppCompatActivity {

    private ImageView iconTwo;
    private ImageView iconFour;
    private ImageView iconOne;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cal); // cal.xml 레이아웃 설정

        // UI 요소 초기화
        iconTwo = findViewById(R.id.icon_two);
        iconFour = findViewById(R.id.icon_four);
        iconOne = findViewById(R.id.icon_one);

        // 아이콘 1 클릭 시 HistoryActivity로 이동
        iconOne.setOnClickListener(v -> {
            Intent intent = new Intent(calActivity.this, SendLetterActivity.class);
            startActivity(intent);
        });
        // 아이콘 2 클릭 시 HistoryActivity로 이동
        iconTwo.setOnClickListener(v -> {
            Intent intent = new Intent(calActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // 아이콘 4 클릭 시 AIConsultingActivity로 이동
        iconFour.setOnClickListener(v -> {
            Intent intent = new Intent(calActivity.this, AIConsultingActivity.class);
            startActivity(intent);
        });
    }
}

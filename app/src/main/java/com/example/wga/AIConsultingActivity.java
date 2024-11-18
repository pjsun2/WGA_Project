package com.example.wga;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wga.network.AiService;
import com.example.wga.data.UserInput;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AIConsultingActivity extends AppCompatActivity {

    private EditText messageInput;
    private LinearLayout chatContainer;
    private AiService aiService;
    private DatabaseHelper dbHelper;
    private Handler handler;
    private Runnable updateChatRunnable;
    private long lastMessageId = 0; // 마지막으로 로드된 메시지 ID
    private ImageView iconTwo;
    private ImageView iconThree;
    private ImageView iconOne;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ai_consulting);

        messageInput = findViewById(R.id.message_input);
        chatContainer = findViewById(R.id.chat_container);
        dbHelper = new DatabaseHelper(this);
        iconOne = findViewById(R.id.icon_one);
        iconTwo = findViewById(R.id.icon_two);
        iconThree = findViewById(R.id.icon_three);

        iconOne.setOnClickListener(v -> {
            Intent intent = new Intent(AIConsultingActivity.this, SendLetterActivity.class);
            startActivity(intent);
        });

        iconTwo.setOnClickListener(v -> {
            Intent intent = new Intent(AIConsultingActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        iconThree.setOnClickListener(v -> {
            Intent intent = new Intent(AIConsultingActivity.this, calActivity.class);
            startActivity(intent);
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.70.138:5000")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        aiService = retrofit.create(AiService.class);

        loadChatMessagesFromDatabase();  // 기존 메시지 로드
        handler = new Handler();
        startCheckingForNewMessages();  // 주기적으로 새 메시지 확인

        findViewById(R.id.send_button).setOnClickListener(v -> {
            String userInput = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(userInput)) {
                sendMessage(userInput);
            } else {
                Toast.makeText(this, "메시지를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String userInput) {
        addUserMessageToChat(userInput);

        aiService.chat(new UserInput(userInput, "default_server_code")).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String aiMessage = response.body();
                    saveChatMessageToDatabase(userInput, aiMessage);
                    addAiMessageToChat(aiMessage);
                } else {
                    Toast.makeText(AIConsultingActivity.this, "서버 응답 실패.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(AIConsultingActivity.this, "API 호출 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChatMessageToDatabase(String userInput, String aiMessage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_input", userInput);
        values.put("gpt_response", aiMessage);
        long id = db.insert("chat_messages", null, values);  // 저장 후 ID 반환
        lastMessageId = id;  // 마지막 메시지 ID 갱신
        db.close();
    }

    private void loadChatMessagesFromDatabase() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT ROWID, user_input, gpt_response FROM chat_messages ORDER BY ROWID", null);

        while (cursor.moveToNext()) {
            long messageId = cursor.getLong(cursor.getColumnIndex("id"));
            String userMessage = cursor.getString(cursor.getColumnIndex("user_input"));
            String aiMessage = cursor.getString(cursor.getColumnIndex("gpt_response"));

            if (messageId > lastMessageId) {
                addUserMessageToChat(userMessage);
                addAiMessageToChat(aiMessage);
                lastMessageId = messageId;  // 마지막 메시지 ID 갱신
            }
        }
        cursor.close();
    }

    private void startCheckingForNewMessages() {
        updateChatRunnable = () -> {
            checkForNewMessages();
            handler.postDelayed(updateChatRunnable, 5000);
        };
        handler.post(updateChatRunnable);
    }

    private void checkForNewMessages() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT ROWID, user_input, gpt_response FROM chat_messages WHERE ROWID > ? ORDER BY ROWID",
                new String[]{String.valueOf(lastMessageId)}
        );

        while (cursor.moveToNext()) {
            long messageId = cursor.getLong(cursor.getColumnIndex("id"));
            String userMessage = cursor.getString(cursor.getColumnIndex("user_input"));
            String aiMessage = cursor.getString(cursor.getColumnIndex("gpt_response"));

            addUserMessageToChat(userMessage);
            addAiMessageToChat(aiMessage);
            lastMessageId = messageId;  // 마지막 메시지 ID 갱신
        }
        cursor.close();
    }

    private void addUserMessageToChat(String message) {
        TextView textView = (TextView) LayoutInflater.from(this).inflate(R.layout.user_bubble, chatContainer, false);
        textView.setText(message);
        chatContainer.addView(textView);
    }

    private void addAiMessageToChat(String message) {
        TextView textView = (TextView) LayoutInflater.from(this).inflate(R.layout.gpt_bubble, chatContainer, false);
        textView.setText(message);
        chatContainer.addView(textView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateChatRunnable);
    }
}


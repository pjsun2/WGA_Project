package com.example.wga;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = findViewById(R.id.next_button_background);
        Button signUpButton = findViewById(R.id.signup_button_background);
        EditText usernameEditText = findViewById(R.id.login_id_text);
        EditText passwordEditText = findViewById(R.id.login_password_text);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (checkLogin(username, password)) {
                saveToSharedPreferences("loggedInUserID", username);
                saveServerCodeToSharedPreferences(username);
                navigateToNextActivity();
            } else {
                Toast.makeText(LoginActivity.this, "아이디 또는 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                passwordEditText.setText("");  // 비밀번호 필드 지우기
            }
        });

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private boolean checkLogin(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM users WHERE userID = ? AND password = ?", new String[]{username, password})) {
            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("LoginActivity", "Login check failed: " + e.getMessage());
            return false;
        }
    }

    private void saveToSharedPreferences(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
        Log.d("LoginActivity", key + " saved: " + value);
    }

    private void saveServerCodeToSharedPreferences(String userID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT server_code FROM users WHERE userID = ?", new String[]{userID})) {
            if (cursor.moveToFirst()) {
                String serverCode = cursor.getString(0);
                saveToSharedPreferences("server_code", serverCode);
                Log.d("LoginActivity", "serverCode saved: " + serverCode);
            } else {
                Log.w("LoginActivity", "No server code found for user: " + userID);
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "Failed to save server code: " + e.getMessage());
        }
    }

    private void navigateToNextActivity() {
        Intent intent;
        String serverCode = sharedPreferences.getString("server_code", null);
        if (serverCode != null) {
            intent = new Intent(LoginActivity.this, HistoryActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, FCodeActivity.class);
        }
        Toast.makeText(LoginActivity.this, "로그인에 성공했습니다.", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();  // 데이터베이스 연결을 닫기
        super.onDestroy();
    }
}

package com.example.wga;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity {

    private EditText idText, passwordText, passwordCheckText;
    private EditText nameText, nicknameText, phoneText, birthdateText;
    private RadioGroup genderGroup;
    private TextView idErrorMessage, idDuplicateErrorMessage, passwordErrorMessage, passwordMatchErrorMessage;
    private Button nextButton, returnButton;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 회원가입 필드 및 에러 메시지 TextView 초기화
        idText = findViewById(R.id.id_text);
        passwordText = findViewById(R.id.password_text);
        passwordCheckText = findViewById(R.id.password_check_text);
        nameText = findViewById(R.id.name_text);
        nicknameText = findViewById(R.id.nickname_text);
        phoneText = findViewById(R.id.phone_text);
        birthdateText = findViewById(R.id.date_text_view);  // 생년월일 필드
        genderGroup = findViewById(R.id.radioGroup);  // 성별 선택

        idErrorMessage = findViewById(R.id.id_error_message);
        idDuplicateErrorMessage = findViewById(R.id.idDC_error_message);
        passwordErrorMessage = findViewById(R.id.password_error_message);
        passwordMatchErrorMessage = findViewById(R.id.password_match_error_message);

        nextButton = findViewById(R.id.next_button_background);
        returnButton = findViewById(R.id.return_button_background);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // 생년월일 필드 클릭 시 DatePickerDialog 표시
        birthdateText.setOnClickListener(v -> showDatePickerDialog());

        nextButton.setOnClickListener(v -> {
            String userId = idText.getText().toString().trim();
            String password = passwordText.getText().toString().trim();
            String passwordCheck = passwordCheckText.getText().toString().trim();
            String name = nameText.getText().toString().trim();
            String nickname = nicknameText.getText().toString().trim();
            String phone = phoneText.getText().toString().trim();
            String birthdate = birthdateText.getText().toString().trim();

            // 성별 선택
            int selectedGenderId = genderGroup.getCheckedRadioButtonId();
            RadioButton selectedGenderButton = findViewById(selectedGenderId);
            String gender = (selectedGenderButton != null) ? selectedGenderButton.getText().toString() : "";

            boolean isValid = true;

            // 아이디 형식 검사
            if (!isValidId(userId)) {
                idErrorMessage.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                idErrorMessage.setVisibility(View.GONE);
            }

            // 아이디 중복 검사
            if (isDuplicate(userId)) {
                idDuplicateErrorMessage.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                idDuplicateErrorMessage.setVisibility(View.GONE);
            }

            // 비밀번호 형식 검사
            if (!isValidPassword(password)) {
                passwordErrorMessage.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                passwordErrorMessage.setVisibility(View.GONE);
            }

            // 비밀번호 일치 여부 확인
            if (!password.equals(passwordCheck)) {
                passwordMatchErrorMessage.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                passwordMatchErrorMessage.setVisibility(View.GONE);
            }

            // 모든 조건이 통과한 경우 데이터베이스 저장
            if (isValid) {
                saveToDatabase(userId, password, name, nickname, phone, gender, birthdate);
            }
        });

        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // DatePickerDialog를 표시하는 메서드
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                SignUpActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    birthdateText.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    public boolean isValidId(String id) {
        return id.matches("^[a-zA-Z0-9]+$");
    }

    public boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-zA-Z])(?=.*[!@#])(?=.*[0-9])[a-zA-Z0-9!@#]+$");
    }

    public boolean isDuplicate(String userId) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query("users", new String[]{"userID"}, "userID = ?", new String[]{userId}, null, null, null)) {
            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("SignUpActivity", "Error checking duplicate ID: " + e.getMessage());
            return false;
        }
    }

    public void saveToDatabase(String userId, String password, String name, String nickname, String phone, String gender, String birthdate) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("userID", userId);
            values.put("password", password);
            values.put("name", name);
            values.put("nickname", nickname);
            values.put("phone", phone);
            values.put("gender", gender);
            values.put("birthdate", birthdate);

            long newRowId = db.insert("users", null, values);
            if (newRowId != -1) {
                Toast.makeText(SignUpActivity.this, "가입 성공!", Toast.LENGTH_SHORT).show();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userID", userId);
                editor.apply();

                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(SignUpActivity.this, "가입 실패!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("SignUpActivity", "Error saving to database: " + e.getMessage());
        }
    }
}

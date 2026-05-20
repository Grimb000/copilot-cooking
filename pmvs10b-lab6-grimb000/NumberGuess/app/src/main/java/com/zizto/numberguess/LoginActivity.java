package com.zizto.numberguess;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    SharedPreferences userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        userProfile = getSharedPreferences("user_profile", MODE_PRIVATE);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            String savedEmail = userProfile.getString("email", null);
            Log.d("TAG", "------------------------------------------------------- " + savedEmail + ": " + userProfile.getString("password", ""));
            if (savedEmail == null) {
                userProfile.edit()
                        .putString("email", email)
                        .putString("password", password)
                        .apply();
                Toast.makeText(this, "Пользователь зарегистрирован", Toast.LENGTH_SHORT).show();
                goToMain();
            } else if (savedEmail.equals(email) && userProfile.getString("password", "").equals(password)) {
                Toast.makeText(this, "Вход выполнен", Toast.LENGTH_SHORT).show();
                goToMain();
            } else {
                Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain() {

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
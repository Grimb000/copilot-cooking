package com.zizto.numberguess;

import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    Button bControl;
    EditText etInput;
    TextView tvInfo, tvAttempts;
    ImageView ivIcon;
    ProgressBar progressBar;

    GestureLibrary gLib;
    GestureOverlayView gesturesView;

    int numberToGuess = 0;
    boolean isGameEnded = false;
    int attemptsCount = 0;
    final int MAX_ATTEMPTS = 10;
    int launchCount = 0;

    SharedPreferences appSettings;
    String uniqueAppId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appSettings = getSharedPreferences("app_settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = appSettings.edit();

        launchCount = appSettings.getInt("launch_count", 0) + 1;
        editor.putInt("launch_count", launchCount);

        uniqueAppId = appSettings.getString("app_id", null);
        if (uniqueAppId == null) {
            uniqueAppId = java.util.UUID.randomUUID().toString();
            editor.putString("app_id", uniqueAppId);
        }

        editor.apply();

        tvInfo = findViewById(R.id.textView);
        tvAttempts = findViewById(R.id.tvAttempts);
        etInput = findViewById(R.id.editTextNumber);
        bControl = findViewById(R.id.button);
        ivIcon = findViewById(R.id.ivIcon);
        progressBar = findViewById(R.id.progressBarAttempts);

        gLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!gLib.load()) {
            Toast.makeText(this, "Не удалось загрузить жесты!", Toast.LENGTH_SHORT).show();
            finish();
        }

        gesturesView = findViewById(R.id.gestures_overlay);
        gesturesView.addOnGesturePerformedListener(this);

        startNewGame();
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = gLib.recognize(gesture);

        if (predictions.size() > 0 && predictions.get(0).score > 1.0) {
            String gestureName = predictions.get(0).name;

            if (isNumeric(gestureName)) {
                etInput.append(gestureName);
            }
            else if (gestureName.equals("check")) {
                onClick(bControl);
            }
            else if (gestureName.equals("clear")) {
                etInput.setText("");
            }
            else {
                Toast.makeText(this, "Жест: " + gestureName, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void startNewGame() {
        numberToGuess = (int) (Math.random() * 100);
        isGameEnded = false;
        attemptsCount = 0;

        bControl.setText(getResources().getString(R.string.input_value));
        tvInfo.setText(getResources().getString(R.string.try_to_guess));
        tvInfo.setText("Id = " + uniqueAppId + "; kolZap = " + launchCount);
        etInput.setText("");

        progressBar.setProgress(0);
        String attemptsText = getResources().getQuantityString(
                R.plurals.attempts_count,
                attemptsCount,
                attemptsCount,
                MAX_ATTEMPTS
        );
        tvAttempts.setText(attemptsText);

        bControl.setEnabled(true);

        Animation combo = AnimationUtils.loadAnimation(this, R.anim.combo_anim);
        ivIcon.startAnimation(combo);
    }

    public void onClick(View v) {
        if (isGameEnded) {
            startNewGame();
            return;
        }

        String inputText = etInput.getText().toString();
        if (inputText.isEmpty()) {
            tvInfo.setText(getResources().getString(R.string.error_empty));
            return;
        }

        int inp = Integer.parseInt(inputText);
        if (inp < 0 || inp > 100) {
            tvInfo.setText(getResources().getString(R.string.error_range));
            etInput.setText("");
            return;
        }

        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_center);
        ivIcon.startAnimation(rotate);

        bControl.setEnabled(false);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkGuess(inp);
            if (!isGameEnded) {
                bControl.setEnabled(true);
            }
        }, 600);
    }

    private void checkGuess(int inp) {
        attemptsCount++;
        progressBar.setProgress(attemptsCount);

        String attemptsText = getResources().getQuantityString(
                R.plurals.attempts_count,
                attemptsCount,
                attemptsCount,
                MAX_ATTEMPTS
        );
        tvAttempts.setText(attemptsText);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        tvInfo.startAnimation(fadeIn);

        if (inp == numberToGuess) {
            tvInfo.setText(getResources().getString(R.string.hit));

            int bestScore = appSettings.getInt("best_score", Integer.MAX_VALUE);
            if (attemptsCount < bestScore) {
                appSettings.edit().putInt("best_score", attemptsCount).apply();
                Toast.makeText(this, "Новый рекорд: " + attemptsCount + " попыток!", Toast.LENGTH_LONG).show();
            }

            showResultDialog("Победа!", "Вы угадали число " + numberToGuess + " за " + attemptsCount + " попыток.");
            isGameEnded = true;
            bControl.setText(getResources().getString(R.string.play_more));
            bControl.setEnabled(true);
        } else {
            if (attemptsCount >= MAX_ATTEMPTS) {
                showResultDialog("Проигрыш", "Попытки закончились! Число было: " + numberToGuess);
                isGameEnded = true;
                bControl.setText(getResources().getString(R.string.play_more));
                bControl.setEnabled(true);
            } else {
                if (inp > numberToGuess)
                    tvInfo.setText(getResources().getString(R.string.ahead));
                else
                    tvInfo.setText(getResources().getString(R.string.behind));

                etInput.setText("");
            }
        }
    }

    private void showResultDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Начать заново", (dialog, id) -> startNewGame())
                .setNegativeButton("Выход", (dialog, id) -> finish());
        AlertDialog alert = builder.create();
        alert.show();
    }
}
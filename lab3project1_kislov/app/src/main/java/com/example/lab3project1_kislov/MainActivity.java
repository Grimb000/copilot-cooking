package com.example.lab3project1_kislov;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView tvInfo;
    private TextView tvTries;
    private EditText etInput;
    private Button btnAction;
    private Button btnExit;
    private ProgressBar progressTries;
    private Spinner spRange;
    private CheckBox cbHints;
    private SwitchCompat swAnimations;
    private ImageView ivTarget;

    private int guess;
    private int tries;
    private int maxTries = 10;
    private int maxNumber = 100;
    private boolean gameFinished;

    private final ExecutorService progressExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tvInfo = findViewById(R.id.tvInfo);
        tvTries = findViewById(R.id.tvTries);
        etInput = findViewById(R.id.etInput);
        btnAction = findViewById(R.id.btnAction);
        btnExit = findViewById(R.id.btnExit);
        progressTries = findViewById(R.id.progressTries);
        spRange = findViewById(R.id.spRange);
        cbHints = findViewById(R.id.cbHints);
        swAnimations = findViewById(R.id.swAnimations);
        ivTarget = findViewById(R.id.ivTarget);

        btnAction.setOnClickListener(v -> handleAction());
        btnExit.setOnClickListener(v -> finish());

        spRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRange(position);
                startNewGame();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });

        startNewGame();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressExecutor.shutdownNow();
    }

    private void updateRange(int position) {
        switch (position) {
            case 0:
                maxNumber = 50;
                maxTries = 7;
                break;
            case 2:
                maxNumber = 200;
                maxTries = 12;
                break;
            default:
                maxNumber = 100;
                maxTries = 10;
                break;
        }
        progressTries.setMax(maxTries);
    }

    private void startNewGame() {
        guess = (int) (Math.random() * maxNumber) + 1;
        tries = 0;
        gameFinished = false;
        btnAction.setText(R.string.input_value);
        tvInfo.setText(getString(R.string.try_to_guess, maxNumber));
        tvTries.setText(getString(R.string.tries_status, tries, maxTries));
        etInput.setText("");
        progressTries.setProgress(0);
        maybeAnimate(ivTarget, R.anim.scale_pop);
    }

    private void handleAction() {
        if (gameFinished) {
            startNewGame();
            return;
        }

        String s = etInput.getText().toString().trim();
        if (s.isEmpty()) {
            showError(getString(R.string.error_empty));
            return;
        }

        int inp;
        try {
            inp = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_not_number));
            return;
        }

        if (inp < 1 || inp > maxNumber) {
            showError(getString(R.string.error_out_of_range, maxNumber));
            return;
        }

        tries++;
        updateTries();

        if (inp == guess) {
            gameFinished = true;
            btnAction.setText(R.string.play_more);
            showResultDialog(true);
            maybeAnimate(ivTarget, R.anim.combo_move_scale);
            return;
        }

        if (cbHints.isChecked()) {
            if (inp > guess) {
                tvInfo.setText(R.string.ahead);
            } else {
                tvInfo.setText(R.string.behind);
            }
        } else {
            tvInfo.setText(getString(R.string.try_to_guess, maxNumber));
        }

        maybeAnimate(tvInfo, R.anim.alpha_pulse);
        maybeAnimate(ivTarget, R.anim.slide_in);

        if (tries >= maxTries) {
            gameFinished = true;
            btnAction.setText(R.string.play_more);
            showResultDialog(false);
            maybeAnimate(ivTarget, R.anim.rotate);
        }
    }

    private void showError(String message) {
        tvInfo.setText(message);
        maybeAnimate(ivTarget, R.anim.rotate);
    }

    private void updateTries() {
        tvTries.setText(getString(R.string.tries_status, tries, maxTries));
        animateProgress(tries);
    }

    private void animateProgress(int target) {
        int start = progressTries.getProgress();
        progressExecutor.execute(() -> {
            int step = start <= target ? 1 : -1;
            for (int value = start; value != target; value += step) {
                int next = value + step;
                mainHandler.post(() -> progressTries.setProgress(next));
                try {
                    Thread.sleep(35);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
    }

    private void showResultDialog(boolean win) {
        String title = win ? getString(R.string.dialog_win_title) : getString(R.string.dialog_lose_title);
        String message = win
            ? getString(R.string.dialog_win_message, guess, tries)
            : getString(R.string.dialog_lose_message, guess);

        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.dialog_play_again, (dialog, which) -> startNewGame())
            .setNegativeButton(R.string.dialog_exit, (dialog, which) -> finish())
            .show();
    }

    private void maybeAnimate(View view, int animRes) {
        if (swAnimations.isChecked()) {
            view.startAnimation(AnimationUtils.loadAnimation(this, animRes));
        }
    }
}

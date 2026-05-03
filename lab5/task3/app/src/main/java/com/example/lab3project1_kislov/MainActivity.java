package com.example.lab3project1_kislov;

import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureStore;
import android.gesture.GestureStroke;
import android.gesture.Prediction;
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

import java.util.List;
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
    private GestureOverlayView gestureOverlay;
    private GestureLibrary gestureLibrary;

    private static final float GESTURE_SCORE_THRESHOLD = 2.0f;

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
        gestureOverlay = findViewById(R.id.gestureOverlay);

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

        setupGestureInput();
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

    private void setupGestureInput() {
        etInput.setShowSoftInputOnFocus(false);
        gestureLibrary = buildGestureLibrary();
        gestureOverlay.addOnGesturePerformedListener((overlay, gesture) -> handleGesture(gesture));
    }

    private void handleGesture(Gesture gesture) {
        List<Prediction> predictions = gestureLibrary.recognize(gesture);
        if (predictions.isEmpty() || predictions.get(0).score < GESTURE_SCORE_THRESHOLD) {
            showError(getString(R.string.gesture_not_recognized));
            return;
        }

        String name = predictions.get(0).name;
        if ("stop".equals(name)) {
            handleAction();
            return;
        }

        appendDigit(name);
    }

    private void appendDigit(String digit) {
        if (!digit.matches("\\d")) {
            showError(getString(R.string.gesture_not_recognized));
            return;
        }
        if (gameFinished) {
            startNewGame();
        }
        String current = etInput.getText().toString();
        int maxLength = String.valueOf(maxNumber).length();
        if (current.length() >= maxLength) {
            showError(getString(R.string.error_out_of_range, maxNumber));
            return;
        }
        String updated = "0".equals(current) ? digit : current + digit;
        etInput.setText(updated);
    }

    private GestureLibrary buildGestureLibrary() {
        GestureStore store = new GestureStore();
        store.setOrientationStyle(GestureStore.ORIENTATION_INVARIANT);
        store.setSequenceType(GestureStore.SEQUENCE_SENSITIVE);

        store.addGesture("0", createGesture(createCirclePoints(50f, 50f, 40f, 20)));
        store.addGesture("1", createGesture(new float[] {50f, 10f, 50f, 90f}));
        store.addGesture("2", createGesture(new float[] {20f, 25f, 80f, 25f, 80f, 50f, 20f, 80f, 80f, 80f}));
        store.addGesture("3", createGesture(new float[] {20f, 25f, 80f, 25f, 60f, 50f, 80f, 75f, 20f, 75f}));
        store.addGesture("4", createGesture(new float[] {80f, 20f, 20f, 60f, 80f, 60f, 80f, 90f}));
        store.addGesture("5", createGesture(new float[] {80f, 20f, 20f, 20f, 20f, 50f, 80f, 50f, 80f, 80f, 20f, 80f}));
        store.addGesture("6", createGesture(new float[] {80f, 25f, 30f, 25f, 20f, 50f, 20f, 80f, 80f, 80f, 80f, 50f, 20f, 50f}));
        store.addGesture("7", createGesture(new float[] {20f, 20f, 80f, 20f, 40f, 90f}));
        store.addGesture("8", createGesture(concatPoints(
            createCirclePoints(50f, 30f, 20f, 12),
            createCirclePoints(50f, 70f, 20f, 12)
        )));
        store.addGesture("9", createGesture(new float[] {20f, 80f, 80f, 80f, 80f, 30f, 50f, 20f, 20f, 30f, 20f, 50f, 80f, 50f}));
        store.addGesture("stop", createGesture(new float[] {20f, 55f, 45f, 80f, 80f, 20f}));

        return store;
    }

    private Gesture createGesture(float[] points) {
        long[] timestamps = new long[points.length / 2];
        long time = 0;
        for (int i = 0; i < timestamps.length; i++) {
            timestamps[i] = time;
            time += 10;
        }
        GestureStroke stroke = new GestureStroke(points, timestamps);
        Gesture gesture = new Gesture();
        gesture.addStroke(stroke);
        return gesture;
    }

    private float[] createCirclePoints(float centerX, float centerY, float radius, int steps) {
        float[] points = new float[(steps + 1) * 2];
        for (int i = 0; i <= steps; i++) {
            double angle = (Math.PI * 2 * i) / steps;
            points[i * 2] = centerX + (float) (radius * Math.cos(angle));
            points[i * 2 + 1] = centerY + (float) (radius * Math.sin(angle));
        }
        return points;
    }

    private float[] concatPoints(float[]... segments) {
        int total = 0;
        for (float[] segment : segments) {
            total += segment.length;
        }
        float[] combined = new float[total];
        int offset = 0;
        for (float[] segment : segments) {
            System.arraycopy(segment, 0, combined, offset, segment.length);
            offset += segment.length;
        }
        return combined;
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

package com.example.lab2project2_kislov;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    TextView tvInfo;
    EditText etInput;
    Button bControl;

    int tries;
    final int MAX_TRIES = 10;


    int guess;
    boolean gameFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tvInfo = (TextView) findViewById(R.id.textView1);
        etInput = (EditText) findViewById(R.id.editText1);
        bControl = (Button) findViewById(R.id.button1);


        guess = (int) (Math.random() * 100) + 1;
        tries = 0;
        gameFinished = false;

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    public void onClick(View v) {
        if (!gameFinished) {
            String s = etInput.getText().toString().trim();
            if (s.isEmpty()) {
                tvInfo.setText(getResources().getString(R.string.error));
                return;
            }

            int inp;
            try {
                inp = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                tvInfo.setText(getResources().getString(R.string.error));
                return;
            }

            if (inp < 1 || inp > 100) {
                tvInfo.setText(getResources().getString(R.string.out_of_range));
                return;
            }


            tries++;

            if (inp > guess) {
                tvInfo.setText(getResources().getString(R.string.ahead));
            } else if (inp < guess) {
                tvInfo.setText(getResources().getString(R.string.behind));
            } else {
                tvInfo.setText(getResources().getString(R.string.hit));
                bControl.setText(getResources().getString(R.string.play_more));
                gameFinished = true;
                return;
            }


            if (tries >= MAX_TRIES) {
                String msg = getString(R.string.no_more_tries, guess);
                tvInfo.setText(msg);
                bControl.setText(getResources().getString(R.string.play_more));
                gameFinished = true;
            }

        } else {

            guess = (int) (Math.random() * 100) + 1;
            gameFinished = false;
            tries = 0;
            bControl.setText(getResources().getString(R.string.input_value));
            tvInfo.setText(getResources().getString(R.string.try_to_guess));
            etInput.setText("");
        }
    }


    public void onExitClick(View v) {
        finish();
        // или finishAffinity();
    }

}

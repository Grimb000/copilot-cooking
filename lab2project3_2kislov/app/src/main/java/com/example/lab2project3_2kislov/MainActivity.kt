package com.example.lab2project3_2kislov

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvInfo: TextView
    private lateinit var etInput: EditText
    private lateinit var bControl: Button

    private var guess: Int = 0
    private var gameFinished: Boolean = false
    private var tries: Int = 0
    private val maxTries: Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tvInfo = findViewById(R.id.textView1)
        etInput = findViewById(R.id.editText1)
        bControl = findViewById(R.id.button1)

        startNewGame()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun startNewGame() {
        guess = (Math.random() * 100).toInt() + 1
        tries = 0
        gameFinished = false
        bControl.setText(R.string.input_value)
        tvInfo.setText(R.string.try_to_guess)
        etInput.setText("")
    }

    fun onClick(v: View) {
        if (!gameFinished) {
            val s = etInput.text.toString().trim()
            if (s.isEmpty()) {
                tvInfo.setText(R.string.error)
                return
            }

            val inp: Int = try {
                s.toInt()
            } catch (e: NumberFormatException) {
                tvInfo.setText(R.string.error)
                return
            }

            if (inp < 1 || inp > 100) {
                tvInfo.setText(R.string.out_of_range)
                return
            }

            tries++

            when {
                inp > guess -> tvInfo.setText(R.string.ahead)
                inp < guess -> tvInfo.setText(R.string.behind)
                else -> {
                    tvInfo.setText(R.string.hit)
                    bControl.setText(R.string.play_more)
                    gameFinished = true
                    return
                }
            }

            if (tries >= maxTries) {
                val msg = getString(R.string.no_more_tries, guess)
                tvInfo.text = msg
                bControl.setText(R.string.play_more)
                gameFinished = true
            }
        } else {
            startNewGame()
        }
    }

    fun onExitClick(v: View) {
        finish()
    }
}

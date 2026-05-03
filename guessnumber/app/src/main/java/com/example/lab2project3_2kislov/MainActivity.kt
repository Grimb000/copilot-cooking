package com.example.lab2project3_2kislov

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
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
    private lateinit var keyboardIndicator: View

    private lateinit var gameEngine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tvInfo = findViewById(R.id.textView1)
        etInput = findViewById(R.id.editText1)
        bControl = findViewById(R.id.button1)
        keyboardIndicator = findViewById(R.id.keyboardIndicator)
        gameEngine = GameEngine(NumberGeneratorProvider.generator)

        startNewGame()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            keyboardIndicator.visibility = if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                View.VISIBLE
            } else {
                View.GONE
            }
            insets
        }
    }

    private fun startNewGame() {
        gameEngine.startNewGame()
        bControl.setText(R.string.input_value)
        bControl.contentDescription = getString(R.string.input_value)
        tvInfo.setText(R.string.try_to_guess)
        etInput.setText("")
    }

    fun onClick(v: View) {
        hideKeyboard()
        if (gameEngine.isGameOver()) {
            startNewGame()
            return
        }

        val s = etInput.text.toString().trim()
        if (s.isEmpty()) {
            tvInfo.setText(R.string.error)
            return
        }

        val inp: Int = s.toIntOrNull() ?: run {
            tvInfo.setText(R.string.error)
            return
        }

        if (inp < 1 || inp > GameEngine.DEFAULT_MAX_NUMBER) {
            tvInfo.setText(R.string.out_of_range)
            return
        }

        val result = gameEngine.guess(inp)
        if (gameEngine.isOutOfTries()) {
            tvInfo.text = getString(R.string.no_more_tries, gameEngine.getTargetNumber())
        } else {
            tvInfo.text = result
        }

        if (gameEngine.isGameOver()) {
            bControl.setText(R.string.play_more)
            bControl.contentDescription = getString(R.string.play_more)
        }
    }

    fun onExitClick(v: View) {
        finish()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val token = currentFocus?.windowToken ?: etInput.windowToken
        imm.hideSoftInputFromWindow(token, 0)
        etInput.clearFocus()
    }
}

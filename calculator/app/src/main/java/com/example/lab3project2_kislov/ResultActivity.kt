package com.example.lab3project2_kislov

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        if (savedInstanceState == null) {
            val input = intent.getStringExtra(EXTRA_INPUT).orEmpty()
            val result = intent.getStringExtra(EXTRA_RESULT).orEmpty()
            supportFragmentManager.beginTransaction()
                .replace(R.id.result_container, ResultFragment.newInstance(input, result))
                .commit()
        }
    }

    companion object {
        private const val EXTRA_INPUT = "extra_input"
        private const val EXTRA_RESULT = "extra_result"

        fun newIntent(context: Context, input: String, result: String): Intent {
            return Intent(context, ResultActivity::class.java).apply {
                putExtra(EXTRA_INPUT, input)
                putExtra(EXTRA_RESULT, result)
            }
        }

        fun readInput(intent: Intent): String = intent.getStringExtra(EXTRA_INPUT).orEmpty()

        fun readResult(intent: Intent): String = intent.getStringExtra(EXTRA_RESULT).orEmpty()
    }
}

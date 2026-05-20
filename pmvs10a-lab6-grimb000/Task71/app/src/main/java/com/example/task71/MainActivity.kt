package com.example.task71

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var repository: GameRepository

    private lateinit var appStatusView: TextView
    private lateinit var profileStatusView: TextView
    private lateinit var gameInfoView: TextView
    private lateinit var attemptsView: TextView
    private lateinit var maxAttemptsInput: EditText
    private lateinit var guessInput: EditText

    private var settings = AppSettings()
    private var profile = UserProfile()
    private var secretNumber = 0
    private var attemptsUsed = 0
    private var isGameFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = GameRepository(this)

        appStatusView = findViewById(R.id.appStatusView)
        profileStatusView = findViewById(R.id.profileStatusView)
        gameInfoView = findViewById(R.id.gameInfoView)
        attemptsView = findViewById(R.id.attemptsView)
        maxAttemptsInput = findViewById(R.id.maxAttemptsInput)
        guessInput = findViewById(R.id.guessInput)

        findViewById<Button>(R.id.saveAttemptsButton).setOnClickListener {
            lifecycleScope.launch { saveMaxAttempts() }
        }
        findViewById<Button>(R.id.openLoginButton).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<Button>(R.id.checkGuessButton).setOnClickListener {
            lifecycleScope.launch { checkGuess() }
        }
        findViewById<Button>(R.id.restartButton).setOnClickListener {
            startNewGame()
        }

        lifecycleScope.launch {
            settings = repository.incrementLaunchCount()
            profile = repository.loadProfile()
            maxAttemptsInput.setText(settings.maxAttempts.toString())
            startNewGame()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            settings = repository.ensureSettings()
            profile = repository.loadProfile()
            maxAttemptsInput.setText(settings.maxAttempts.toString())
            renderDashboard()
        }
    }

    private fun startNewGame() {
        secretNumber = Random.nextInt(1, 101)
        attemptsUsed = 0
        isGameFinished = false
        guessInput.text.clear()
        gameInfoView.text = getString(R.string.enter_guess, settings.maxAttempts)
        renderDashboard()
    }

    private suspend fun saveMaxAttempts() {
        val newLimit = maxAttemptsInput.text.toString().toIntOrNull()
        if (newLimit == null || newLimit !in 1..20) {
            Toast.makeText(this, R.string.guess_invalid, Toast.LENGTH_SHORT).show()
            return
        }

        settings = settings.copy(maxAttempts = newLimit)
        repository.saveSettings(settings)
        Toast.makeText(this, getString(R.string.settings_saved, newLimit), Toast.LENGTH_SHORT).show()
        startNewGame()
    }

    private suspend fun checkGuess() {
        if (isGameFinished) {
            Toast.makeText(this, R.string.restart_game, Toast.LENGTH_SHORT).show()
            return
        }

        val guess = guessInput.text.toString().toIntOrNull()
        if (guess == null || guess !in 1..100) {
            Toast.makeText(this, R.string.guess_invalid, Toast.LENGTH_SHORT).show()
            return
        }

        attemptsUsed += 1

        when {
            guess < secretNumber -> gameInfoView.text = getString(R.string.guess_too_small)
            guess > secretNumber -> gameInfoView.text = getString(R.string.guess_too_large)
            else -> {
                val score = (settings.maxAttempts - attemptsUsed + 1).coerceAtLeast(1) * 10
                settings = settings.copy(
                    gamesPlayed = settings.gamesPlayed + 1,
                    gamesWon = settings.gamesWon + 1,
                    bestScore = maxOf(settings.bestScore, score),
                    totalScore = settings.totalScore + score
                )
                repository.saveSettings(settings)
                isGameFinished = true
                gameInfoView.text = getString(R.string.guess_win, secretNumber, attemptsUsed, score)
                renderDashboard()
                return
            }
        }

        if (attemptsUsed >= settings.maxAttempts) {
            settings = settings.copy(gamesPlayed = settings.gamesPlayed + 1)
            repository.saveSettings(settings)
            isGameFinished = true
            gameInfoView.text = getString(R.string.guess_lose, secretNumber)
        }

        renderDashboard()
    }

    private fun renderDashboard() {
        appStatusView.text = getString(
            R.string.app_status_template,
            settings.launchCount,
            settings.appUuid,
            settings.gamesPlayed,
            settings.gamesWon,
            settings.bestScore,
            settings.totalScore,
            settings.maxAttempts
        )

        profileStatusView.text = if (profile.email.isBlank()) {
            getString(R.string.profile_empty)
        } else {
            getString(R.string.profile_status_template, profile.email, profile.password)
        }

        attemptsView.text = getString(R.string.attempts_info, attemptsUsed, settings.maxAttempts)
    }
}

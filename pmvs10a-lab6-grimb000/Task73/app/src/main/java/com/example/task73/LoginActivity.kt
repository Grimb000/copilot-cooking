package com.example.task73

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var repository: GameRepository
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var profileSummaryView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        repository = GameRepository(applicationContext)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        profileSummaryView = findViewById(R.id.profileSummaryView)

        findViewById<Button>(R.id.saveProfileButton).setOnClickListener {
            lifecycleScope.launch { saveProfile() }
        }
        findViewById<Button>(R.id.clearProfileButton).setOnClickListener {
            lifecycleScope.launch { clearProfile() }
        }

        lifecycleScope.launch {
            loadProfile()
        }
    }

    private suspend fun loadProfile() {
        val profile = repository.loadProfile()
        emailInput.setText(profile.email)
        passwordInput.setText(profile.password)
        profileSummaryView.text = if (profile.email.isBlank()) {
            getString(R.string.profile_summary_empty)
        } else {
            getString(R.string.profile_summary_template, profile.email, profile.password)
        }
    }

    private suspend fun saveProfile() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, R.string.fill_profile_fields, Toast.LENGTH_SHORT).show()
            return
        }

        repository.saveProfile(UserProfile(email, password))
        loadProfile()
        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show()
    }

    private suspend fun clearProfile() {
        repository.clearProfile()
        loadProfile()
        Toast.makeText(this, R.string.profile_cleared, Toast.LENGTH_SHORT).show()
    }
}

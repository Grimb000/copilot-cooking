package com.example.task1

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var lastLoginView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        usernameField = findViewById(R.id.usernameInput)
        passwordField = findViewById(R.id.passwordInput)
        lastLoginView = findViewById(R.id.lastLoginValue)

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            performLogin()
        }

        restoreState()
    }

    override fun onResume() {
        super.onResume()
        restoreState()
    }

    private fun restoreState() {
        usernameField.setText(preferences.getString(KEY_USERNAME, ""))
        passwordField.setText(preferences.getString(KEY_PASSWORD, ""))

        val lastLogin = preferences.getString(KEY_LAST_LOGIN, null)
        lastLoginView.text = lastLogin ?: getString(R.string.no_last_login)
    }

    private fun performLogin() {
        val username = usernameField.text.toString().trim()
        val password = passwordField.text.toString().trim()

        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
            return
        }

        val lastLogin = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            .format(Date())

        preferences.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_LAST_LOGIN, lastLogin)
            .apply()

        startActivity(Intent(this, DetailsActivity::class.java))
    }

    companion object {
        const val PREFS_NAME = "task1_user_profile"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"
        const val KEY_LAST_LOGIN = "last_login"
    }
}

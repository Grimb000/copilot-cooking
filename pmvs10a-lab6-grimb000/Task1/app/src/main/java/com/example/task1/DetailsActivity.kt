package com.example.task1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)

        findViewById<TextView>(R.id.usernameValue).text =
            preferences.getString(MainActivity.KEY_USERNAME, "")
        findViewById<TextView>(R.id.passwordValue).text =
            preferences.getString(MainActivity.KEY_PASSWORD, "")
        findViewById<TextView>(R.id.lastLoginStoredValue).text =
            preferences.getString(MainActivity.KEY_LAST_LOGIN, getString(R.string.no_last_login))

        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            preferences.edit().clear().apply()

            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }
    }
}

package com.example.task71

import android.content.Context
import java.util.UUID

class GameRepository(context: Context) {
    private val appPrefs = context.getSharedPreferences(APP_SETTINGS_FILE, Context.MODE_PRIVATE)
    private val profilePrefs = context.getSharedPreferences(USER_PROFILE_FILE, Context.MODE_PRIVATE)

    suspend fun ensureSettings(): AppSettings {
        val current = loadSettings()
        return if (current.appUuid.isBlank()) {
            val updated = current.copy(appUuid = UUID.randomUUID().toString())
            saveSettings(updated)
            updated
        } else {
            current
        }
    }

    suspend fun loadSettings(): AppSettings = AppSettings(
        launchCount = appPrefs.getInt(KEY_LAUNCH_COUNT, 0),
        appUuid = appPrefs.getString(KEY_APP_UUID, "") ?: "",
        maxAttempts = appPrefs.getInt(KEY_MAX_ATTEMPTS, 10),
        gamesPlayed = appPrefs.getInt(KEY_GAMES_PLAYED, 0),
        gamesWon = appPrefs.getInt(KEY_GAMES_WON, 0),
        bestScore = appPrefs.getInt(KEY_BEST_SCORE, 0),
        totalScore = appPrefs.getInt(KEY_TOTAL_SCORE, 0)
    )

    suspend fun saveSettings(settings: AppSettings) {
        appPrefs.edit()
            .putInt(KEY_LAUNCH_COUNT, settings.launchCount)
            .putString(KEY_APP_UUID, settings.appUuid)
            .putInt(KEY_MAX_ATTEMPTS, settings.maxAttempts)
            .putInt(KEY_GAMES_PLAYED, settings.gamesPlayed)
            .putInt(KEY_GAMES_WON, settings.gamesWon)
            .putInt(KEY_BEST_SCORE, settings.bestScore)
            .putInt(KEY_TOTAL_SCORE, settings.totalScore)
            .apply()
    }

    suspend fun incrementLaunchCount(): AppSettings {
        val updated = ensureSettings().copy(launchCount = loadSettings().launchCount + 1)
        saveSettings(updated)
        return updated
    }

    suspend fun loadProfile(): UserProfile = UserProfile(
        email = profilePrefs.getString(KEY_EMAIL, "") ?: "",
        password = profilePrefs.getString(KEY_PASSWORD, "") ?: ""
    )

    suspend fun saveProfile(profile: UserProfile) {
        profilePrefs.edit()
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_PASSWORD, profile.password)
            .apply()
    }

    suspend fun clearProfile() {
        profilePrefs.edit().clear().apply()
    }

    companion object {
        const val APP_SETTINGS_FILE = "app_settings"
        const val USER_PROFILE_FILE = "user_profile"
        private const val KEY_LAUNCH_COUNT = "launch_count"
        private const val KEY_APP_UUID = "app_uuid"
        private const val KEY_MAX_ATTEMPTS = "max_attempts"
        private const val KEY_GAMES_PLAYED = "games_played"
        private const val KEY_GAMES_WON = "games_won"
        private const val KEY_BEST_SCORE = "best_score"
        private const val KEY_TOTAL_SCORE = "total_score"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }
}

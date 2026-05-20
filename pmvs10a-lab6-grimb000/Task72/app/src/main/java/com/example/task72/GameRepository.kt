package com.example.task72

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.UUID

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")
private val Context.userProfileDataStore by preferencesDataStore(name = "user_profile")

class GameRepository(private val context: Context) {
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

    suspend fun loadSettings(): AppSettings {
        val preferences = context.appSettingsDataStore.data.first()
        return AppSettings(
            launchCount = preferences[KEY_LAUNCH_COUNT] ?: 0,
            appUuid = preferences[KEY_APP_UUID] ?: "",
            maxAttempts = preferences[KEY_MAX_ATTEMPTS] ?: 10,
            gamesPlayed = preferences[KEY_GAMES_PLAYED] ?: 0,
            gamesWon = preferences[KEY_GAMES_WON] ?: 0,
            bestScore = preferences[KEY_BEST_SCORE] ?: 0,
            totalScore = preferences[KEY_TOTAL_SCORE] ?: 0
        )
    }

    suspend fun saveSettings(settings: AppSettings) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[KEY_LAUNCH_COUNT] = settings.launchCount
            preferences[KEY_APP_UUID] = settings.appUuid
            preferences[KEY_MAX_ATTEMPTS] = settings.maxAttempts
            preferences[KEY_GAMES_PLAYED] = settings.gamesPlayed
            preferences[KEY_GAMES_WON] = settings.gamesWon
            preferences[KEY_BEST_SCORE] = settings.bestScore
            preferences[KEY_TOTAL_SCORE] = settings.totalScore
        }
    }

    suspend fun incrementLaunchCount(): AppSettings {
        val updated = ensureSettings().copy(launchCount = loadSettings().launchCount + 1)
        saveSettings(updated)
        return updated
    }

    suspend fun loadProfile(): UserProfile {
        val preferences = context.userProfileDataStore.data.first()
        return UserProfile(
            email = preferences[KEY_EMAIL] ?: "",
            password = preferences[KEY_PASSWORD] ?: ""
        )
    }

    suspend fun saveProfile(profile: UserProfile) {
        context.userProfileDataStore.edit { preferences ->
            preferences[KEY_EMAIL] = profile.email
            preferences[KEY_PASSWORD] = profile.password
        }
    }

    suspend fun clearProfile() {
        context.userProfileDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        private val KEY_LAUNCH_COUNT = intPreferencesKey("launch_count")
        private val KEY_APP_UUID = stringPreferencesKey("app_uuid")
        private val KEY_MAX_ATTEMPTS = intPreferencesKey("max_attempts")
        private val KEY_GAMES_PLAYED = intPreferencesKey("games_played")
        private val KEY_GAMES_WON = intPreferencesKey("games_won")
        private val KEY_BEST_SCORE = intPreferencesKey("best_score")
        private val KEY_TOTAL_SCORE = intPreferencesKey("total_score")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PASSWORD = stringPreferencesKey("password")
    }
}

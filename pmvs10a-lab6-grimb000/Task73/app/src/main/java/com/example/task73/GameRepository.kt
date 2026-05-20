package com.example.task73

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.task73.proto.AppSettingsProto
import com.example.task73.proto.UserProfileProto
import kotlinx.coroutines.flow.first
import java.util.UUID

private val Context.appSettingsDataStore: DataStore<AppSettingsProto> by dataStore(
    fileName = "settings.pb",
    serializer = AppSettingsSerializer
)

private val Context.userProfileDataStore: DataStore<UserProfileProto> by dataStore(
    fileName = "user_profile.pb",
    serializer = UserProfileSerializer
)

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
        val proto = context.appSettingsDataStore.data.first()
        return AppSettings(
            launchCount = proto.launchCount,
            appUuid = proto.appUuid,
            maxAttempts = if (proto.maxAttempts == 0) 10 else proto.maxAttempts,
            gamesPlayed = proto.gamesPlayed,
            gamesWon = proto.gamesWon,
            bestScore = proto.bestScore,
            totalScore = proto.totalScore
        )
    }

    suspend fun saveSettings(settings: AppSettings) {
        context.appSettingsDataStore.updateData { current ->
            current.toBuilder()
                .setLaunchCount(settings.launchCount)
                .setAppUuid(settings.appUuid)
                .setMaxAttempts(settings.maxAttempts)
                .setGamesPlayed(settings.gamesPlayed)
                .setGamesWon(settings.gamesWon)
                .setBestScore(settings.bestScore)
                .setTotalScore(settings.totalScore)
                .build()
        }
    }

    suspend fun incrementLaunchCount(): AppSettings {
        val updated = ensureSettings().copy(launchCount = loadSettings().launchCount + 1)
        saveSettings(updated)
        return updated
    }

    suspend fun loadProfile(): UserProfile {
        val proto = context.userProfileDataStore.data.first()
        return UserProfile(proto.email, proto.password)
    }

    suspend fun saveProfile(profile: UserProfile) {
        context.userProfileDataStore.updateData { current ->
            current.toBuilder()
                .setEmail(profile.email)
                .setPassword(profile.password)
                .build()
        }
    }

    suspend fun clearProfile() {
        context.userProfileDataStore.updateData {
            it.toBuilder()
                .clearEmail()
                .clearPassword()
                .build()
        }
    }
}

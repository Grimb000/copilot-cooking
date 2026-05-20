package com.example.task71

data class AppSettings(
    val launchCount: Int = 0,
    val appUuid: String = "",
    val maxAttempts: Int = 10,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val bestScore: Int = 0,
    val totalScore: Int = 0
)

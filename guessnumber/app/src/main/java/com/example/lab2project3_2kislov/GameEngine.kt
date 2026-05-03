package com.example.lab2project3_2kislov

class GameEngine(
    private val numberGenerator: NumberGenerator,
    private val maxNumber: Int = DEFAULT_MAX_NUMBER,
    private val maxTries: Int = DEFAULT_MAX_TRIES
) {

    private var targetNumber: Int = 0
    private var tries: Int = 0
    private var gameOver: Boolean = false
    private var outOfTries: Boolean = false

    init {
        startNewGame()
    }

    fun startNewGame() {
        targetNumber = numberGenerator.nextInt(maxNumber)
        tries = 0
        gameOver = false
        outOfTries = false
    }

    fun guess(value: Int): String {
        if (gameOver) {
            return if (outOfTries) RESULT_OUT_OF_TRIES else RESULT_CORRECT
        }

        tries++
        val result = when {
            value < targetNumber -> RESULT_HIGHER
            value > targetNumber -> RESULT_LOWER
            else -> RESULT_CORRECT
        }

        if (result == RESULT_CORRECT) {
            gameOver = true
            outOfTries = false
        } else if (tries >= maxTries) {
            gameOver = true
            outOfTries = true
        }

        return result
    }

    fun isGameOver(): Boolean = gameOver

    fun isOutOfTries(): Boolean = outOfTries

    fun getTargetNumber(): Int = targetNumber

    companion object {
        const val DEFAULT_MAX_NUMBER = 100
        const val DEFAULT_MAX_TRIES = 10
        const val RESULT_HIGHER = "Больше"
        const val RESULT_LOWER = "Меньше"
        const val RESULT_CORRECT = "Угадал"
        const val RESULT_OUT_OF_TRIES = "Попытки закончились"
    }
}

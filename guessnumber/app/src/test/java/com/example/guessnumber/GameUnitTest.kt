package com.example.guessnumber

import com.example.lab2project3_2kislov.GameEngine
import com.example.lab2project3_2kislov.NumberGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class GameUnitTest {

    private fun createEngine(maxTries: Int = 10): GameEngine {
        val generator = Mockito.mock(NumberGenerator::class.java)
        Mockito.`when`(generator.nextInt(Mockito.anyInt())).thenReturn(42)
        return GameEngine(generator, maxNumber = 100, maxTries = maxTries)
    }

    @Test
    fun testGuess_Lower() {
        val engine = createEngine()
        assertEquals(GameEngine.RESULT_HIGHER, engine.guess(30))
        assertFalse(engine.isGameOver())
    }

    @Test
    fun testGuess_Higher() {
        val engine = createEngine()
        assertEquals(GameEngine.RESULT_LOWER, engine.guess(50))
        assertFalse(engine.isGameOver())
    }

    @Test
    fun testGuess_Correct() {
        val engine = createEngine()
        assertEquals(GameEngine.RESULT_CORRECT, engine.guess(42))
        assertTrue(engine.isGameOver())
    }

    @Test
    fun testGuess_OutOfTries() {
        val engine = createEngine(maxTries = 1)
        engine.guess(30)
        assertTrue(engine.isGameOver())
        assertTrue(engine.isOutOfTries())
    }

    @Test
    fun testStartNewGameResetsState() {
        val engine = createEngine()
        engine.guess(42)
        assertTrue(engine.isGameOver())
        engine.startNewGame()
        assertFalse(engine.isGameOver())
    }
}

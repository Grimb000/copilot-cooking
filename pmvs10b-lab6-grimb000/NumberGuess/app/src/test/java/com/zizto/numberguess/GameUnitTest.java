package com.zizto.numberguess;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GameUnitTest {

    private NumberGenerator mockGenerator;
    private NumberGame game;

    @Before
    public void setUp() {
        mockGenerator = mock(NumberGenerator.class);

        when(mockGenerator.generate()).thenReturn(42);

        game = new NumberGame(mockGenerator);
    }

    @Test
    public void testGuess_Lower_ReturnsBehind() {
        GuessResult result = game.checkGuess(30);
        assertEquals(GuessResult.BEHIND, result);
        assertFalse(game.isGameOver());
    }

    @Test
    public void testGuess_Higher_ReturnsAhead() {
        GuessResult result = game.checkGuess(50);
        assertEquals(GuessResult.AHEAD, result);
        assertFalse(game.isGameOver());
    }

    @Test
    public void testGuess_Correct_ReturnsHit() {
        GuessResult result = game.checkGuess(42);
        assertEquals(GuessResult.HIT, result);
        assertTrue(game.isGameOver());
    }

    @Test
    public void testGuess_MaxAttempts_ReturnsGameOver() {
        for (int i = 0; i < 9; i++) {
            game.checkGuess(10);
        }

        GuessResult finalResult = game.checkGuess(10);

        assertEquals(GuessResult.GAME_OVER_LOSE, finalResult);
        assertTrue(game.isGameOver());
        assertEquals(10, game.getAttemptsCount());
    }

    @Test
    public void testGameReset_ClearsAttempts() {
        game.checkGuess(10);
        assertEquals(1, game.getAttemptsCount());

        game.startNewGame();

        assertEquals(0, game.getAttemptsCount());
        assertFalse(game.isGameOver());
    }
}
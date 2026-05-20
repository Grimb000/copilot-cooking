package com.zizto.numberguess;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ParamTest {

    private NumberGame game;
    private NumberGenerator mockGenerator;

    private int guessValue;
    private int targetNumber;
    private GuessResult expectedResult;

    public ParamTest(int guessValue, int targetNumber, GuessResult expectedResult) {
        this.guessValue = guessValue;
        this.targetNumber = targetNumber;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "Test: guess={0}, target={1}, expected={2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {30, 50, GuessResult.BEHIND},
                {80, 50, GuessResult.AHEAD},
                {50, 50, GuessResult.HIT},
                {1, 99, GuessResult.BEHIND},
                {99, 1, GuessResult.AHEAD}
        });
    }

    @Before
    public void setUp() {
        mockGenerator = mock(NumberGenerator.class);
        when(mockGenerator.generate()).thenReturn(targetNumber);

        game = new NumberGame(mockGenerator);
    }

    @Test
    public void testCheckGuessMultipleScenarios() {
        GuessResult actualResult = game.checkGuess(guessValue);

        assertEquals(expectedResult, actualResult);
    }
}

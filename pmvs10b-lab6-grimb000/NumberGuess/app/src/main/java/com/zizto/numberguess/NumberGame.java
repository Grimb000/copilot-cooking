package com.zizto.numberguess;

public class NumberGame {
    private final int MAX_ATTEMPTS = 10;
    private int numberToGuess;
    private int attemptsCount;
    private boolean isGameEnded;
    private NumberGenerator generator;

    public NumberGame(NumberGenerator generator) {
        this.generator = generator;
        startNewGame();
    }

    public void startNewGame() {
        numberToGuess = generator.generate();
        attemptsCount = 0;
        isGameEnded = false;
    }

    public GuessResult checkGuess(int guess) {
        if (isGameEnded) {
            return GuessResult.GAME_OVER_LOSE;
        }

        attemptsCount++;

        if (guess == numberToGuess) {
            isGameEnded = true;
            return GuessResult.HIT;
        }

        if (attemptsCount >= MAX_ATTEMPTS) {
            isGameEnded = true;
            return GuessResult.GAME_OVER_LOSE;
        }

        if (guess > numberToGuess) {
            return GuessResult.AHEAD;
        } else {
            return GuessResult.BEHIND;
        }
    }

    public int getAttemptsCount() {
        return attemptsCount;
    }

    public boolean isGameOver() {
        return isGameEnded;
    }
}

package com.example.calculator

import com.example.lab3project2_kislov.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CalculatorUnitTest {

    private val engine = CalculatorEngine()

    @Test
    fun testAddition_Positive() {
        assertEquals(5, engine.add(2, 3))
    }

    @Test
    fun testDivision_ByZero() {
        assertThrows(ArithmeticException::class.java) {
            engine.divide(10, 0)
        }
    }

    @Test
    fun testSubtraction_NegativeResult() {
        assertEquals(-2, engine.subtract(3, 5))
    }

    @Test
    fun testMultiplication_WithZero() {
        assertEquals(0, engine.multiply(10, 0))
    }

    @Test
    fun testAddition_NegativeNumbers() {
        assertEquals(-4, engine.add(-7, 3))
    }
}

package com.example.lab3project2_kislov

class CalculatorEngine {
    fun add(left: Int, right: Int): Int = left + right

    fun subtract(left: Int, right: Int): Int = left - right

    fun multiply(left: Int, right: Int): Int = left * right

    fun divide(left: Int, right: Int): Int {
        if (right == 0) {
            throw ArithmeticException("Division by zero")
        }
        return left / right
    }
}

package com.example.calculator

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.lab3project2_kislov.MainActivity
import com.example.lab3project2_kislov.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class CalculatorUITest {

    @Test
    fun testUi_SimpleAddition() {
        ActivityScenario.launch(MainActivity::class.java).use {
            switchToBasicMode()
            pressDigits(2)
            onView(withId(R.id.btnCalcAdd)).perform(click())
            pressDigits(3)
            onView(withId(R.id.btnCalcEquals)).perform(click())
            onView(withId(R.id.tvCalcDisplay)).check(matches(withText("5")))
        }
    }

    @Test
    fun testUi_ChainOperations() {
        ActivityScenario.launch(MainActivity::class.java).use {
            switchToBasicMode()
            pressDigits(5)
            onView(withId(R.id.btnCalcAdd)).perform(click())
            pressDigits(5)
            onView(withId(R.id.btnCalcSub)).perform(click())
            pressDigits(2)
            onView(withId(R.id.btnCalcEquals)).perform(click())
            onView(withId(R.id.tvCalcDisplay)).check(matches(withText("8")))
        }
    }

    @Test
    fun testUi_DivisionByZero_ShowsError() {
        ActivityScenario.launch(MainActivity::class.java).use {
            switchToBasicMode()
            pressDigits(1)
            pressDigits(0)
            onView(withId(R.id.btnCalcDiv)).perform(click())
            pressDigits(0)
            onView(withId(R.id.btnCalcEquals)).perform(click())
            onView(withId(R.id.tvCalcError)).check(matches(withText(R.string.error_div_zero)))
        }
    }

    @Test
    fun testUi_MultiplicationByZero() {
        ActivityScenario.launch(MainActivity::class.java).use {
            switchToBasicMode()
            pressDigits(7)
            onView(withId(R.id.btnCalcMul)).perform(click())
            pressDigits(0)
            onView(withId(R.id.btnCalcEquals)).perform(click())
            onView(withId(R.id.tvCalcDisplay)).check(matches(withText("0")))
        }
    }

    @Test
    fun testUi_ClearResetsDisplay() {
        ActivityScenario.launch(MainActivity::class.java).use {
            switchToBasicMode()
            pressDigits(9)
            onView(withId(R.id.btnCalcClear)).perform(click())
            onView(withId(R.id.tvCalcDisplay)).check(matches(withText(R.string.calc_display_default)))
        }
    }

    @Test
    fun testUi_RotationSavesInput() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.rbModeBasic)).perform(click())
            pressDigits(1)
            pressDigits(2)
            pressDigits(3)
            scenario.recreate()
            onView(withId(R.id.tvCalcDisplay)).check(matches(withText("123")))
        }
    }

    private fun switchToBasicMode() {
        onView(withId(R.id.rbModeBasic)).perform(click())
    }

    private fun pressDigits(value: Int) {
        value.toString().forEach { digit ->
            val buttonId = when (digit) {
                '0' -> R.id.btnCalcDigit0
                '1' -> R.id.btnCalcDigit1
                '2' -> R.id.btnCalcDigit2
                '3' -> R.id.btnCalcDigit3
                '4' -> R.id.btnCalcDigit4
                '5' -> R.id.btnCalcDigit5
                '6' -> R.id.btnCalcDigit6
                '7' -> R.id.btnCalcDigit7
                '8' -> R.id.btnCalcDigit8
                else -> R.id.btnCalcDigit9
            }
            onView(withId(buttonId)).perform(click())
        }
    }
}

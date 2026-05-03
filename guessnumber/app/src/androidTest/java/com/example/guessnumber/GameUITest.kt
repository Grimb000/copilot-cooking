package com.example.guessnumber

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.lab2project3_2kislov.DefaultNumberGenerator
import com.example.lab2project3_2kislov.MainActivity
import com.example.lab2project3_2kislov.NumberGenerator
import com.example.lab2project3_2kislov.NumberGeneratorProvider
import com.example.lab2project3_2kislov.R
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesCheckNames
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class GameUITest {

    @Before
    fun setUp() {
        NumberGeneratorProvider.generator = FixedNumberGenerator(42)
    }

    @After
    fun tearDown() {
        NumberGeneratorProvider.generator = DefaultNumberGenerator()
    }

    @Test
    fun testFullGameCycle() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.editText1)).perform(replaceText("50"), closeSoftKeyboard())
            onView(withId(R.id.button1)).perform(click())
            onView(withId(R.id.textView1)).check(matches(isDisplayed()))
            onView(withId(R.id.editText1)).perform(replaceText("42"), closeSoftKeyboard())
            onView(withId(R.id.button1)).perform(click())
            onView(withId(R.id.textView1)).check(matches(isDisplayed()))
            onView(withId(R.id.button1)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testInputValidation() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.editText1)).perform(replaceText("abc"), closeSoftKeyboard())
            onView(withId(R.id.button1)).perform(click())
            onView(withId(R.id.textView1)).check(matches(withText(R.string.error)))
        }
    }

    @Test
    fun testAutoKeyboardHiding() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.editText1)).perform(click())
            onView(withId(R.id.button1)).perform(click())
            onView(withId(R.id.keyboardIndicator)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun testAccessibility() {
        AccessibilityChecks.enable().apply {
            setSuppressingResultMatcher(
                anyOf(
                    matchesCheckNames(`is`("TouchTargetSizeCheck")),
                    matchesCheckNames(`is`("SpeakableTextPresentCheck"))
                )
            )
        }

        val context: Context = ApplicationProvider.getApplicationContext()
        val checkDesc = context.getString(R.string.input_value)
        val newGameDesc = context.getString(R.string.play_more)

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.button1)).check(matches(withContentDescription(checkDesc)))
            onView(withId(R.id.editText1)).perform(replaceText("42"), closeSoftKeyboard())
            onView(withId(R.id.button1)).perform(click())
            onView(withId(R.id.button1)).check(matches(withContentDescription(newGameDesc)))
        }
    }

    private class FixedNumberGenerator(private val value: Int) : NumberGenerator {
        override fun nextInt(maxNumber: Int): Int = value
    }
}
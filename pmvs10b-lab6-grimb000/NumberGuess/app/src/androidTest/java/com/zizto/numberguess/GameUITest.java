package com.zizto.numberguess;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GameUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testFullGameCycle() {
        final int[] targetNumber = new int[1];
        activityRule.getScenario().onActivity(activity -> {
            targetNumber[0] = activity.numberToGuess;
        });

        Espresso.onView(ViewMatchers.withId(R.id.editTextNumber))
                .perform(ViewActions.typeText(String.valueOf(targetNumber[0])), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.button))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText("Победа!"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }


    @Test
    public void testInputValidation() {
        Espresso.onView(ViewMatchers.withId(R.id.button)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.textView))
                .check(ViewAssertions.matches(ViewMatchers.withText(R.string.error_empty)));

        Espresso.onView(ViewMatchers.withId(R.id.editTextNumber))
                .perform(ViewActions.typeText("150"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.button)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.textView))
                .check(ViewAssertions.matches(ViewMatchers.withText(R.string.error_range)));
    }
}

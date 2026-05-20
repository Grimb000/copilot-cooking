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
public class LocalUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testPluralsRussianLocalization() {
        Espresso.onView(ViewMatchers.withId(R.id.tvAttempts))
                .check(ViewAssertions.matches(ViewMatchers.withText("0 try from 10")));

        Espresso.onView(ViewMatchers.withId(R.id.editTextNumber))
                .perform(ViewActions.typeText("50"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.button)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.tvAttempts))
                .check(ViewAssertions.matches(ViewMatchers.withText("1 try from 10")));

        Espresso.onView(ViewMatchers.withId(R.id.editTextNumber))
                .perform(ViewActions.typeText("60"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.button)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.tvAttempts))
                .check(ViewAssertions.matches(ViewMatchers.withText("2 try from 10")));
    }
}

package com.zizto.numberguess;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.accessibility.AccessibilityChecks;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdvUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void enableAccessibilityChecks() {
        AccessibilityChecks.enable().setRunChecksFromRootView(true);
    }

    @Test
    public void testAttemptsCounterUpdates() {
        Espresso.onView(ViewMatchers.withId(R.id.editTextNumber))
                .perform(ViewActions.typeText("50"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.button)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.tvAttempts))
                .check(ViewAssertions.matches(ViewMatchers.withText(org.hamcrest.Matchers.containsString("1/10"))));
    }

    @Test
    public void testFocusMovesCorrectly() {
        Espresso.onView(ViewMatchers.withId(R.id.editTextNumber))
                .check(ViewAssertions.matches(org.hamcrest.Matchers.not(ViewMatchers.hasFocus())));

        Espresso.onView(ViewMatchers.withId(R.id.editTextNumber)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.editTextNumber))
                .check(ViewAssertions.matches(ViewMatchers.hasFocus()));
    }
}

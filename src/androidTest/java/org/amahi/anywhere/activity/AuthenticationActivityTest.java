package org.amahi.anywhere.activity;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.amahi.anywhere.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AuthenticationActivityTest {
    @Rule
    public ActivityTestRule<AuthenticationActivity> authenticationActivityTestRule =
        new ActivityTestRule(AuthenticationActivity.class);

    @Test
    public void testIsErrorMessageDisplayed_UsernameOrPasswordIsEmpty() {
        onView(withId(R.id.username_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.password_layout)).check(matches(isDisplayed()));

        onView(withId(R.id.button_authentication))
            .check(matches(isDisplayed()))
            .perform(click());

        onView(withId(R.id.text_message_authentication_empty))
            .check(matches(isDisplayed()));
    }
}

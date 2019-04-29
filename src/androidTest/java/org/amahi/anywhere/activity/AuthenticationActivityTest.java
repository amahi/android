package org.amahi.anywhere.activity;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import org.amahi.anywhere.R;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AuthenticationActivityTest {
    @Rule
    public ActivityTestRule<AuthenticationActivity> authenticationActivityTestRule =
        new ActivityTestRule(AuthenticationActivity.class);

    @Before
    public void unlockScreen() {
        final AuthenticationActivity activity = authenticationActivityTestRule.getActivity();
        Runnable wakeUpDevice = new Runnable() {
            public void run() {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        };
        activity.runOnUiThread(wakeUpDevice);
    }

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

package org.amahi.anywhere.activity;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.amahi.anywhere.R;
import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@LargeTest

public class UiAutomatedTest {

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

    private void showError(String permission) {
        Description description = new StringDescription();
        description.appendText("Expected permission ")
            .appendText(permission)
            .appendText(" is missing from AndroidManifest.xml");

        throw new AssertionError(description.toString());
    }

}

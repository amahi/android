package org.amahi.anywhere.activity;

import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.amahi.anywhere.R;
import org.amahi.anywhere.util.Android;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.DEFAULT)

public class ServerFileImageActivityTest {

    @Rule
    public ActivityTestRule<RecentFilesActivity> mActivityRule = new ActivityTestRule<>(
        RecentFilesActivity.class
    );

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test1(){
        onView(withText(R.string.title_recent_files)).check(matches(isDisplayed()));
    }

    @Test
    public void test2(){
        onView(withId(R.id.recent_list)).check(matches(not(isDisplayed())));
    }

    @After
    public void tearDown() throws Exception {
    }
}

package org.amahi.anywhere.activity;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.amahi.anywhere.R;
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

public class ExpandedControlsActivityTest {

    @Rule
    public ActivityTestRule<ExpandedControlsActivity> mActivityRule = new ActivityTestRule<>(
        ExpandedControlsActivity.class
    );

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test1(){
        onView(withId(R.menu.action_bar_expanded_controller));
    }

    @Test
    public void test2(){
        onView(withId(R.id.media_route_menu_item));
    }

    @After
    public void tearDown() throws Exception {
    }
}

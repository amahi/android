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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.DEFAULT)

public class IntroductionActivityTest {

    @Rule
    public ActivityTestRule<IntroductionActivity> mActivityRule = new ActivityTestRule<>(
        IntroductionActivity.class
    );

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test1(){
        onView(withText(R.string.intro_phone_1)).check(matches(isDisplayed()));
        onView(withText(R.string.intro_desc_phone_1)).check(matches(isDisplayed()));
    }

    @Test
    public void test2(){
        onView(withText(R.string.intro_title_2));
        onView(withText(R.string.intro_desc_2));
    }

    @Test
    public void test3(){
        onView(withText(R.string.intro_title_3));
        onView(withText(R.string.intro_desc_phone_3));
    }

    @Test
    public void test4(){
        onView(withText(R.string.intro_title_4));
        onView(withText(R.string.intro_desc_phone_4));
    }

    @Test
    public void test5(){
        onView(withText(R.string.intro_title_5));
        onView(withText(R.string.intro_desc_phone_5));
    }

    @Test
    public void test6(){
        onView(withText(R.string.intro_title_6));
        onView(withText(R.string.intro_desc_6));
    }

    @After
    public void tearDown() throws Exception {
    }
}

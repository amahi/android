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
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.DEFAULT)

public class NavigationActivityTest {

    @Rule
    public ActivityTestRule<NavigationActivity> mActivityRule
        = new ActivityTestRule<>(NavigationActivity.class);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test1() {
        onView(withId(R.id.view_stub_tv_loading));
    }

    @Test
    public void test2() {
        onView(withId(R.id.tv_loading));
    }

    @Test
    public void test3() {
        onView(withId(R.id.container_content));
    }

    @Test
    public void test4() {
        onView(withId(R.id.container_navigation));
    }

    @Test
    public void test5() {
        onView(withId(R.id.toolbar));
    }

    @Test
    public void test6() {
        onView(withText(R.string.title_shares));
    }

    @Test
    public void test7() {
        onView(withText(R.string.menu_navigation_open));
    }

    @Test
    public void test8() {
        onView(withId(R.id.drawer_content));
    }

    @Test
    public void test9() {
        onView(withText(R.string.application_name));
    }

    @Test
    public void test10() {
        onView(withText(R.string.title_apps));
    }

    @After
    public void tearDown() throws Exception {
    }
}

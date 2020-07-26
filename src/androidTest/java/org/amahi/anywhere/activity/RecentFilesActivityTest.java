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
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.DEFAULT)

public class RecentFilesActivityTest {

    @Rule
    public ActivityTestRule<RecentFilesActivity> mActivityRule = new ActivityTestRule<>(
        RecentFilesActivity.class
    );

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test1() {
        onView(withText(R.string.title_recent_files)).check(matches(isDisplayed()));
    }

    @Test
    public void test2() {
        onView(withId(R.id.recent_list)).check(matches(not(isDisplayed())));
    }

    @Test
    public void test3() {
        onView(withId(android.R.id.empty)).check(matches(isDisplayed()));
    }

    @Test
    public void test4() {
        onView(withId(R.id.layout_refresh)).check(matches(isDisplayed()));
    }

    @Test
    public void test5() {
        onView(withText(R.string.message_delete_file_error)).check(doesNotExist());
    }

    @Test
    public void test6() {
        onView(withText(R.string.message_offline_file_deleted)).check(doesNotExist());
    }

    @Test
    public void test7() {
        onView(withId(R.id.media_route_menu_item)).check(doesNotExist());
    }

    @After
    public void tearDown() throws Exception {
    }
}

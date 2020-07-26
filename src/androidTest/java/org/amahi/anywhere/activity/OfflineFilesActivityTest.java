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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.DEFAULT)

public class OfflineFilesActivityTest {

    @Rule
    public ActivityTestRule<OfflineFilesActivity> mActivityRule = new ActivityTestRule<>(
        OfflineFilesActivity.class
    );

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test1() {
        onView(withText(R.string.title_offline_files)).check(matches(isDisplayed()));
    }

    @Test
    public void test2() {
        onView(withId(R.id.container_files)).check(matches(isDisplayed()));
    }

    @Test
    public void test3() {
        onView(withText(R.string.message_progress_file_downloading));
    }

    @Test
    public void test4() {
        onView(withText(R.string.message_file_download_complete));
    }

    @Test
    public void test5() {
        onView(withId(R.id.parent_view));
    }

    @Test
    public void test6() {
        onView(withText(R.string.alert_delete_dialog));
    }

    @Test
    public void test7() {
        onView(withText(R.string.alert_delete_confirm));
    }

    @Test
    public void test8() {
        onView(withText(R.string.message_offline_file_deleted));
    }

    @After
    public void tearDown() throws Exception {
    }
}

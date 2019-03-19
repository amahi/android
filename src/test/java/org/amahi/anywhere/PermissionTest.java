package org.amahi.anywhere;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

import java.util.List;

/**
 * Test for checking permissions in {@link AndroidManifest}
 */
@RunWith(RobolectricTestRunner.class)
// for the test suite when  we will want to run this from the test suite
@Config(constants = BuildConfig.class, sdk = 23)
public class PermissionTest {

    @Test
    public void permissionCheck() {
        AndroidManifest androidManifest = new AndroidManifest(Fs.fileFromPath("build/intermediates/merged_manifests/debug/AndroidManifest.xml"), null, null);
        List<String> permissions = androidManifest.getUsedPermissions();

        //List of expected permissions to be present in AndroidManifest.xml
        String[] expectedPermissions = {
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.DOWNLOAD_WITHOUT_NOTIFICATION",
            "android.permission.INTERNET",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.AUTHENTICATE_ACCOUNTS",
            "android.permission.GET_ACCOUNTS",
            "android.permission.MANAGE_ACCOUNTS",
            "android.permission.USE_CREDENTIALS",
            "android.permission.WAKE_LOCK"
        };

        //Checking expected permissions one by one
        for (String permission : expectedPermissions) {
            if (!permissions.contains(permission)) {
                showError(permission);
            }
        }
    }

    /**
     * Method to display missing permission error.
     */
    private void showError(String permission) {
        Description description = new StringDescription();
        description.appendText("Expected permission ")
            .appendText(permission)
            .appendText(" is missing from AndroidManifest.xml");

        throw new AssertionError(description.toString());
    }
}



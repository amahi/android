package org.amahi.anywhere.util;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Application identifiers accessor.
 */
public class Identifier {
    private Identifier() {
    }

    public static String getUserAgent(Context context) {
        return String.format(Locale.US, Format.USER_AGENT,
            Android.getApplicationVersion(),
            Android.getVersion(),
            Android.getDeviceName(),
            Android.getDeviceScreenSize(context),
            Android.getDeviceScreenHeight(context),
            Android.getDeviceScreenWidth(context));
    }

    public static String getUserAgent(Context context, Map<String, String> fields) {
        List<String> userAgentFields = new ArrayList<>();

        userAgentFields.add(getUserAgent(context));

        for (String fieldKey : fields.keySet()) {
            String fieldValue = fields.get(fieldKey);

            String userAgentField = String.format(Format.USER_AGENT_FIELD, fieldKey, fieldValue);

            userAgentFields.add(userAgentField);
        }

        return TextUtils.join(" ", userAgentFields);
    }

    private static final class Format {
        public static final String USER_AGENT = "AmahiAnywhere/%s (Android %s; %s) Size/%.1f Resolution/%dx%d";
        public static final String USER_AGENT_FIELD = "%s/%s";

        private Format() {
        }
    }
}

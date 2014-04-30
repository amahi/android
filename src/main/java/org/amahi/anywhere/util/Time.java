package org.amahi.anywhere.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class Time
{
	private static final DateFormat rfc1123Formatter;

	static {
		rfc1123Formatter = buildRfc1123Formatter();
	}

	private static DateFormat buildRfc1123Formatter() {
		DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

		// RFC 2616, section 3.3
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

		return formatter;
	}

	private Time() {
	}

	public static Date parseRfc1123(String time) {
		try {
			return rfc1123Formatter.parse(time);
		} catch (ParseException e) {
			return null;
		}
	}
}

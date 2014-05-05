/*
 * Copyright (c) 2014 Amahi
 *
 * This file is part of Amahi.
 *
 * Amahi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Amahi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amahi. If not, see <http ://www.gnu.org/licenses/>.
 */

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

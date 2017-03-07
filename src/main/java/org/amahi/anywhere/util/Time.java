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

import java.util.Date;

/**
 * Time formats accessor.
 */
public class Time
{
	public static final class Format
	{
		private Format() {
		}

		public static final String RFC_1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
	}

	private Time() {
	}

	public static String getEpochTimeString(Date date) {
		return String.valueOf(date.getTime());
	}
}

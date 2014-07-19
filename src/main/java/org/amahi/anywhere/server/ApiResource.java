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

package org.amahi.anywhere.server;

import java.io.InputStream;

public class ApiResource
{
	private static final class Encodings
	{
		private Encodings() {
		}

		public static final String UTF_8 = "UTF-8";
	}

	private final InputStream content;
	private final String mime;
	private final String encoding;

	public ApiResource(InputStream content, String mime, String encoding) {
		this.content = content;
		this.mime = mime;
		this.encoding = encoding;
	}

	public ApiResource(InputStream content, String mime) {
		this(content, mime, Encodings.UTF_8);
	}

	public InputStream getContent() {
		return content;
	}

	public String getMime() {
		return mime;
	}

	public String getEncoding() {
		return encoding;
	}
}

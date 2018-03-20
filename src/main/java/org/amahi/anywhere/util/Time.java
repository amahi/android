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

import android.media.MediaMetadataRetriever;

import java.util.Date;
import java.util.HashMap;

/**
 * Time formats accessor.
 */
public class Time {
    private Time() {
    }

    public static String getEpochTimeString(Date date) {
        return String.valueOf(date.getTime());
    }

    public static long getDuration(String videoUrl) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(videoUrl, new HashMap<>());
        return Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    public static final class Format {
        public static final String RFC_1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

        private Format() {
        }
    }
}

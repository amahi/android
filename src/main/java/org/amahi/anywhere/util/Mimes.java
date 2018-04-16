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

import android.support.v4.util.ArrayMap;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.ServerFile;

import java.util.Map;

/**
 * MIME type guesser. Provides {@link org.amahi.anywhere.util.Mimes.Type}
 * for its {@link java.lang.String} declaration.
 */
public class Mimes {
    private static final Map<String, Integer> types;

    static {
        types = new ArrayMap<>();

        types.put("application/octet-stream", Type.UNDEFINED);

        types.put("application/gzip", Type.ARCHIVE);
        types.put("application/rar", Type.ARCHIVE);
        types.put("application/zip", Type.ARCHIVE);
        types.put("application/x-gtar", Type.ARCHIVE);
        types.put("application/x-tar", Type.ARCHIVE);
        types.put("application/x-rar-compressed", Type.ARCHIVE);

        types.put("application/ogg", Type.AUDIO);
        types.put("application/x-flac", Type.AUDIO);

        types.put("text/css", Type.CODE);
        types.put("text/xml", Type.CODE);
        types.put("application/json", Type.CODE);
        types.put("application/javascript", Type.CODE);
        types.put("application/xml", Type.CODE);

        types.put("application/pdf", Type.DOCUMENT);
        types.put("application/msword", Type.DOCUMENT);
        types.put("application/vnd.oasis.opendocument.text", Type.DOCUMENT);
        types.put("application/x-abiword", Type.DOCUMENT);
        types.put("application/x-kword", Type.DOCUMENT);
        types.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", Type.DOCUMENT);

        types.put("text/directory", Type.DIRECTORY);

        types.put("application/vnd.oasis.opendocument.graphics", Type.IMAGE);
        types.put("application/vnd.oasis.opendocument.graphics-template", Type.IMAGE);

        types.put("application/vnd.ms-powerpoint", Type.PRESENTATION);
        types.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", Type.PRESENTATION);
        types.put("application/vnd.openxmlformats-officedocument.presentationml.slideshow", Type.PRESENTATION);

        types.put("application/vnd.ms-excel", Type.SPREADSHEET);
        types.put("application/vnd.oasis.opendocument.spreadsheet", Type.SPREADSHEET);
        types.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Type.SPREADSHEET);

        types.put("application/x-quicktimeplayer", Type.VIDEO);

        types.put("application/x-subrip", Mimes.Type.SUBTITLE);
        types.put("image/vnd.dvb.subtitle", Mimes.Type.SUBTITLE);
        types.put("application/x-subtitle", Type.SUBTITLE);
    }

    public static int match(String mime) {
        int type = matchKnown(mime);

        if (type != Type.UNDEFINED) {
            return type;
        } else {
            return matchCategory(mime);
        }
    }

    private static int matchKnown(String mime) {
        Integer type = types.get(mime);

        if (type != null) {
            return type;
        } else {
            return Type.UNDEFINED;
        }
    }

    private static int matchCategory(String mime) {
        String type = mime.split("/")[0];

        if ("audio".equals(type)) {
            return Type.AUDIO;
        }

        if ("image".equals(type)) {
            return Type.IMAGE;
        }

        if ("text".equals(type)) {
            return Type.DOCUMENT;
        }

        if ("video".equals(type)) {
            return Type.VIDEO;
        }

        return Type.UNDEFINED;
    }

    public static int getFileIcon(ServerFile file) {
        switch (Mimes.match(file.getMime())) {
            case Mimes.Type.ARCHIVE:
                return R.drawable.ic_file_archive;

            case Mimes.Type.AUDIO:
                return R.drawable.ic_file_audio;

            case Mimes.Type.CODE:
                return R.drawable.ic_file_code;

            case Mimes.Type.DOCUMENT:
                return R.drawable.ic_file_text;

            case Mimes.Type.DIRECTORY:
                return R.drawable.ic_file_directory;

            case Mimes.Type.IMAGE:
                return R.drawable.ic_file_image;

            case Mimes.Type.PRESENTATION:
                return R.drawable.ic_file_presentation;

            case Mimes.Type.SPREADSHEET:
                return R.drawable.ic_file_spreadsheet;

            case Mimes.Type.VIDEO:
                return R.drawable.ic_file_video;

            default:
                return R.drawable.ic_file_generic;
        }
    }

    public static int getTVFileIcon(ServerFile file) {
        switch (Mimes.match(file.getMime())) {
            case Mimes.Type.ARCHIVE:
                return R.drawable.tv_ic_archive;

            case Mimes.Type.AUDIO:
                return R.drawable.tv_ic_audio;

            case Mimes.Type.CODE:
                return R.drawable.tv_ic_code;

            case Mimes.Type.DOCUMENT:
                return R.drawable.tv_ic_document;

            case Mimes.Type.DIRECTORY:
                return R.drawable.tv_ic_folder;

            case Mimes.Type.IMAGE:
                return R.drawable.tv_ic_images;

            case Mimes.Type.PRESENTATION:
                return R.drawable.tv_ic_presentation;

            case Mimes.Type.SPREADSHEET:
                return R.drawable.tv_ic_spreadsheet;

            case Mimes.Type.VIDEO:
                return R.drawable.tv_ic_video;

            default:
                return R.drawable.tv_ic_generic;
        }
    }

    public static final class Type {
        public static final int UNDEFINED = 0;
        public static final int ARCHIVE = 1;
        public static final int AUDIO = 2;
        public static final int CODE = 3;
        public static final int DOCUMENT = 4;
        public static final int DIRECTORY = 5;
        public static final int IMAGE = 6;
        public static final int PRESENTATION = 7;
        public static final int SPREADSHEET = 8;
        public static final int VIDEO = 9;
        public static final int SUBTITLE = 10;

        private Type() {
        }

    }
}

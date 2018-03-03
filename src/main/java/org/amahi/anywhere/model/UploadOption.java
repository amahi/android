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

package org.amahi.anywhere.model;

import android.support.annotation.IntDef;

import org.amahi.anywhere.fragment.UploadBottomSheet;

/**
 * Upload option model for display in {@link UploadBottomSheet}
 */
public class UploadOption {
    public static final int CAMERA = 1;
    public static final int FILE = 2;
    @Types
    private int type;
    private String name;
    private int icon;

    public UploadOption(@Types int type, String name, int icon) {
        this.name = name;
        this.icon = icon;
        this.type = type;
    }

    @Types
    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    @IntDef({CAMERA, FILE})
    public @interface Types {
    }
}

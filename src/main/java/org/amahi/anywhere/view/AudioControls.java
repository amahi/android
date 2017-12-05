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

package org.amahi.anywhere.view;

import android.content.Context;

/**
 * Audio Controls view. Does the same as {@link org.amahi.anywhere.view.MediaControls}
 * with a couple of modifications. Controls do not auto hide after 3 seconds.
 */
public class AudioControls extends MediaControls {
    public AudioControls(Context context) {
        super(context);
    }

    @Override
    public void hide() {
        // Do nothing
        // Done to stop auto hide of controls after 3 seconds
    }

    public void hideControls() {
        // To hide the controls manually
        super.hide();
    }
}

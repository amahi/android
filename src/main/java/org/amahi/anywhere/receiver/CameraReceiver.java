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

package org.amahi.anywhere.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.Toast;

/**
 * Camera new picture event receiver.
 */
public class CameraReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Cursor cursor = context.getContentResolver().query(intent.getData(),
				null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			String image_path = cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.DATA));
			Toast.makeText(context, "New Photo " + image_path, Toast.LENGTH_SHORT).show();
			cursor.close();
		}
	}
}

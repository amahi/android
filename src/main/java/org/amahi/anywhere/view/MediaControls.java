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

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.MediaController;

import org.amahi.anywhere.R;

public class MediaControls extends MediaController implements Animation.AnimationListener
{
	public MediaControls(Context context) {
		super(context);
	}

	@Override
	public void show(int timeout) {
		super.show(0);
	}

	public void showAnimated() {
		Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up_view);
		startAnimation(animation);

		show();
	}

	public void hideAnimated() {
		Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down_view);
		animation.setAnimationListener(this);
		startAnimation(animation);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		hide();
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
			Activity activity = (Activity) getContext();
			activity.finish();

			return true;
		}

		return super.dispatchKeyEvent(event);
	}
}

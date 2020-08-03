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

package org.amahi.anywhere.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;

import org.amahi.anywhere.R;
import org.amahi.anywhere.util.CheckTV;
import org.amahi.anywhere.util.LocaleHelper;
import org.amahi.anywhere.util.Preferences;

public class IntroductionActivity extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.intro_phone_1), getString(R.string.intro_desc_phone_1), R.drawable.ic_banner_white, ContextCompat.getColor(this, R.color.intro_1)));
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.intro_title_2), getString(R.string.intro_desc_2), R.drawable.network, ContextCompat.getColor(this, R.color.intro_2)));
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.intro_title_3), getString(R.string.intro_desc_phone_3), R.drawable.photos, Color.DKGRAY));
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.intro_title_4), getString(R.string.intro_desc_phone_4), R.drawable.music, ContextCompat.getColor(this, R.color.intro_4)));
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.intro_title_5), getString(R.string.intro_desc_phone_5), R.drawable.movies, ContextCompat.getColor(this, R.color.intro_5)));
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.intro_title_6), getString(R.string.intro_desc_6), R.drawable.tick, ContextCompat.getColor(this, R.color.intro_6)));
        setColorTransitionsEnabled(true);
        setFadeAnimation();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        launchTv();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        launchTv();
    }

    private void launchTv() {
        if(!CheckTV.isATV(this) && Preferences.getFirstRun(this)) {
            Preferences.setFirstRun(this);
            launchNavigation();
        }

        finish();
    }

    private void launchNavigation() {
        startActivity(new Intent(this, NavigationActivity.class));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}

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

package org.amahi.anywhere.tv.presenter;


import android.content.Context;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.ServerApp;

public class CardPresenter extends Presenter {
    private Context mContext;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        mContext = parent.getContext();

        ImageCardView cardView = new ImageCardView(mContext);

        cardView.setFocusable(true);

        cardView.setFocusableInTouchMode(true);

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item != null) {

            ServerApp serverApp = (ServerApp) item;

            ((ViewHolder) viewHolder).mCardView.setTitleText(serverApp.getName());

            int CARD_WIDTH = 400;
            int CARD_HEIGHT = 300;

            ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);

            Glide.with(mContext)
                    .load(serverApp.getLogoUrl())
                    .placeholder(R.drawable.ic_app_logo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(((ViewHolder) viewHolder).mCardView.getMainImageView());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        //Do Nothing
    }

    private static class ViewHolder extends Presenter.ViewHolder {

        private ImageCardView mCardView;

        ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
        }

    }
}

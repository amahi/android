package org.amahi.anywhere.presenter;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.ServerApp;

public class CardPresenter extends Presenter{
    private static final String TAG = CardPresenter.class.getSimpleName();

    private static Context mContext;
    private static int CARD_WIDTH = 400;
    private static int CARD_HEIGHT = 300;
    private static ServerApp mServerApp;

    static class ViewHolder extends Presenter.ViewHolder{
        private ImageCardView mCardView;
        private Drawable mDefaultCardImage;

        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
            mDefaultCardImage = mContext.getResources().getDrawable(R.drawable.ic_app_logo);
        }

        public void setServerApp(ServerApp serverApp){mServerApp=serverApp;}
    }

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
        ServerApp serverApp = (ServerApp)item;
        ((ViewHolder)viewHolder).setServerApp(serverApp);
        ((ViewHolder) viewHolder).mCardView.setTitleText(serverApp.getName());
        ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        Glide.with(mContext)
                .load(serverApp.getLogoUrl())
                .placeholder(R.drawable.ic_app_logo)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(((ViewHolder) viewHolder).mCardView.getMainImageView());

        Log.d(TAG,serverApp.getLogoUrl());
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }
}

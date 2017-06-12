package org.amahi.anywhere.presenter;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.ServerApp;

public class CardPresenter extends Presenter{
    private Context mContext;

    private static class ViewHolder extends Presenter.ViewHolder{

        private ImageCardView mCardView;

        ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
        }

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
        if(item!=null) {

            ServerApp serverApp = (ServerApp) item;

            ((ViewHolder) viewHolder).mCardView.setTitleText(serverApp.getName());

            int CARD_WIDTH = 400;int CARD_HEIGHT = 300;

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
}

package org.amahi.anywhere.presenter;

import android.graphics.Color;
import android.support.v17.leanback.widget.Presenter;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingsItemPresenter extends Presenter {

    private static final int SETTINGS_ITEM_WIDTH = 400;
    private static final int SETTINGS_ITEM_HEIGHT = 200;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        TextView view = new TextView(parent.getContext());

        view.setLayoutParams(new ViewGroup.LayoutParams(SETTINGS_ITEM_WIDTH,SETTINGS_ITEM_HEIGHT));

        view.setFocusable(true);

        view.setFocusableInTouchMode(true);

        view.setBackgroundColor(Color.DKGRAY);

        view.setTextColor(Color.WHITE);

        view.setGravity(Gravity.CENTER);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ((TextView) viewHolder.view).setText((String) item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }
}
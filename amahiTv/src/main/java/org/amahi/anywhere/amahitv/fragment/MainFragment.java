package org.amahi.anywhere.amahitv.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.app.OnboardingFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.primitives.Shorts;

import org.amahi.anywhere.amahitv.server.model.ServerApp;
import org.amahi.anywhere.amahitv.server.model.ServerShare;

import java.util.ArrayList;

public class MainFragment extends BrowseFragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    private ArrayObjectAdapter mRowsAdapter;
    private static final int GRID_ITEM_WIDTH = 400;
    private static final int GRID_ITEM_HEIGHT = 300;
    private ArrayList<ServerApp> serverAppsList;
    ArrayList<ServerShare> serverSharesList;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        serverAppsList = new ArrayList<>();
        serverSharesList = new ArrayList<>();
        serverAppsList = getActivity().getIntent().getParcelableArrayListExtra("INTENT_SERVER_APPS");
        serverSharesList = getActivity().getIntent().getParcelableArrayListExtra("INTENT_SERVER_SHARES");
        setupUIElements();
        loadRows(0);
    }

    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(R.drawable.videos_by_google_banner));
        setTitle("Amahi TV"); // Badge, when set, takes precedent
        //getTitleView().setBackgroundColor(getResources().getColor(R.color.intro1));
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        //getView().setBackgroundColor(Color.parseColor("#FFF"));

        // set fastLane (or headers) background color
        setBrandColor(Color.parseColor("#0277bd"));
        // set search icon color
        setSearchAffordanceColor(Color.GREEN);
    }

    private void loadRows(int index) {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        /* GridItemPresenter */
        HeaderItem server = new HeaderItem(index, "Server");
        HeaderItem shares = new HeaderItem(index, "Shares");
        HeaderItem apps = new HeaderItem(index, "Apps");
        HeaderItem settings = new HeaderItem(index,"Preferences");

        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add("octacode");
        gridRowAdapter.add("Kumar Shashwat");
        mRowsAdapter.add(new ListRow(server, gridRowAdapter));

        /* set */
        setAdapter(mRowsAdapter);
        gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        if(serverSharesList!=null)
            for(int i=0;i<serverSharesList.size();i++) gridRowAdapter.add(i,serverSharesList.get(i).getName());
        else
            gridRowAdapter.add(0,"No shares present");

        mRowsAdapter.add(new ListRow(shares, gridRowAdapter));
        gridRowAdapter = new ArrayObjectAdapter(new SettingsItemPresenter());

        if(serverAppsList!=null)
            for(int i=0;i<serverAppsList.size();i++)
                gridRowAdapter.add(i,serverAppsList.get(i).getName());
        else
            gridRowAdapter.add(0,"No apps present");

        mRowsAdapter.add(new ListRow(apps,gridRowAdapter));
        SettingsItemPresenter settingsItemPresenter = new SettingsItemPresenter();
        gridRowAdapter = new ArrayObjectAdapter(settingsItemPresenter);
        gridRowAdapter.add("Sign out");
        gridRowAdapter.add("Connection");
        gridRowAdapter.add("Select Theme");
        mRowsAdapter.add(new ListRow(settings,gridRowAdapter));
    }

    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(Color.DKGRAY);
            view.setTextColor(Color.WHITE);
            view.setTextSize(20);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            final TextView tv = (TextView) viewHolder.view;
            tv.setText((String) item);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(),"You've clicked on : "+tv.getText(),Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

    private class SettingsItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(400,200));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(Color.DKGRAY);
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            final TextView tv = (TextView) viewHolder.view;
            tv.setText((String) item);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(),"You've clicked on : "+tv.getText(),Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

    public final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            if(item=="Sign out"){
                //startActivity(new Intent(getActivity(),SignOutStepActivity.class));
            }
        }
    }
}
package org.amahi.anywhere.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;

import org.amahi.anywhere.presenter.CardPresenter;
import org.amahi.anywhere.presenter.GridItemPresenter;
import org.amahi.anywhere.presenter.SettingsItemPresenter;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerApp;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.ArrayList;

public class MainTVFragment extends BrowseFragment {
    private static final String TAG = MainTVFragment.class.getSimpleName();

    private ArrayObjectAdapter mRowsAdapter;
    private ArrayList<ServerApp> serverAppArrayList;
    private ArrayList<ServerShare> serverShareArrayList;
    private ArrayList<Server> serverArrayList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        serverAppArrayList = getActivity().getIntent().getParcelableArrayListExtra("INTENT_APPS");
        serverShareArrayList = getActivity().getIntent().getParcelableArrayListExtra("INTENT_SHARES");
        serverArrayList = getActivity().getIntent().getParcelableArrayListExtra("INTENT_SERVERS");
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

        if(serverArrayList!=null)
            for(int i=0;i<serverArrayList.size();i++) gridRowAdapter.add(serverArrayList.get(i).getName());
        else gridRowAdapter.add(0,"No servers available");

        mRowsAdapter.add(new ListRow(server, gridRowAdapter));
        setAdapter(mRowsAdapter);
        gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);

        if(serverShareArrayList!=null)
            for(int i=0;i<serverShareArrayList.size();i++) gridRowAdapter.add(i,serverShareArrayList.get(i).getName());
        else gridRowAdapter.add(0,"No shares present");

        mRowsAdapter.add(new ListRow(shares, gridRowAdapter));
        gridRowAdapter = new ArrayObjectAdapter(new CardPresenter());

        if(serverAppArrayList!=null)
            for(int i=0;i<serverAppArrayList.size();i++) gridRowAdapter.add(i,serverAppArrayList.get(i));
        else gridRowAdapter.add(0,"No Apps present");

        mRowsAdapter.add(new ListRow(apps,gridRowAdapter));

        SettingsItemPresenter settingsItemPresenter = new SettingsItemPresenter();
        gridRowAdapter = new ArrayObjectAdapter(settingsItemPresenter);
        gridRowAdapter.add("Sign out");
        gridRowAdapter.add("Connection");
        gridRowAdapter.add("Select Theme");
        mRowsAdapter.add(new ListRow(settings,gridRowAdapter));
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
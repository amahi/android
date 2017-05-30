package org.amahi.anywhere.amahitv.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.amahi.anywhere.amahitv.R;

import java.util.Arrays;
import java.util.List;

/**
 * Navigation Drawer adapter. Visualizes predefined values
 * for the {@link org.amahi.anywhere.amahitv.fragment.NavigationFragment}.
 */

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.NavigationDrawerViewHolder> {

    public static final class NavigationItems
    {
        private NavigationItems() {
        }

        public static final int SHARES = 0;
        public static final int APPS = 1;
    }

    private final List<Integer> navigationItems;
    private Context mContext;

    public static NavigationDrawerAdapter newLocalAdapter(Context context) {
        return new NavigationDrawerAdapter(context, Arrays.asList(NavigationDrawerAdapter.NavigationItems.SHARES, NavigationDrawerAdapter.NavigationItems.APPS));
    }

    public static NavigationDrawerAdapter newRemoteAdapter(Context context) {
        return new NavigationDrawerAdapter(context, Arrays.asList(NavigationDrawerAdapter.NavigationItems.SHARES));
    }

    public NavigationDrawerAdapter(Context context, List<Integer> navigationItems){
        this.navigationItems = navigationItems;
        mContext = context;
    }

    class NavigationDrawerViewHolder extends RecyclerView.ViewHolder{
        TextView titleShare;
        NavigationDrawerViewHolder(View itemView) {
            super(itemView);
            titleShare = (TextView)itemView.findViewById(R.id.text_share_title);
        }
    }

    @Override
    public NavigationDrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NavigationDrawerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_navigation_item,parent,false));
    }

    @Override
    public void onBindViewHolder(NavigationDrawerViewHolder holder, int position) {
        holder.titleShare.setText(getNavigationName(mContext,position));
    }

    @Override
    public int getItemCount() {
        return navigationItems.size();
    }

    private String getNavigationName(Context context, int navigationItem) {
        switch (navigationItem) {
            case NavigationDrawerAdapter.NavigationItems.SHARES:
                return context.getString(R.string.title_shares);

            case NavigationDrawerAdapter.NavigationItems.APPS:
                return context.getString(R.string.title_apps);

            default:
                return null;
        }
    }

}

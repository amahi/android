package org.amahi.anywhere.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

import org.amahi.anywhere.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Navigation Drawer adapter. Visualizes predefined values
 * for the {@link org.amahi.anywhere.fragment.NavigationFragment}.
 */

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.NavigationDrawerViewHolder> {

    //for selected server options
    private final List<Integer> mNavigationItems;
    //for server list
    private final ArrayList<String> mServerName;
    private Context mContext;

    //for selected server options
    private NavigationDrawerAdapter(Context context, List<Integer> navigationItems) {
        this.mNavigationItems = navigationItems;
        this.mServerName = null;
        mContext = context;
    }

    //For Server Name List With Alphabets Bubbles
    public NavigationDrawerAdapter(Context context, ArrayList<String> serverName) {
        this.mServerName = serverName;
        mNavigationItems = null;
        mContext = context;
    }

    public static NavigationDrawerAdapter newLocalAdapter(Context context) {
        return new NavigationDrawerAdapter(context, Arrays.asList(NavigationDrawerAdapter.NavigationItems.SHARES, NavigationDrawerAdapter.NavigationItems.APPS));
    }

    public static NavigationDrawerAdapter newRemoteAdapter(Context context) {
        return new NavigationDrawerAdapter(context, Arrays.asList(NavigationDrawerAdapter.NavigationItems.SHARES));
    }

    @Override
    public NavigationDrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NavigationDrawerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_navigation_item, parent, false));
    }

    private Drawable getServerBubble(ArrayList<String> serverList, int position) {
        //get first letter of each String item
        String firstLetter = String.valueOf(serverList.get(position).charAt(0));

        //generate color bubble based on Server Name

        return TextDrawable.builder()
            .buildRound(firstLetter, mContext.getResources().getColor(R.color.primary));
    }

    @Override
    public void onBindViewHolder(NavigationDrawerViewHolder holder, int position) {
        if (mServerName != null) {
            setSelectedServerOptions(holder, position);
            return;
        }
        setServerList(holder, position);
    }

    private void setSelectedServerOptions(NavigationDrawerViewHolder holder, int position) {
        getServerBubble(holder).setImageDrawable(getServerBubble(mServerName, position));
        getTitleShare(holder).setText(mServerName.get(position));
    }

    private void setServerList(NavigationDrawerViewHolder holder, int position) {
        String name;
        int imageId;
        switch (position) {
            case NavigationDrawerAdapter.NavigationItems.SHARES:
                name = mContext.getString(R.string.title_shares);
                imageId = R.drawable.ic_shares_white;
                break;

            case NavigationDrawerAdapter.NavigationItems.APPS:
                name = mContext.getString(R.string.title_apps);
                imageId = R.drawable.ic_apps_white;
                break;

            default:
                return;
        }

        getTitleShare(holder).setText(name);
        getServerBubble(holder).setImageResource(imageId);
    }

    private ImageView getServerBubble(NavigationDrawerViewHolder holder) {
        return holder.server_bubble;
    }

    private TextView getTitleShare(NavigationDrawerViewHolder holder) {
        return holder.titleShare;
    }

    @Override
    public int getItemCount() {
        return mNavigationItems != null ? mNavigationItems.size() : mServerName.size();
    }

    public static final class NavigationItems {
        public static final int SHARES = 0;
        public static final int APPS = 1;

        private NavigationItems() {
        }
    }

    class NavigationDrawerViewHolder extends RecyclerView.ViewHolder {
        TextView titleShare;
        ImageView server_bubble;

        NavigationDrawerViewHolder(View itemView) {
            super(itemView);
            titleShare = itemView.findViewById(R.id.nav_name);
            server_bubble = itemView.findViewById(R.id.nav_icon);
        }
    }

}

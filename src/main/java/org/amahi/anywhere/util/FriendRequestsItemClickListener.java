package org.amahi.anywhere.util;

import android.view.View;

/**
 * FriendRequestsItemClickListener interface.
 * Handles onItemClick and onMoreOptionClick for FriendRequestsListAdapter
 */

public interface FriendRequestsItemClickListener {

    void onItemClick(View view, int position);

    void onMoreOptionClick(View view, int position);
}

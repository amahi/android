package org.amahi.anywhere.util;

import android.view.View;

/**
 * FriendsItemClickListener interface.
 * Handles onItemClick and onMoreOptionClick for FriendsListAdapter
 */

public interface FriendsItemClickListener {

    void onItemClick(View view, int position);

    void onMoreOptionClick(View view, int position);

}

package org.amahi.anywhere.util;

import android.view.View;

/**
 * RecyclerViewItemClickListener interface.
 * Handles onItemClick and onLongItemClick for RecyclerView
 */

public interface RecyclerViewItemClickListener {

    void onItemClick(View view, int position);

    boolean onLongItemClick(View view, int position);
}

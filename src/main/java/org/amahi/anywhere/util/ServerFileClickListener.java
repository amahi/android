package org.amahi.anywhere.util;

import android.view.View;

/**
 * ServerFileClickListener interface.
 * Handles onItemClick and onMoreOptionClick for ServerFileAdapter
 */

public interface ServerFileClickListener {

    void onItemClick(View view, int position);

    void onMoreOptionClick(View view, int position);
}

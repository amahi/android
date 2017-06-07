package org.amahi.anywhere.view;

/**
 * Created by pulkit on 30/11/16.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.amahi.anywhere.R;

public class PercentageView {

    private View view;
    private ProgressBar progressBar;
    private TextView valuePercent, valueName;

    public PercentageView(Context context) {
        ViewGroup layout = (ViewGroup) ((Activity) context).findViewById(android.R.id.content).getRootView();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.percentage_view, layout, false);
        view.getBackground().setAlpha(100);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        valueName = (TextView) view.findViewById(R.id.value_name);
        valuePercent = (TextView) view.findViewById(R.id.value_percent);
        view.requestLayout();
    }

    public void setProgress(int n) {
        progressBar.setProgress(n);
    }


    public boolean isVisible() {
        return view.getVisibility() == View.VISIBLE;
    }

    public void setTitle(String s) {
        valueName.setText(s);
    }

    public void setProgressText(String s) {
        valuePercent.setText(s);
    }
}


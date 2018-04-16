package org.amahi.anywhere.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.amahi.anywhere.R;


public class SeekView {


    private View view;
    private TextView textView;

    public SeekView(ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.seek_view, viewGroup, false);
        view.requestLayout();
        textView = view.findViewById(R.id.seek_value);
    }

    public View getView() {
        return view;
    }

    public void setText(String s) {
        textView.setText(s);
    }

    public void hide() {
        view.setVisibility(View.GONE);
    }

    public void show() {
        view.setVisibility(View.VISIBLE);
    }

    public boolean isShowing() {
        return view.getVisibility() == View.VISIBLE;
    }
}

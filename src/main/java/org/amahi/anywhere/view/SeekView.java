package org.amahi.anywhere.view;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.amahi.anywhere.R;


public class SeekView {

    private View view;
    private TextView textView;

    public <T extends Activity> SeekView(T activity) {
        ViewGroup layout = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.seek_view, null);
        view.getBackground().setAlpha(100);
        textView = (TextView) view.findViewById(R.id.seek_value);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        RelativeLayout relativeLayout = new RelativeLayout(activity);
        relativeLayout.setGravity(Gravity.CENTER);
        relativeLayout.addView(view);
        layout.addView(relativeLayout, params);
        view.requestLayout();
        view.setVisibility(View.INVISIBLE);
    }

    public void show() {
        view.setVisibility(View.VISIBLE);
    }

    public void hide() {
        view.setVisibility(View.INVISIBLE);

    }

    public void setText(String s) {
        textView.setText(s);
    }

    public boolean isVisible() {
        return view.getVisibility() == View.VISIBLE;
    }
}

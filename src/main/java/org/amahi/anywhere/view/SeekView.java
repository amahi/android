package org.amahi.anywhere.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.amahi.anywhere.R;


public class SeekView {


    private View view;
    private TextView textView;

    public <T extends Activity> SeekView(ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.seek_view, viewGroup, false);
        view.getBackground().setAlpha(100);
        view.requestLayout();
        textView = (TextView) view.findViewById(R.id.seek_value);
    }

    public View getView() {
        return view;
    }

    public void setText(String s) {
        textView.setText(s);
    }
}

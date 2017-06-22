package org.amahi.anywhere.tv.presenter;


import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v17.leanback.widget.Presenter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.tv.fragment.ServerFileTvFragment;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Mimes;

import java.util.List;

public class ServerFileTvPresenter extends Presenter {
    private FragmentManager mFragmentManager;
    private List<ServerFile> mServerFileList;

    public ServerFileTvPresenter(FragmentManager fragmentManager, List<ServerFile> serverFileList) {
        mFragmentManager = fragmentManager;
        mServerFileList = serverFileList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        TextView view = new TextView(parent.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(400, 300));
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setBackgroundColor(Color.DKGRAY);
        view.setTextColor(Color.WHITE);
        view.setTextSize(20);
        view.setGravity(Gravity.CENTER);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        TextView textView = (TextView) viewHolder.view;
        final ServerFile serverFile = (ServerFile) item;
        textView.setText(serverFile.getName());
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDirectory(serverFile)) {
                    setFragment(serverFile, serverFile.getParentShare());
                } else {
                    startFileOpening(serverFile);
                }
            }
        });
    }

    private void setFragment(ServerFile serverFile, ServerShare serverShare) {
        mFragmentManager.beginTransaction().replace(R.id.server_file_tv_container, buildTvFragment(serverFile, serverShare), getClass().getSimpleName()).addToBackStack(getClass().getSimpleName()).commit();
    }

    private Fragment buildTvFragment(ServerFile serverFile, ServerShare serverShare) {
        Fragment fragment = new ServerFileTvFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Intents.Extras.SERVER_FILE, serverFile);
        bundle.putParcelable(Intents.Extras.SERVER_SHARE, serverShare);
        fragment.setArguments(bundle);
        return fragment;
    }

    private void startFileOpening(ServerFile file) {
        BusProvider.getBus().post(new FileOpeningEvent(file.getParentShare(), mServerFileList, file));
    }

    private boolean isDirectory(ServerFile file) {
        return Mimes.match(file.getMime()) == Mimes.Type.DIRECTORY;
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }
}
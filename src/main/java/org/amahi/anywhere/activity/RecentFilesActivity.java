package org.amahi.anywhere.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.RecentFilesAdapter;
import org.amahi.anywhere.db.entities.RecentFile;
import org.amahi.anywhere.db.repositories.RecentFileRepository;
import org.amahi.anywhere.util.ServerFileClickListener;

import java.util.List;

public class RecentFilesActivity extends AppCompatActivity implements
    ServerFileClickListener,
    SwipeRefreshLayout.OnRefreshListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_files);

        setUpHomeNavigation();
        setUpRecentFileList();
        setUpFilesContentRefreshing();
    }

    private void setUpHomeNavigation() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        setUpFilesTitle();
    }

    private void setUpFilesTitle() {
        getSupportActionBar().setTitle(R.string.title_recent_files);
    }

    private void setUpRecentFileList() {
        getRecentFileRView().setLayoutManager(new LinearLayoutManager(this));
        addListItemDivider();
        setUpListAdapter();
    }

    private RecyclerView getRecentFileRView() {
        return findViewById(R.id.recent_list);
    }

    private void addListItemDivider() {
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
            getRecentFileRView().getContext(),
            DividerItemDecoration.VERTICAL);

        getRecentFileRView().addItemDecoration(dividerItemDecoration);
    }

    private void setUpListAdapter() {
        List<RecentFile> recentFiles = getRecentFilesList();
        if (!recentFiles.isEmpty()) {
            getRecentFileRView().setAdapter(new RecentFilesAdapter(this, recentFiles));
            showList(true);
        } else {
            showList(false);
        }
    }

    private List<RecentFile> getRecentFilesList() {
        RecentFileRepository repository = new RecentFileRepository(this);
        return repository.getAllRecentFiles();
    }

    private void showList(boolean notEmpty) {
        if (notEmpty) {
            getRecentFileRView().setVisibility(View.VISIBLE);
            getEmptyView().setVisibility(View.GONE);
        } else {
            getRecentFileRView().setVisibility(View.GONE);
            getEmptyView().setVisibility(View.VISIBLE);
        }
    }

    private LinearLayout getEmptyView() {
        return findViewById(android.R.id.empty);
    }

    private void setUpFilesContentRefreshing() {
        SwipeRefreshLayout refreshLayout = getRefreshLayout();

        refreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_green_light,
            android.R.color.holo_red_light);

        refreshLayout.setOnRefreshListener(this);
    }

    private SwipeRefreshLayout getRefreshLayout() {
        return findViewById(R.id.layout_refresh);
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onMoreOptionClick(View view, int position) {

    }

    @Override
    public void onRefresh() {
        setUpListAdapter();
        getRefreshLayout().setRefreshing(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }
}

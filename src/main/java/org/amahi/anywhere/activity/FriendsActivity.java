package org.amahi.anywhere.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.FriendsPagerAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FriendRequestsLoadedEvent;
import org.amahi.anywhere.bus.FriendUsersLoadedEvent;
import org.amahi.anywhere.fragment.FriendRequestsFragment;
import org.amahi.anywhere.fragment.FriendsFragment;
import org.amahi.anywhere.server.api.ServerApi;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.FriendRequestResponse;
import org.amahi.anywhere.server.model.FriendUserResponse;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FriendsActivity extends AppCompatActivity {


    @Inject
    ServerClient serverClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        setUpHomeNavigation();

        setUpInjections();

        setUpPager();

        getTabLayout().setupWithViewPager(getPager());

        loadData();
    }

    private void loadData() {

        Retrofit friendUsers = new Retrofit.Builder()
            .baseUrl("https://friending-testing.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

         ServerApi frndUserserverApi = friendUsers.create(ServerApi.class);

        Call<FriendUserResponse> frndUsercall = frndUserserverApi.getFriendUsers("abcdef");


        frndUsercall.enqueue(new Callback<FriendUserResponse>() {
            @Override
            public void onResponse(Call<FriendUserResponse> call, Response<FriendUserResponse> response) {
              if(!response.isSuccessful()){
                  Toast.makeText(FriendsActivity.this, "fail", Toast.LENGTH_SHORT).show();
                  return;
              }
                FriendUserResponse friendUserResponse = response.body();
                BusProvider.getBus().post(new FriendUsersLoadedEvent(friendUserResponse.getFriendUsers()));
            }

            @Override
            public void onFailure(Call<FriendUserResponse> call, Throwable t) {

            }
        });



        Retrofit friendRequests = new Retrofit.Builder()
            .baseUrl("https://friending-testing.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        ServerApi frndRequestServerapi = friendRequests.create(ServerApi.class);

        Call<FriendRequestResponse> frndRequestcall = frndRequestServerapi.getFriendRequests("abcdef");


       frndRequestcall.enqueue(new Callback<FriendRequestResponse>() {
           @Override
           public void onResponse(Call<FriendRequestResponse> call, Response<FriendRequestResponse> response) {
               if(!response.isSuccessful()){
                   Toast.makeText(FriendsActivity.this, "fail", Toast.LENGTH_SHORT).show();
                   return;
               }

               FriendRequestResponse friendRequestResponse = response.body();
               BusProvider.getBus().post(new FriendRequestsLoadedEvent(friendRequestResponse.getFriendRequests()));
           }

           @Override
           public void onFailure(Call<FriendRequestResponse> call, Throwable t) {

           }
       });

        }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpHomeNavigation() {
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpPager() {

        List<Fragment> fragmentList = getFragments();

        List<String> fragmentTitles = getFragmentTitles();

        FriendsPagerAdapter adapter = new FriendsPagerAdapter(getSupportFragmentManager(), fragmentList, fragmentTitles);
        getPager().setAdapter(adapter);
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

    private List<Fragment> getFragments() {

        Fragment friendsFragment = new FriendsFragment();
        Fragment friendRequestsFragment = new FriendRequestsFragment();
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(friendsFragment);
        fragmentList.add(friendRequestsFragment);

        return fragmentList;
    }

    private List<String> getFragmentTitles() {

        List<String> fragmentTitles = new ArrayList<>();
        fragmentTitles.add("Friends");
        fragmentTitles.add("Friend Requests");

        return fragmentTitles;
    }

    private ViewPager getPager() {
        return findViewById(R.id.pager_friends);
    }

    private TabLayout getTabLayout() {
        return findViewById(R.id.tablayout_friends);
    }


    @Override
    public void onResume() {
        super.onResume();

        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        BusProvider.getBus().unregister(this);
    }

}

/*
 * Copyright (c) 2014 Amahi
 *
 * This file is part of Amahi.
 *
 * Amahi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Amahi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amahi. If not, see <http ://www.gnu.org/licenses/>.
 */

package org.amahi.anywhere.tv.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.util.Log;
import android.widget.Toast;

import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.activity.NavigationActivity;

import java.util.Arrays;
import java.util.List;

public class SignOutFragment extends GuidedStepFragment implements AccountManagerCallback<Boolean> {

    private static final int ACTION_CONTINUE = 0;
    private static final int ACTION_BACK = 1;

    private Context mContext;

    public SignOutFragment(){}

    @SuppressLint("ValidFragment")
    public SignOutFragment(Context context){mContext = context;}

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getString(R.string.pref_title_account);

        String breadcrumb = getString(R.string.pref_title_sign_out);

        String description = getString(R.string.pref_sign_out_desc);

        Drawable icon = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            icon = getActivity().getDrawable(R.drawable.ic_app_logo);
        }

        return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
    }

    @Override
    public void onCreateActions(@NonNull List actions, Bundle savedInstanceState) {

        addAction(actions, ACTION_CONTINUE, getString(R.string.pref_title_sign_out), "");

        addAction(actions, ACTION_BACK, getString(R.string.pref_option_go_back), "");
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {

        switch ((int) action.getId()){
            case ACTION_CONTINUE:
                tearDownAccount();
                break;

            case ACTION_BACK:
                getActivity().finish();
                break;

            default:
                Log.w(getClass().getSimpleName(), getString(R.string.pref_action_not_defined));
                break;
        }
    }

    private void addAction(List actions, long id, String title, String desc) {
        actions.add(new GuidedAction.Builder(mContext)
                .id(id)
                .title(title)
                .description(desc)
                .build());
    }

    private void tearDownAccount() {
        if (!getAccounts().isEmpty()) {
            Account account = getAccounts().get(0);

            getAccountManager().removeAccount(account, this, null);
        } else {
            tearDownActivity();
        }
    }

    private List<Account> getAccounts() {
        return Arrays.asList(getAccountManager().getAccountsByType(AmahiAccount.TYPE));
    }

    private AccountManager getAccountManager() {
        return AccountManager.get(getActivity());
    }

    private void tearDownActivity() {
        Toast.makeText(getActivity(), R.string.message_logout, Toast.LENGTH_SHORT).show();

        Intent myIntent = new Intent(getActivity().getApplicationContext(), NavigationActivity.class);

        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(myIntent);

        getActivity().finish();
    }

    @Override
    public void run(AccountManagerFuture<Boolean> accountManagerFuture) {
        tearDownActivity();
    }
}

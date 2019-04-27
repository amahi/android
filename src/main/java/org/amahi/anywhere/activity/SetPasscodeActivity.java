package org.amahi.anywhere.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;

import java.util.Arrays;
import java.util.List;

public class SetPasscodeActivity extends AppCompatActivity implements AccountManagerCallback<Boolean> {

    private PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;
    private TextView ProfileName;
    private TextView sign_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);

        Intent intent = getIntent();
        String text = intent.getStringExtra("text");

        sign_out = (TextView) findViewById(R.id.sign_out);
        ProfileName = (TextView) findViewById(R.id.profile_name);
        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);

        if (text == null)
            ProfileName.setText(getString(R.string.passcode_enter_new));
        else
            ProfileName.setText(getString(R.string.passcode_reenter));

        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLength(4);

        setUpPasscode();

        setUpSignout();

    }

    private void setUpPasscode() {
        mPinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                final SharedPreferences.Editor editor = pref.edit();
                Intent intent1 = getIntent();
                String pin1 = intent1.getStringExtra("text");
                if (pin1 == null) {
                    Intent intent = new Intent(SetPasscodeActivity.this, SetPasscodeActivity.class);
                    intent.putExtra("text", pin);
                    startActivity(intent);
                } else {
                    if (pin.equals(pin1)) {
                        editor.putString("code", pin);
                        editor.apply();

                        Toast.makeText(SetPasscodeActivity.this, getString(R.string.passcode_set), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SetPasscodeActivity.this, SettingsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else
                        Toast.makeText(SetPasscodeActivity.this, getString(R.string.passcode_invalid), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onEmpty() {
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        openSettingsActivity();
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(SetPasscodeActivity.this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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

    private void setUpSignout() {
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConfirmationDialog();
            }
        });
    }

    private void setConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.sign_out_title))
            .setMessage(getString(R.string.sign_out_message))
            .setPositiveButton(getString(R.string.sign_out_title), (dialog, which) -> tearDownAccount())
            .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss()).show();
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
        return AccountManager.get(this);
    }

    private void tearDownActivity() {
        Toast.makeText(this, R.string.message_logout, Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(this.getApplicationContext(), NavigationActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(myIntent);
        this.finish();
    }

    @Override
    public void run(AccountManagerFuture<Boolean> accountManagerFuture) {
        tearDownActivity();
    }
}

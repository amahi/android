package org.amahi.anywhere;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.account.AccountAuthenticatorAppCompatActivity;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.bus.AuthenticationConnectionFailedEvent;
import org.amahi.anywhere.bus.AuthenticationFailedEvent;
import org.amahi.anywhere.bus.AuthenticationSucceedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.util.LocaleHelper;
import org.amahi.anywhere.util.ViewDirector;

public class activity_login extends AccountAuthenticatorAppCompatActivity implements TextWatcher {

    AmahiClient amahiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AmahiApplication.getInstance().isLightThemeEnabled()) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUpInjections();

        setUpAuthentication();

    }


    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpAuthentication() {
        setUpAuthenticationMessages();
        setUpAuthenticationListeners();
    }

    private String getUsername() {
        return getUsernameEdit().getText().toString();
    }

    private EditText getUsernameEdit() {
        TextInputLayout username_layout = findViewById(R.id.username_layout);
        return username_layout.getEditText();
    }

    private String getPassword() {
        return getPasswordEdit().getText().toString();
    }

    private EditText getPasswordEdit() {
        TextInputLayout password_layout = findViewById(R.id.password_layout);
        return password_layout.getEditText();
    }

    private ActionProcessButton getAuthenticationButton() {
        return findViewById(R.id.button_authentication);
    }

    private void setUpAuthenticationMessages() {
        TextView forgotPassword = findViewById(R.id.text_forgot_password);
        TextView authenticationConnectionFailureMessage = findViewById(R.id.text_message_authentication_connection);
        forgotPassword.setMovementMethod(LinkMovementMethod.getInstance());
        authenticationConnectionFailureMessage.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setUpAuthenticationListeners() {
        setUpAuthenticationTextListener();
        setUpAuthenticationActionListener();
    }

    private void setUpAuthenticationTextListener() {
        getUsernameEdit().addTextChangedListener(this);
        getPasswordEdit().addTextChangedListener(this);
        getPasswordEdit().setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_GO) {
                onClick(getAuthenticationButton());
                handled = true;
            }
            return handled;
        });
    }

    @Override
    public void onTextChanged(CharSequence text, int after, int before, int count) {
        hideAuthenticationFailureMessage();
    }

    private void hideAuthenticationFailureMessage() {
        ViewDirector.of(this, R.id.animator_message).show(R.id.view_message_empty);
    }

    @Override
    public void afterTextChanged(Editable text) {
    }

    @Override
    public void beforeTextChanged(CharSequence text, int start, int count, int before) {
    }

    private void setUpAuthenticationActionListener() {
        getAuthenticationButton().setOnClickListener(this::onClick);
    }

    public void onClick(View view) {
        if (getUsername().trim().isEmpty() || getPassword().trim().isEmpty()) {
            ViewDirector.of(this, R.id.animator_message).show(R.id.text_message_authentication_empty);

            if (getUsername().trim().isEmpty())
                getUsernameEdit().requestFocus();

            if (getPassword().trim().isEmpty())
                getPasswordEdit().requestFocus();

            if (getUsername().trim().isEmpty() && getPassword().trim().isEmpty())
                getUsernameEdit().requestFocus();

        } else {
            startAuthentication();

            authenticate();
        }
    }

    private void startAuthentication() {
        hideAuthenticationText();

        showProgress();

        hideAuthenticationFailureMessage();
    }

    private void hideAuthenticationText() {
        getUsernameEdit().setEnabled(false);
        getPasswordEdit().setEnabled(false);
    }

    private void showProgress() {
        ActionProcessButton authenticationButton = getAuthenticationButton();

        authenticationButton.setMode(ActionProcessButton.Mode.ENDLESS);
        authenticationButton.setProgress(1);
    }

    private void authenticate() {
        amahiClient.authenticate(getUsername().trim(), getPassword());
    }

    @Subscribe
    public void onAuthenticationFailed(AuthenticationFailedEvent event) {
        finishAuthentication();

        showAuthenticationFailureMessage();
    }

    private void finishAuthentication() {
        showAuthenticationText();

        hideProgress();
    }

    private void showAuthenticationText() {
        getUsernameEdit().setEnabled(true);
        getPasswordEdit().setEnabled(true);
    }

    private void hideProgress() {
        getAuthenticationButton().setProgress(0);
    }

    private void showAuthenticationFailureMessage() {
        ViewDirector.of(this, R.id.animator_message).show(R.id.text_message_authentication);
    }

    @Subscribe
    public void onAuthenticationConnectionFailed(AuthenticationConnectionFailedEvent event) {
        finishAuthentication();

        showAuthenticationConnectionFailureMessage();
    }

    private void showAuthenticationConnectionFailureMessage() {
        ViewDirector.of(this, R.id.animator_message).show(R.id.text_message_authentication_connection);
    }

    @Subscribe
    public void onAuthenticationSucceed(AuthenticationSucceedEvent event) {
        finishAuthentication(event.getAuthentication().getToken());
    }

    private void finishAuthentication(String authenticationToken) {
        AccountManager accountManager = AccountManager.get(this);

        Bundle authenticationBundle = new Bundle();

        Account account = new AmahiAccount(getUsername());

        if (accountManager.addAccountExplicitly(account, getPassword(), null)) {
            authenticationBundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            authenticationBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            authenticationBundle.putString(AccountManager.KEY_AUTHTOKEN, authenticationToken);

            accountManager.setAuthToken(account, account.type, authenticationToken);
        }

        setAccountAuthenticatorResult(authenticationBundle);

        setResult(Activity.RESULT_OK);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        BusProvider.getBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        BusProvider.getBus().unregister(this);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}


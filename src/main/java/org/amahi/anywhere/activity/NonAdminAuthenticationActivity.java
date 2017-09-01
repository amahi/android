package org.amahi.anywhere.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.NonAdminAuthConnectionFailedEvent;
import org.amahi.anywhere.bus.NonAdminAuthSucceedEvent;
import org.amahi.anywhere.bus.NonAdminAuthenticationFailedEvent;
import org.amahi.anywhere.server.client.NonAdminClient;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.NonAdminAuthentication;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Preferences;
import org.amahi.anywhere.util.ViewDirector;

import javax.inject.Inject;

public class NonAdminAuthenticationActivity extends AppCompatActivity implements TextWatcher {

    @Inject
    NonAdminClient nonAdminClient;

    @Inject
    ServerClient serverClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        setUpInjections();
        getAuthenticationButton().setText(R.string.login_as_non_admin);
        Toast.makeText(this, "   "+getIntent().getStringExtra(Intents.Extras.PUBLIC_KEY), Toast.LENGTH_SHORT).show();
        setAuthenticationListener();
        setFieldsListener();
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setAuthenticationListener() {
        getUsernameEdit().addTextChangedListener(this);
        getPasswordEdit().addTextChangedListener(this);
    }

    private void setFieldsListener() {
        getAuthenticationButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getUsername().trim().isEmpty() || getPassword().trim().isEmpty()) {
                    ViewDirector.of(NonAdminAuthenticationActivity.this, R.id.animator_message).show(R.id.text_message_authentication_empty);

                    if (getUsername().trim().isEmpty())
                        getUsernameEdit().getBackground().setColorFilter(ContextCompat.getColor(NonAdminAuthenticationActivity.this, android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
                    if (getPassword().trim().isEmpty())
                        getPasswordEdit().getBackground().setColorFilter(ContextCompat.getColor(NonAdminAuthenticationActivity.this, android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);

                    getUsernameEdit().addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (!getUsername().trim().isEmpty())
                                getUsernameEdit().getBackground().setColorFilter(ContextCompat.getColor(NonAdminAuthenticationActivity.this, R.color.blue_normal), PorterDuff.Mode.SRC_ATOP);
                            else
                                getUsernameEdit().getBackground().setColorFilter(ContextCompat.getColor(NonAdminAuthenticationActivity.this, R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });

                    getPasswordEdit().addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (!getPassword().trim().isEmpty())
                                getPasswordEdit().getBackground().setColorFilter(ContextCompat.getColor(NonAdminAuthenticationActivity.this, R.color.blue_normal), PorterDuff.Mode.SRC_ATOP);
                            else
                                getPasswordEdit().getBackground().setColorFilter(ContextCompat.getColor(NonAdminAuthenticationActivity.this, android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                }
                else {
                    startAuthentication();

                    authenticate();
                }
            }
        });
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
        nonAdminClient.authenticate(getUsername(), getPassword());
    }

    @Subscribe
    public void onNonAdminAuthSucceed(NonAdminAuthSucceedEvent event) {
        if(getRunningServerName().equals(event.getNonAdminAuthentication().getServerName())){
            Preferences.setNonAdminUserCredentials(event, this);
            finishAuthentication(event);
        }
        else {
            ViewDirector.of(NonAdminAuthenticationActivity.this, R.id.animator_message).show(R.id.text_message_server_not_authorised);
        }
    }

    @Subscribe
    public void onNonAdminAuthConnectionFailed(NonAdminAuthConnectionFailedEvent event) {
        ViewDirector.of(NonAdminAuthenticationActivity.this, R.id.animator_message).show(R.id.text_message_authentication_connection);
    }

    @Subscribe
    public void onNonAdminAuthenticationFailed(NonAdminAuthenticationFailedEvent event) {
        ViewDirector.of(NonAdminAuthenticationActivity.this, R.id.animator_message).show(R.id.text_message_authentication);
    }

    private String getRunningServerName() {
        return getIntent().getStringExtra(Intents.Extras.SERVER_NAME);
    }

    private void finishAuthentication(NonAdminAuthSucceedEvent event) {
        NonAdminAuthentication nonAdminAuthentication = event.getNonAdminAuthentication();
        Server server = new Server(
                nonAdminAuthentication.getServerName(),
                nonAdminAuthentication.getSessionToken(),
                nonAdminAuthentication.getServerAddress(),
                true);
        serverClient.connecttoNonadmin(server);
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


    private ActionProcessButton getAuthenticationButton() {
        return (ActionProcessButton) findViewById(R.id.button_authentication);
    }

    private String getUsername() {
        return getUsernameEdit().getText().toString();
    }

    private EditText getUsernameEdit() {
        TextInputLayout username_layout = (TextInputLayout) findViewById(R.id.username_layout);
        return username_layout.getEditText();
    }

    private String getPassword() {
        return getPasswordEdit().getText().toString();
    }

    private EditText getPasswordEdit() {
        TextInputLayout password_layout = (TextInputLayout) findViewById(R.id.password_layout);
        return password_layout.getEditText();
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
}

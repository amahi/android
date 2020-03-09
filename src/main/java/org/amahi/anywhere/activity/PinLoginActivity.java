package org.amahi.anywhere.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.material.textfield.TextInputLayout;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.util.ViewDirector;

import javax.inject.Inject;

public class PinLoginActivity extends AppCompatActivity implements TextWatcher {
    @Inject
    AmahiClient amahiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_login);
        getSupportActionBar().hide();
        setUpPinAuth();
    }

    private void setUpPinAuth() {
        setUpAuthMessages();
        setUpAuthListeners();
    }

    private void setUpAuthListeners() {
        setUpAuthenticationTextListener();
        setUpAuthenticationActionListener();
    }

    private void setUpAuthenticationActionListener() {
        getAuthPinButton().setOnClickListener(this::onClick);
        getLoginAccountText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PinLoginActivity.this, AuthenticationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setUpAuthenticationTextListener() {
        getPinEdit().addTextChangedListener(this);
        getPinEdit().setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_GO) {
                onClick(getAuthPinButton());
                handled = true;
            }
            return handled;
        });

    }

    private void setUpAuthMessages() {
        TextView authenticationConnectionFailureMessage = findViewById(R.id.text_message_authentication_connection);
        authenticationConnectionFailureMessage.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private ActionProcessButton getAuthPinButton() {
        return findViewById(R.id.button_pin_access);
    }

    private TextView getLoginAccountText() {
        return findViewById(R.id.text_login_account);
    }

    private EditText getPinEdit() {
        TextInputLayout pin_layout = findViewById(R.id.pin_code_layout);
        return pin_layout.getEditText();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public void onClick(View view) {
        if (getPinCode().trim().isEmpty()) {
            ViewDirector.of(this, R.id.animator_message).show(R.id.text_message_authentication_empty);

            if (getPinCode().trim().isEmpty())
                getPinEdit().requestFocus();

        } else {
            startAuthentication();
            authenticate();
        }
    }

    private void authenticate() {
        Toast.makeText(this, "AUTH", Toast.LENGTH_SHORT).show();
        amahiClient.authenticate(getPinCode());
    }

    private void startAuthentication() {
        hideAuthenticationText();
        showProgress();
        hideAuthenticationFailureMessage();
    }

    private String getPinCode() {
        return getPinEdit().getText().toString();
    }

    private void hideAuthenticationText() {
        getPinEdit().setEnabled(false);
    }

    private void showProgress() {
        ActionProcessButton authenticationButton = getAuthPinButton();
        authenticationButton.setMode(ActionProcessButton.Mode.ENDLESS);
        authenticationButton.setProgress(1);
    }

    private void hideAuthenticationFailureMessage() {
        ViewDirector.of(this, R.id.animator_message).show(R.id.view_message_empty);
    }
}

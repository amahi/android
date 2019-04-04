package org.amahi.anywhere.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import org.amahi.anywhere.R;
import org.amahi.anywhere.activity.NavigationActivity;


/**
 * Created by whit3hawks on 11/16/16.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintCheck extends FingerprintManager.AuthenticationCallback {


    private Context context;


    // Constructor
    public FingerprintCheck(Context mContext) {
        context = mContext;
    }


    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        CancellationSignal cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }


    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        this.update("Fingerprint Authentication error\n" + errString, false);
    }


    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        this.update("Fingerprint Authentication help\n" + helpString, false);
    }


    @Override
    public void onAuthenticationFailed() {
        this.update("Fingerprint Authentication failed.", false);
    }


    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        Intent intent = new Intent(context, NavigationActivity.class);
        context.startActivity(intent);
        this.update("Fingerprint Authentication succeeded.", true);
    }

    public void update(String e, Boolean success) {
        TextView textView = (TextView) ((Activity) context).findViewById(R.id.errorText);
        textView.setText(e);
        if (success) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.fingerprint_success));
        } else
            textView.setTextColor(ContextCompat.getColor(context, R.color.red_error));
    }

}

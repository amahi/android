package org.amahi.anywhere.activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
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
import org.amahi.anywhere.util.FingerprintHandler;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class PasscodeActivity extends AppCompatActivity implements AccountManagerCallback<Boolean> {
    private PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;
    private TextView sign_out;
    String code;

    private static final String KEY_NAME = "Key";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);

        sign_out = (TextView) findViewById(R.id.sign_out);
        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);

        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLength(4);

        setUpPasscode();

        setUpSignout();

        setUpFingerprint();

    }

    private void setUpPasscode() {
        mPinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                code = pref.getString("code", null);

                if (pin.equals(code)) {
                    Intent intent = new Intent(PasscodeActivity.this, NavigationActivity.class);
                    intent.putExtra("code", pin);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(PasscodeActivity.this, getString(R.string.passcode_failed), Toast.LENGTH_SHORT).show();
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


    private void setUpFingerprint() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager =
                (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager =
                (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            if (!fingerprintManager.isHardwareDetected()) {
                Toast.makeText(PasscodeActivity.this, getString(R.string.fingerprint_support), Toast.LENGTH_SHORT).show();
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(PasscodeActivity.this, getString(R.string.fingerprint_permission), Toast.LENGTH_SHORT).show();

            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(PasscodeActivity.this, getString(R.string.fingerprint_configured), Toast.LENGTH_SHORT).show();
            }

            if (!keyguardManager.isKeyguardSecure()) {
                Toast.makeText(PasscodeActivity.this, getString(R.string.fingerprint_security), Toast.LENGTH_SHORT).show();
            } else {
                try {
                    generateKey();
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }

                if (initCipher()) {
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    FingerprintHandler helper = new FingerprintHandler(this);
                    helper.startAuth(fingerprintManager, cryptoObject);
                }
            }
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void generateKey() throws FingerprintException {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyStore.load(null);
            keyGenerator.init(new
                KeyGenParameterSpec.Builder(KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT |
                    KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build());
            keyGenerator.generateKey();

        } catch (KeyStoreException
            | NoSuchAlgorithmException
            | NoSuchProviderException
            | InvalidAlgorithmParameterException
            | CertificateException
            | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean initCipher() {
        try {
            cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
            NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
            | UnrecoverableKeyException | IOException
            | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
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

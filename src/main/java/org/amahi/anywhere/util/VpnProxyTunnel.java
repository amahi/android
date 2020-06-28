package org.amahi.anywhere.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import org.amahi.anywhere.activity.AuthenticationActivity;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
public class VpnProxyTunnel extends VpnService implements Handler.Callback {
    private static final String TAG = VpnProxyTunnel.class.getSimpleName();
    public static final String ACTION_CONNECT = "com.example.android.toyvpn.START";
    public static final String ACTION_DISCONNECT = "com.example.android.toyvpn.STOP";
    private Handler mHandler;
    private static class Connection extends Pair<Thread, ParcelFileDescriptor> {
        public Connection(Thread thread, ParcelFileDescriptor pfd) {
            super(thread, pfd);
        }
    }
    private final AtomicReference<Thread> mConnectingThread = new AtomicReference<>();
    private final AtomicReference<Connection> mConnection = new AtomicReference<>();
    private AtomicInteger mNextConnectionId = new AtomicInteger(1);
    private PendingIntent mConfigureIntent;
    @Override
    public void onCreate() {
        // The handler is only used to show messages.
        if (mHandler == null) {
            mHandler = new Handler(this);
        }
        // Create the intent to "configure" the connection (just start ToyVpnClient).
        mConfigureIntent = PendingIntent.getActivity(this, 0, new Intent(this, AuthenticationActivity.class),
            PendingIntent.FLAG_UPDATE_CURRENT);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
            disconnect();
            return START_NOT_STICKY;
        } else {
            return START_STICKY;
        }
    }
    @Override
    public void onDestroy() {
        disconnect();
    }
    @Override
    public boolean handleMessage(Message message) {
        Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();

        return true;
    }

    private void setConnectingThread(final Thread thread) {
        final Thread oldThread = mConnectingThread.getAndSet(thread);
        if (oldThread != null) {
            oldThread.interrupt();
        }
    }
    private void setConnection(final Connection connection) {
        final Connection oldConnection = mConnection.getAndSet(connection);
        if (oldConnection != null) {
            try {
                oldConnection.first.interrupt();
                oldConnection.second.close();
            } catch (IOException e) {
                Log.e(TAG, "Closing VPN interface", e);
            }
        }
    }
    private void disconnect() {
        setConnectingThread(null);
        setConnection(null);
        stopForeground(true);
    }
    private void updateForegroundNotification(final int message) {
        final String NOTIFICATION_CHANNEL_ID = "ToyVpn";
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(
            NOTIFICATION_SERVICE);
    }
}

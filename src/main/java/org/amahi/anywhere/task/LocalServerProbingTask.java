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

package org.amahi.anywhere.task;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import org.amahi.anywhere.bus.AuthenticationConnectionFailedEvent;
import org.amahi.anywhere.bus.AuthenticationFailedEvent;
import org.amahi.anywhere.bus.AuthenticationSucceedEvent;
import org.amahi.anywhere.bus.BusEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.model.Authentication;
import org.amahi.anywhere.util.Preferences;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LocalServerProbingTask extends AsyncTask<Void, Void, BusEvent> {

    private static final String TAG = "LocalServerProbingTask";

    private WeakReference<Context> context;
    private OkHttpClient httpClient;
    private RequestBody body;

    public LocalServerProbingTask(Context context, @NonNull String pin) {
        this.context = new WeakReference<>(context);
        this.httpClient = buildHttpClient();
        this.body = buildRequestBody(pin);
    }

    private OkHttpClient buildHttpClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(1, TimeUnit.SECONDS);
        httpClient = clientBuilder.build();
        return httpClient;
    }

    private RequestBody buildRequestBody(String pin) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        return RequestBody.create(JSON, "{\"pin\":\"" + pin + "\"}");
    }

    @Override
    protected BusEvent doInBackground(Void... voids) {
//        String ipString = "192.168.54.129";
        WifiManager wm = (WifiManager) context.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm != null) {
            DhcpInfo d = wm.getDhcpInfo();
            String ip = intToIp(d.dns1);
            String prefix = ip.substring(0, ip.lastIndexOf(".") + 1);
            for (int i = 0; i < 255; i++) {
                String testIp = prefix + String.valueOf(i);
//                testIp = "192.168.54.129";
                try {
                    InetAddress ipAddress = InetAddress.getByName(testIp);
                    String hostName = ipAddress.getCanonicalHostName();
                    if (ipAddress.isReachable(200)) {
                        Request httpRequest = new Request.Builder()
                            .url(getConnectionUrl(hostName))
                            .post(body)
                            .build();

                        Response response = httpClient
                            .newCall(httpRequest)
                            .execute();

                        if (response.code() == 200) {
                            Gson gson = new Gson();
                            String json = response.body().string();
                            Authentication authentication = gson.fromJson(json, Authentication.class);
                            Preferences.setLocalServerIP(context.get().getApplicationContext(), testIp);
                            return new AuthenticationSucceedEvent(authentication);
                        } else if (response.code() == 403) {
                            return new AuthenticationFailedEvent();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new AuthenticationConnectionFailedEvent();
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
            ((i >> 8) & 0xFF) + "." +
            ((i >> 16) & 0xFF) + "." +
            ((i >> 24) & 0xFF);
    }

    private String getConnectionUrl(String hostName) {
        return "http://" + hostName + ":4563/auth";
    }

    @Override
    protected void onPostExecute(BusEvent busEvent) {
        super.onPostExecute(busEvent);

        BusProvider.getBus().post(busEvent);
    }
}

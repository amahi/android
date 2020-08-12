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

package org.amahi.anywhere.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Amahi authenticator service.
 * Allows {@link android.accounts.AccountManager} to interact with{@link AmahiAuthenticator}.
 */
public class AmahiAuthenticatorService extends Service {
    private AmahiAuthenticator authenticator;

    @Override
    public void onCreate() {
        super.onCreate();

        authenticator = new AmahiAuthenticator(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}

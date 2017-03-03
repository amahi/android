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

package org.amahi.anywhere.server.response;

import org.amahi.anywhere.bus.AuthenticationConnectionFailedEvent;
import org.amahi.anywhere.bus.AuthenticationFailedEvent;
import org.amahi.anywhere.bus.AuthenticationSucceedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.model.Authentication;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Authentication response proxy. Consumes API callback and posts it via {@link com.squareup.otto.Bus}
 * as {@link org.amahi.anywhere.bus.BusEvent}.
 */
public class AuthenticationResponse implements Callback<Authentication>
{
	@Override
	public void onResponse(Call<Authentication> call, Response<Authentication> response) {
		if (response.isSuccessful())
			BusProvider.getBus().post(new AuthenticationSucceedEvent(response.body()));
		else
			this.onFailure(call, new HttpException(response));
	}

	@Override
	public void onFailure(Call<Authentication> call, Throwable t) {
		if (t instanceof IOException) { //implies no network connection
			BusProvider.getBus().post(new AuthenticationConnectionFailedEvent());
		} else {
			BusProvider.getBus().post(new AuthenticationFailedEvent());
		}
	}
}

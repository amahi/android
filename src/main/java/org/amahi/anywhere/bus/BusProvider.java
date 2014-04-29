package org.amahi.anywhere.bus;

import com.squareup.otto.Bus;

public final class BusProvider
{
	private static final class BusHolder
	{
		public static final Bus BUS = new Bus();
	}

	private BusProvider() {
	}

	public static Bus getBus() {
		return BusHolder.BUS;
	}
}

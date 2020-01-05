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

package org.amahi.anywhere.bus;

import com.squareup.otto.Bus;

/**
 * {@link com.squareup.otto.Bus} holder, which allows to use a single instance everywhere.
 */
public final class BusProvider {
    private BusProvider() {

    }

    public static Bus getBus() {
        return BusHolder.BUS;
    }

    private static final class BusHolder {
        public static final Bus BUS = new Bus();
    }
}

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

package org.amahi.anywhere.cache;

import android.Manifest;
import android.content.Context;

import org.amahi.anywhere.model.AudioMetadata;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Manager class for managing cache for {@link AudioMetadata}.
 */

public class CacheManager {
    private MemoryCache memoryCache;
    private DiskCache diskCache;

    public CacheManager(Context context) {
        memoryCache = new MemoryCache();
        if (EasyPermissions.hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            diskCache = new DiskCache(context);
        }
    }

    public AudioMetadata getMetadataFromCache(String key) {
        AudioMetadata metadata = null;
        if (key != null) {
            metadata = memoryCache.get(key);
            if (metadata == null && diskCache != null) {
                metadata = diskCache.get(key);
            }
        }
        return metadata;
    }

    public void addMetadataToCache(String key, AudioMetadata metadata) {
        // Add to memory cache
        memoryCache.add(key, metadata);

        if (diskCache != null) {
            // Also add to disk cache
            diskCache.add(key, metadata);
        }
    }

}

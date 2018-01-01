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

import android.content.Context;

import org.amahi.anywhere.model.AudioMetadata;

/**
 * Manager class for managing cache for audio files album art images.
 */

public class CacheManager {
    private MemoryCache memoryCache;
    private DiskCache diskCache;

    public CacheManager(Context context) {
        memoryCache = new MemoryCache();
        diskCache = new DiskCache(context);
    }

/*
    class AudioMetadataWorkerTask extends AsyncTask<Integer, Void, AudioMetadata> {
        // Decode image in background.
        @Override
        protected AudioMetadata doInBackground(Integer... params) {
            final String imageKey = String.valueOf(params[0]);

            // Check disk cache in background thread
            AudioMetadata metadata = memoryCache.getAudioMetadata(imageKey);

            if (metadata == null) { // Not found in disk cache
                // Process as normal
                //TODO fetch metadata from internet
            }

            // Add final metadata to caches
            addAudioMetadataToCache(imageKey, metadata);

            return metadata;
        }
    }
*/


    public AudioMetadata getMetadataFromCache(String key) {
        AudioMetadata metadata = memoryCache.get(key);
        if (metadata == null) {
            metadata = diskCache.get(key);
        }
        return metadata;
    }

    public void addAudioMetadataToCache(String key, AudioMetadata metadata) {
        // Add to memory cache
        memoryCache.add(key, metadata);

        // Also add to disk cache
        diskCache.add(key, metadata);
    }


}

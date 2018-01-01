package org.amahi.anywhere.cache;

import android.util.LruCache;

import org.amahi.anywhere.model.AudioMetadata;

/**
 * In memory cache implementation. Internally uses {@link android.util.LruCache}.
 */

class MemoryCache {
    private LruCache<String, AudioMetadata> mMemoryCache;


    MemoryCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. (in kilobytes)
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, AudioMetadata>(cacheSize) {
            @Override
            protected int sizeOf(String key, AudioMetadata metadata) {
                // The cache size will be measured in kilobytes rather than
                // number of items. Used only bitmap to measure the size since
                // size of other fields won't make much impact.
                if (metadata.getAudioAlbumArt() != null) {
                    return metadata.getAudioAlbumArt().getByteCount() / 1024;
                } else {
                    return 0;
                }
            }
        };
    }

    // to add a new audio metadata object to cache
    void add(String key, AudioMetadata metadata) {
        if (get(key) == null) {
            mMemoryCache.put(key, metadata);
        }
    }

    // to retrieve a cached audio metadata object
    AudioMetadata get(String key) {
        return mMemoryCache.get(key);
    }
}

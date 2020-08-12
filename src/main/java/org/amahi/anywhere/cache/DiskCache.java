package org.amahi.anywhere.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.bumptech.glide.disklrucache.DiskLruCache;

import org.amahi.anywhere.BuildConfig;
import org.amahi.anywhere.model.AudioMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.os.Environment.isExternalStorageRemovable;

/**
 * Disk cache implementation. Internally uses {@link DiskLruCache}.
 */

public class DiskCache {
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "metadata";
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 5;
    private static final int BITMAP_INDEX = 0;
    private static final int TITLE_INDEX = 1;
    private static final int ALBUM_INDEX = 2;
    private static final int ARTIST_INDEX = 3;
    private static final int DURATION_INDEX = 4;

    private final Object mDiskCacheLock = new Object();
    private DiskLruCache mDiskLruCache;
    private boolean mDiskCacheStarting = true;

    DiskCache(Context context) {
        File cacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR);
        synchronized (mDiskCacheLock) {
            try {
                mDiskLruCache = DiskLruCache.open(cacheDir, APP_VERSION, VALUE_COUNT, DISK_CACHE_SIZE);
                mDiskCacheStarting = false; // Finished initialization
                mDiskCacheLock.notifyAll(); // Wake any waiting threads
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
    // but if not mounted, falls back on internal storage.
    private File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
            Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor) throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(editor.getFile(BITMAP_INDEX));
            return bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    void add(String key, AudioMetadata metadata) {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null && !containsKey(key)) {
                DiskLruCache.Editor editor = null;
                try {
                    editor = mDiskLruCache.edit(key);
                    if (editor == null) {
                        return;
                    }

                    if (writeBitmapToFile(metadata.getAudioAlbumArt(), editor)) {
                        editor.set(TITLE_INDEX, metadata.getAudioTitle());
                        editor.set(ALBUM_INDEX, metadata.getAudioAlbum());
                        editor.set(ARTIST_INDEX, metadata.getAudioArtist());
                        editor.set(DURATION_INDEX, String.valueOf(metadata.getDuration()));
                        mDiskLruCache.flush();
                        editor.commit();
                        if (BuildConfig.DEBUG) {
                            Log.d("cache_test_DISK_", "metadata put on disk cache " + key);
                        }
                    } else {
                        editor.abort();
                        if (BuildConfig.DEBUG) {
                            Log.d("cache_test_DISK_", "ERROR on: metadata put on disk cache " + key);
                        }
                    }
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        Log.d("cache_test_DISK_", "ERROR on: metadata put on disk cache " + key);
                    }
                    try {
                        if (editor != null) {
                            editor.abort();
                        }
                    } catch (IOException ignored) {
                    }
                }

            }
        }

    }

    private Bitmap getBitmap(DiskLruCache.Value value) {
        Bitmap bitmap = null;
        final File file = value.getFile(BITMAP_INDEX);
        if (file != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(fileInputStream, null, options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return bitmap;
    }

    public AudioMetadata get(String key) {
        synchronized (mDiskCacheLock) {
            // Wait while disk cache is started from background thread
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException ignored) {
                }
            }

            if (mDiskLruCache != null) {
                AudioMetadata metadata = new AudioMetadata();
                try {
                    DiskLruCache.Value value = mDiskLruCache.get(key);
                    if (value == null) {
                        return null;
                    }
                    metadata.setAudioAlbumArt(getBitmap(value));
                    metadata.setAudioTitle(value.getString(TITLE_INDEX));
                    metadata.setAudioAlbum(value.getString(ALBUM_INDEX));
                    metadata.setAudioArtist(value.getString(ARTIST_INDEX));
                    metadata.setDuration(value.getString(DURATION_INDEX));
                } catch (IOException e) {
                    e.printStackTrace();
                    metadata = null;
                }

                if (BuildConfig.DEBUG) {
                    Log.d("cache_test_DISK_", (metadata == null ? "metadata read failed " : "metadata read ") + key);
                }
                return metadata;
            }
        }
        return null;
    }

    private boolean containsKey(String key) {
        boolean contained = false;
        try {
            contained = mDiskLruCache.get(key) != null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contained;
    }

    public void clearCache() {
        if (BuildConfig.DEBUG) {
            Log.d("cache_test_DISK_", "disk cache CLEARED");
        }
        try {
            mDiskLruCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskLruCache.getDirectory();
    }
}

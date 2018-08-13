package org.amahi.anywhere.util;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileCopiedEvent;
import org.amahi.anywhere.bus.FileMovedEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class FileManager {

    public static final int RECENT_FILE = 0;
    public static final int SERVER_FILE = 1;
    public static final int OFFLINE_FILE = 2;

    private Context ctx;

    public FileManager(Context context) {
        this.ctx = context;
    }

    public static FileManager newInstance(Context context) {
        return new FileManager(context);
    }

    public void copyFile(File sourceLocation, File targetLocation) {
        new FileCopyTask(sourceLocation, targetLocation).execute();
    }

    public void moveFile(File sourceLocation, File targetLocation) {
        new FileMoveTask(sourceLocation, targetLocation).execute();
    }

    public void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    public Uri getContentUriForOfflineFile(String name) {
        File file = new File(ctx.getFilesDir(), Downloader.OFFLINE_PATH + "/" + name);
        return getContentUri(file);
    }

    public Uri getContentUri(File file) {
        return FileProvider.getUriForFile(ctx, "org.amahi.anywhere.fileprovider", file);
    }

    private void startFileCopying(File sourceLocation, File targetLocation) {
        if (sourceLocation.exists()) {
            InputStream in;
            try {
                in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);

                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class FileCopyTask extends AsyncTask<Void, Void, Void> {

        private File sourceLocation, targetLocation;

        FileCopyTask(File sourceLocation, File targetLocation) {
            this.sourceLocation = sourceLocation;
            this.targetLocation = targetLocation;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            startFileCopying(sourceLocation, targetLocation);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            BusProvider.getBus().post(new FileCopiedEvent(targetLocation));
        }
    }

    private class FileMoveTask extends AsyncTask<Void,Void,Void>{

        private File sourceLocation, targetLocation;

        FileMoveTask(File sourceLocation, File targetLocation) {
            this.sourceLocation = sourceLocation;
            this.targetLocation = targetLocation;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            startFileCopying(sourceLocation, targetLocation);
            deleteFile(sourceLocation);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            BusProvider.getBus().post(new FileMovedEvent(targetLocation));
        }
    }
}

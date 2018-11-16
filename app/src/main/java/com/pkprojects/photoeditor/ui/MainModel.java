package com.pkprojects.photoeditor.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pkprojects.photoeditor.AppConfig;
import com.pkprojects.photoeditor.R;
import com.pkprojects.photoeditor.backend.ImageService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainModel implements MVPContract.Model {

    private static final String TAG = MainModel.class.getSimpleName();
    private static final String LAST_FILTER = "last_filter";
    private static final String JOB_STARTED = "job_started";

    private MVPContract.Presenter presenter;

    private Context context;

    private Bitmap baseBitmap = null;
    private Bitmap currentBitmap = null;

    private Uri baseImageUri = null;
    private int lastFilter = 0;

    private SharedPreferences sp;

    private IntentFilter statusIntentFilter;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;

    private long jobStarted;
    private Handler timeoutHandler;

    public MainModel(Context c) {
        context = c;
        setUp();
    }

    private void setUp() {
        sp = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
        jobStarted = 0;
        setService();
    }

    @Override
    public boolean setPresenter(MVPContract.Presenter presenter) {
        this.presenter = presenter;
        return true;
    }

    @Override
    public boolean setImage(Uri imageUri) {
        Log.i(TAG, "setImage - URI/gallery");
        jobStarted = 0;
        if(imageUri != baseImageUri) {
            baseImageUri = imageUri;
            try {
                InputStream imageStream = context.getContentResolver().openInputStream(baseImageUri);
                baseBitmap = BitmapFactory.decodeStream(imageStream);
                setBitmap();
                saveImageFiles();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                presenter.showMessage(R.string.msg_error_noimage);
            }
        }
        return false;
    }

    @Override
    public boolean setImage(Bitmap bitmap) {
        Log.i(TAG, "setImage - bitmap/camera");
        baseBitmap = bitmap;
        jobStarted = 0;
        lastFilter = 0;
        currentBitmap = null;
        saveImageFiles();
        return baseBitmap != null;
    }

    @Override
    public boolean createImage(int filterId) {
        Log.i(TAG, "createImage() in the work");
        if(baseBitmap == null) return false;
        if(filterId == 0) setBitmap();
        else {
            Log.i(TAG, "createImage() is working with filter " + filterId);

            lastFilter = filterId;

            Intent intent = new Intent();
            intent.putExtra(AppConfig.SERVICE_FILTER, filterId);
            ImageService.enqueueWork(context, ImageService.class, AppConfig.SERVICE_JOB_ID, intent);

            jobStarted = System.currentTimeMillis();
            setTimeout(AppConfig.JOB_TIMEOUT);

            return true;
        }
        return false;
    }

    @Override
    public boolean start() {
        broadcastManager.registerReceiver(broadcastReceiver, statusIntentFilter);
        return true;
    }

    @Override
    public boolean stop() {
        broadcastManager.unregisterReceiver(broadcastReceiver);
        return true;
    }

    @Override
    public boolean saveState() {
        Log.i(TAG, "saveState() with lastFilter " + lastFilter + " and jobStarted " + jobStarted);
        if(baseBitmap == null) clearFiles();

        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(LAST_FILTER, lastFilter);
        editor.putLong(JOB_STARTED, jobStarted);
        editor.apply();

        if(timeoutHandler != null) {
            Log.i(TAG, "Removing messages from timeout handler");
            timeoutHandler.removeCallbacksAndMessages(null);
        }

        return true;
    }

    @Override
    public boolean restoreState() {

        lastFilter = sp.getInt(LAST_FILTER, 0);
        jobStarted = sp.getLong(JOB_STARTED, 0);

        Log.i(TAG, "restoreState() with lastFilter " + lastFilter + " and jobStarted " + jobStarted);

        presenter.filterSet(lastFilter);

        if(jobStarted > 0) {
            int timeDifference = Math.round((System.currentTimeMillis() - jobStarted)/1000);

            Log.i(TAG, "restoreState() Job Started: " + jobStarted + " Difference: " + timeDifference);

            if(timeoutHandler != null) timeoutHandler.removeCallbacksAndMessages(null);

            if(timeDifference >= AppConfig.JOB_TIMEOUT) {
                jobStarted = 0;
            } else {
                Log.i(TAG, "Setting loader");
                presenter.setJobStart(jobStarted);
                setTimeout(AppConfig.JOB_TIMEOUT - timeDifference);
            }
        }

        File imgFile = new File(AppConfig.IMAGE_BASE);
        if(imgFile.exists()) {
            baseBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        imgFile = new  File(AppConfig.IMAGE_CURRENT);
        if(imgFile.exists()) {
            currentBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }

        if(currentBitmap != null) presenter.imageCreated(currentBitmap);

        return true;
    }

    private boolean setService() {
        broadcastManager = LocalBroadcastManager.getInstance(context);
        statusIntentFilter = new IntentFilter(AppConfig.SERVICE_BROADCAST_ACTION);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "Received job broadcast");
                if(jobStarted > 0) { // there is a queued job, otherwise timed oud, ignore
                    Log.i(TAG, "Processing finished job broadcast");
                    jobStarted = 0;
                    currentBitmap = BitmapFactory.decodeFile(AppConfig.IMAGE_CURRENT);
                    presenter.imageCreated(currentBitmap);
                    presenter.setJobStart(jobStarted);
                    if(timeoutHandler != null) {
                        Log.i(TAG, "Removing messages from timeout handler");
                        timeoutHandler.removeCallbacksAndMessages(null);
                    }
                } else {
                    Log.i(TAG, "Job broadcast ignored due to timeout");
                    saveCurrentImage(); // overwriting timed out result with current image - or deleting if none available
                }
            }
        };

        return true;
    }

    private long setTimeout(int seconds) {
        Log.i(TAG, "Setting timeout handler");
        if(timeoutHandler == null) timeoutHandler = new Handler();
        long timeout = seconds * 1000;
        timeoutHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(jobStarted > 0) {
                    Log.i(TAG, "Timeout handler fired");
                    jobStarted = 0;
                    presenter.setJobStart(jobStarted);
                    presenter.showMessage(R.string.msg_job_timeout);
                }
            }
        }, timeout);
        return timeout;
    }

    private boolean setBitmap() {
        Log.i(TAG, "setBitmap() - current image");
        lastFilter = 0;
        currentBitmap = null;
        presenter.setJobStart(0);
        saveCurrentImage(); // overwriting old work result
        return  presenter.imageCreated(baseBitmap);
    }

    private boolean clearFiles() {
        Log.i(TAG, "clearFiles() in the work");
        return deleteFile(AppConfig.IMAGE_BASE)&& deleteFile(AppConfig.IMAGE_CURRENT);
    }

    private boolean saveImageFiles() {
        Log.i(TAG, "saveImageFiles() in the work");
        return saveBaseImage() && saveCurrentImage();
    }

    private boolean saveBaseImage() {
        Log.i(TAG, "saveBaseImage() in the work");
        if(baseBitmap != null) return saveImage(baseBitmap, AppConfig.IMAGE_BASE);
        else return deleteFile(AppConfig.IMAGE_BASE);
    }

    private boolean saveCurrentImage() {
        Log.i(TAG, "saveCurrentImage() in the work");
        if(currentBitmap != null) return saveImage(currentBitmap, AppConfig.IMAGE_CURRENT);
        else if(baseBitmap != null) return saveImage(baseBitmap, AppConfig.IMAGE_CURRENT);
        else return deleteFile(AppConfig.IMAGE_CURRENT);
    }

    private boolean saveImage(Bitmap bitmap, String path) {
        Log.i(TAG, "saveImage with path " + path);
        if(bitmap == null) return false;
        File file = new File(path);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean deleteFile(String path) {
        Log.i(TAG, "deleteFile with path " + path);
        File file = new File(path);
        return file.delete();
    }
}
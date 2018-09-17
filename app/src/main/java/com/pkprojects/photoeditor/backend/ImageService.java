package com.pkprojects.photoeditor.backend;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.pkprojects.photoeditor.AppConfig;
import com.pkprojects.photoeditor.R;
import com.pkprojects.photoeditor.utils.BitmapProcessing;

import java.io.FileOutputStream;
import java.io.IOException;

public class ImageService extends JobIntentService {

    private static final String TAG = ImageService.class.getSimpleName();

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        long jobStarted = System.currentTimeMillis();

        Bitmap currentBitmap = BitmapFactory.decodeFile(AppConfig.IMAGE_CURRENT);

        int filterId = intent.getIntExtra(AppConfig.SERVICE_FILTER, 0);
        String[] arrayName = getResources().getStringArray(R.array.filter_values);

        String filterName = arrayName[filterId];

        Log.i(TAG, "ImageService work with filter: " + filterName + " id: " + filterId);

        switch(filterName) {
            case "sepia":
                currentBitmap = BitmapProcessing.sepia(currentBitmap);
                break;
            case "sketch":
                currentBitmap = BitmapProcessing.sketch(currentBitmap);
                break;
            case "contrast":
                currentBitmap = BitmapProcessing.contrast(currentBitmap, 20); // -100 100
                break;
            case "lighter":
                currentBitmap = BitmapProcessing.brightness(currentBitmap, 25); // -255 255
                break;
            case "darker":
                currentBitmap = BitmapProcessing.brightness(currentBitmap, -25);
                break;
            case "saturate":
                currentBitmap = BitmapProcessing.saturation(currentBitmap, 150); // 0-200
                break;
            case "desaturate":
                currentBitmap = BitmapProcessing.saturation(currentBitmap, 50);
                break;
            case "grayscale":
                currentBitmap = BitmapProcessing.grayscale(currentBitmap);
                break;
            case "sharpen":
                currentBitmap = BitmapProcessing.sharpen(currentBitmap);
                break;
            case "blur":
                currentBitmap = BitmapProcessing.gaussian(currentBitmap);
                break;
        }

        // timeout checks
        long jobEnded = System.currentTimeMillis();

        int jobSeconds = Math.round((jobEnded - jobStarted)/1000);

        if(jobSeconds < AppConfig.JOB_TIMEOUT) {

            // saving file
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(AppConfig.IMAGE_CURRENT);
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
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

            // sending success broadcast to front-end
            Intent intentToSend = new Intent(AppConfig.SERVICE_BROADCAST_ACTION);
            intentToSend.putExtra(AppConfig.SERVICE_FILTER, filterId); // for confirmation if needed
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentToSend);
        }
    }
}

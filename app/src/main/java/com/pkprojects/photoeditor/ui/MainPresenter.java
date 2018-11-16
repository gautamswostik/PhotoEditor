package com.pkprojects.photoeditor.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

public class MainPresenter implements MVPContract.Presenter {

    private static final String TAG = MainPresenter.class.getSimpleName();

    private MVPContract.View view;
    private MVPContract.Model model;

    private int lastFilter;
    private long jobStarted;

    public MainPresenter(MVPContract.View view, MVPContract.Model model) {
        this.view = view;
        this.model = model;
        this.model.setPresenter(this);
        lastFilter = 0;
        jobStarted = 0;
    }

    @Override
    public boolean imageSelected(Uri imageUri) {
        Log.i(TAG, "imageSelected - URI/gallery");
        lastFilter = 0;
        jobStarted = 0;
        return model.setImage(imageUri);
    }

    @Override
    public boolean imageSelected(Bitmap image) {
        Log.i(TAG, "imageSelected - bitmap/camera");
        lastFilter = 0;
        jobStarted = 0;
        return model.setImage(image);
    }

    @Override
    public boolean filterSelected(int filterType) {
        Log.i(TAG, "filterSelected - " + filterType + ", fired");
        lastFilter = filterType;
        if(filterType != 0) view.showLoader(true);
        return model.createImage(filterType);
    }

    @Override
    public boolean filterSet(int filterType) {
        Log.i(TAG, "filterSet - " + filterType + " Last filter: " + lastFilter);
        lastFilter = filterType;
        view.filterSet(filterType);
        return true;
    }

    @Override
    public boolean imageCreated(Bitmap image) {
        Log.i(TAG, "imageCreated in the work");
        if(jobStarted == 0) view.showLoader(false);
        return view.showImage(image);
    }

    @Override
    public boolean showMessage(int stringResId) {
        return view.showMessage(stringResId);
    }

    @Override
    public boolean setJobStart(long millis) {
        Log.i(TAG, "setJobStart in the work " + millis);
        boolean showLoader = false;
        if(millis > 0) {
            jobStarted = millis;
            showLoader = true;
        }
        return view.showLoader(showLoader);
    }

    @Override
    public boolean start() {
        return model.start();
    }

    @Override
    public boolean stop() {
        return model.stop();
    }

    @Override
    public boolean saveState() {
        return model.saveState();
    }

    @Override
    public boolean restoreState() {
        return model.restoreState();
    }

    public int getLastFilter() {
        return lastFilter;
    }

    public long getJobStarted() {
        return jobStarted;
    }
}

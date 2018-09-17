package com.pkprojects.photoeditor.ui;

import android.graphics.Bitmap;
import android.net.Uri;

public class MVPContract {

    public interface View {
        boolean showImage(Bitmap image);
        boolean showMessage(int stringResId);
        boolean showLoader(boolean showHide);
        boolean filterSet(int filterType);
    }

    public interface Presenter {
        boolean imageSelected(Uri imageUri);
        boolean imageSelected(Bitmap image);
        boolean filterSelected(int filterType);
        boolean filterSet(int filterType);
        boolean imageCreated(Bitmap image);
        boolean showMessage(int stringResId);
        boolean setJobStart(long millis);
        boolean start();
        boolean stop();
        boolean saveState();
        boolean restoreState();
    }

    public interface Model {
        boolean setPresenter(Presenter presenter);
        boolean setImage(Uri imageUri);
        boolean setImage(Bitmap bitmap);
        boolean createImage(int filterType);
        boolean start();
        boolean stop();
        boolean saveState();
        boolean restoreState();
    }
}

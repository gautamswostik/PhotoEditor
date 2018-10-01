package com.pkprojects.photoeditor.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.pkprojects.photoeditor.AppBase;
import com.pkprojects.photoeditor.AppConfig;
import com.pkprojects.photoeditor.BuildConfig;
import com.pkprojects.photoeditor.R;
import com.pkprojects.photoeditor.di.DaggerActivityComponent;
import com.pkprojects.photoeditor.di.MvpModule;
import com.pkprojects.photoeditor.utils.SameSpinner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivityMain extends AppCompatActivity implements MVPContract.View {

    private static final String TAG = ActivityMain.class.getSimpleName();

    @BindView(R.id.image_view)
    ImageView imageView;

    @BindView(R.id.spinner)
    SameSpinner spinner; // custom spinner to allow the same item selection

    @BindView(R.id.loadingScreen)
    View loadingScreen;

    @Inject
    MVPContract.Presenter presenter;

    private boolean imageLoaded = false;
    private boolean screenRestoration = false;

    //**********************region BASE+MVP *******************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // presenter = new PresenterMain(this, new ModelMain(this)); // DI below

        DaggerActivityComponent.builder()
                .appComponent(AppBase.getApp(this).component())
                .mvpModule(new MvpModule(this))
                .build()
                .inject(this);

        if(savedInstanceState != null) {
            Log.i(TAG, "Restoring state");
            presenter.restoreState();
            screenRestoration = true;
        }

        populateFilterList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.stop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.saveState();
    }

    @Override
    public boolean showImage(Bitmap image) {
        if(image != null) {
            imageLoaded = true;
            imageView.setImageBitmap(image);
            return true;
        }
        return false;
    }

    public boolean showMessage(int stringResId) {
        return showToast(stringResId);
    }

    @Override
    public boolean showLoader(boolean showHide) {
        Log.i(TAG, "Set loader: " + showHide);
        int visibility = showHide ? View.VISIBLE : View.GONE;
        loadingScreen.setVisibility(visibility);
        return showHide && loadingScreen.getVisibility() == View.VISIBLE;
    }

    @Override
    public boolean filterSet(int filterType) {
        spinnerToPosition(filterType);
        return true;
    }

    //**********************endregion *************************************************************/

    //**********************region PERMISSIONS ****************************************************/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == AppConfig.PERMISSION_GALLERY) {
                try {
                    final Uri imageUri = data.getData();
                    presenter.imageSelected(imageUri);
                    spinnerToPosition(0);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    showToast(R.string.msg_error_noimagedata);
                }
            } else if(requestCode == AppConfig.PERMISSION_CAMERA) {
                try {
                    Bitmap baseBitmap = (Bitmap) data.getExtras().get("data");
                    presenter.imageSelected(baseBitmap);
                    spinnerToPosition(0);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    showToast(R.string.msg_error_noimagedata);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getImage(requestCode);
        } else {
            showToast(R.string.msg_mediacamera_required);
        }
    }

    private boolean checkPermissions(int btnId) {
        if(btnId == R.id.button_gallery) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, AppConfig.PERMISSION_GALLERY);
            } else {
                return true;
            }
        } else {
            if(!isCameraAvailable(this)) { // checking if camera is available at all
                showToast(R.string.msg_nocamera);
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, AppConfig.PERMISSION_CAMERA);
            } else {
                return true;
            }
        }
        return false;
    }

    //**********************endregion *************************************************************/

    //**********************region BUTTONS ********************************************************/

    private void getImage(int how) {
        if(how == AppConfig.PERMISSION_GALLERY || how == R.id.button_gallery)
            getGalleryImage();
        else
            getPhotoImage();
    }

    private void getGalleryImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, AppConfig.PERMISSION_GALLERY);
    }

    private void getPhotoImage() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, AppConfig.PERMISSION_CAMERA);
        } else {
            showToast(R.string.msg_nocamera);
        }
    }

    @OnClick({ R.id.button_gallery, R.id.button_camera, R.id.button_share })
    public void buttonClicked(View btn) {
        int id = btn.getId();
        switch(id) {
            case R.id.button_gallery:
            case R.id.button_camera:
                if(checkPermissions(id)) getImage(id);
                break;
            case R.id.button_share:
                if(imageLoaded) {

                    File toSend = new File(AppConfig.IMAGE_CURRENT);
                    Uri outputFileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", toSend);

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, outputFileUri);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.send_subject));
                    shareIntent.putExtra(Intent.EXTRA_TEXT, getText(R.string.send_text) + getPackageName());
                    shareIntent.setType("image/jpeg");
                    startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
                } else {
                    showToast(R.string.msg_select_image);
                }
                break;
            default:
                break;
        }
    }

    //**********************endregion *************************************************************/

    //**********************region UTILS **********************************************************/

    private void populateFilterList() {
        String[] arrayName = getResources().getStringArray(R.array.filter_titles);
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(arrayName));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, namesList);
        spinner.setAdapter(adapter);

        setupSpinnerClick();
    }

    private void setupSpinnerClick() {
        // Runnable is to prevent spinner layout dropping after setSelection(0) after screen rotation
        spinner.post(new Runnable() {
            public void run() {
                // bug: spinner double firing on screen rotation
                spinner.setSelection(0,true); // to prevent double firing on screen restoration, bug: drop spinner layout
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                        if(!imageLoaded) return;
                        if(!screenRestoration) { // additional protection from premature firing
                            presenter.filterSelected(pos);
                        } else screenRestoration = false;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) { }
                });
            }
        });

    }

    private void spinnerToPosition(final int position) {
        // w/o delayed post spinner is being overwritten with 0 position after screen rotation
        spinner.postDelayed(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(position);
            }
        }, 100);
    }

    public static boolean isCameraAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    private boolean showToast(int id) {
        String text = this.getResources().getString(id);
        return showToast(text);
    }

    private boolean showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        return true;
    }

    //**********************endregion *************************************************************/
}
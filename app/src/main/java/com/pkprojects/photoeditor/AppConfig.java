package com.pkprojects.photoeditor;

import android.os.Environment;

public class AppConfig {

    public static final int PERMISSION_GALLERY = 101;
    public static final int PERMISSION_CAMERA = 102;

    public static final String IMAGE_BASE = Environment.getExternalStorageDirectory().toString().concat("/base_image.jpg");
    public static final String IMAGE_CURRENT = Environment.getExternalStorageDirectory().toString().concat("/current_image.jpg");

    public static final int SERVICE_JOB_ID = 10001;
    public static final String SERVICE_FILTER = "filterId";
    public static final String SERVICE_BROADCAST_ACTION = AppBase.getAppContext().getPackageName().concat(".IMAGE_READY");

    public static final int JOB_TIMEOUT = 30; // seconds

}

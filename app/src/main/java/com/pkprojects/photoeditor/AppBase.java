package com.pkprojects.photoeditor;

import android.app.Application;
import android.content.Context;

import com.pkprojects.photoeditor.di.AppComponent;
import com.pkprojects.photoeditor.di.AppModule;
import com.pkprojects.photoeditor.di.ContextModule;
import com.pkprojects.photoeditor.di.DaggerAppComponent;
import com.pkprojects.photoeditor.di.ModelModule;

public class AppBase extends Application {

    private static Context appContext = null;

    private AppComponent component;

    public void onCreate() {
        super.onCreate();

        appContext = getApplicationContext();

        component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .contextModule(new ContextModule(this))
                .modelModule(new ModelModule(this))
                .build();
    }

    public static AppBase getApp(Context context) {
        return (AppBase) context.getApplicationContext();
    }

    public static Context getAppContext() {
        return appContext;
    }

    public AppComponent component() {
        return component;
    }
}

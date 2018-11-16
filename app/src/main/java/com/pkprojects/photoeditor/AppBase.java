package com.pkprojects.photoeditor;

import android.app.Application;
import android.content.Context;

import com.pkprojects.photoeditor.di.component.AppComponent;
import com.pkprojects.photoeditor.di.component.DaggerAppComponent;
import com.pkprojects.photoeditor.di.module.AppModule;
import com.pkprojects.photoeditor.di.module.ContextModule;
import com.pkprojects.photoeditor.di.module.ModelModule;

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

package com.pkprojects.photoeditor.di.module;

import android.app.Application;

import com.pkprojects.photoeditor.AppBase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private AppBase appBase;

    public AppModule(AppBase appBase) {
        this.appBase = appBase;
    }

    @Provides
    @Singleton
    public Application provideApp() {
        return appBase;
    }
}
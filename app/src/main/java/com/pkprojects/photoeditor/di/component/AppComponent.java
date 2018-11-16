package com.pkprojects.photoeditor.di.component;

import android.app.Application;
import android.content.Context;

import com.pkprojects.photoeditor.AppBase;
import com.pkprojects.photoeditor.di.module.AppModule;
import com.pkprojects.photoeditor.di.module.ContextModule;
import com.pkprojects.photoeditor.di.module.ModelModule;
import com.pkprojects.photoeditor.ui.MainModel;

import javax.inject.Singleton;
import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, ModelModule.class, ContextModule.class})
public interface AppComponent {

    void inject(AppBase appBase);

    Context getContext();

    MainModel getInteractor();

    Application getApplication();
}

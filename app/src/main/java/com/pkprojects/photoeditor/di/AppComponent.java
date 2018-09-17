package com.pkprojects.photoeditor.di;

import android.app.Application;
import android.content.Context;

import com.pkprojects.photoeditor.AppBase;
import com.pkprojects.photoeditor.ui.ModelMain;

import javax.inject.Singleton;
import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, ModelModule.class, ContextModule.class})
public interface AppComponent {

    void inject(AppBase appBase);

    Context getContext();

    ModelMain getInteractor();

    Application getApplication();
}

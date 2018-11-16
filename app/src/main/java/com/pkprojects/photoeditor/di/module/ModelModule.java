package com.pkprojects.photoeditor.di.module;

import android.content.Context;

import com.pkprojects.photoeditor.ui.MainModel;

import dagger.Module;
import dagger.Provides;

@Module
public class ModelModule {

    private Context context;

    public ModelModule(Context context) {
        this.context = context;
    }

    @Provides
    public MainModel provideModelClass() {
        return new MainModel(context);
    }
}

package com.pkprojects.photoeditor.di;

import android.content.Context;

import com.pkprojects.photoeditor.ui.ModelMain;

import dagger.Module;
import dagger.Provides;

@Module
public class ModelModule {

    private Context context;

    public ModelModule(Context context) {
        this.context = context;
    }

    @Provides
    public ModelMain provideModelClass() {
        return new ModelMain(context);
    }
}

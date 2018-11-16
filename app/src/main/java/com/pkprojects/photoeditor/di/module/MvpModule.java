package com.pkprojects.photoeditor.di.module;

import android.content.Context;

import com.pkprojects.photoeditor.ui.MVPContract;
import com.pkprojects.photoeditor.ui.MainModel;
import com.pkprojects.photoeditor.ui.MainPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class MvpModule {

    private MVPContract.View view;

    public MvpModule(MVPContract.View view) {
        this.view = view;
    }

    @Provides
    public MVPContract.View provideView() {
        return view;
    }

    @Provides
    public MVPContract.Model provideModel() {
        return new MainModel((Context) view);
    }

    @Provides
    public MVPContract.Presenter providePresenter(MVPContract.View view, MainModel model) {
        return new MainPresenter(view, model);
    }
}
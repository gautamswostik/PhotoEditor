package com.pkprojects.photoeditor.di;

import android.content.Context;

import com.pkprojects.photoeditor.ui.MVPContract;
import com.pkprojects.photoeditor.ui.ModelMain;
import com.pkprojects.photoeditor.ui.PresenterMain;

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
        return new ModelMain((Context) view);
    }

    @Provides
    public MVPContract.Presenter providePresenter(MVPContract.View view, ModelMain model) {
        return new PresenterMain(view, model);
    }
}
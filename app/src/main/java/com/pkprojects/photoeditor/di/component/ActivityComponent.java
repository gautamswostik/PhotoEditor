package com.pkprojects.photoeditor.di.component;

import com.pkprojects.photoeditor.di.module.MvpModule;
import com.pkprojects.photoeditor.di.scope.ActivityScope;
import com.pkprojects.photoeditor.ui.MainActivity;
import com.pkprojects.photoeditor.ui.MVPContract;

import dagger.Component;

@ActivityScope
@Component(dependencies = AppComponent.class, modules = MvpModule.class)
public interface ActivityComponent {
    void inject(MainActivity mainActivity);

    MVPContract.Presenter getMainPresenter();
}

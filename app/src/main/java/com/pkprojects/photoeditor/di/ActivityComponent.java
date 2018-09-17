package com.pkprojects.photoeditor.di;

import com.pkprojects.photoeditor.di.scope.ActivityScope;
import com.pkprojects.photoeditor.ui.ActivityMain;
import com.pkprojects.photoeditor.ui.MVPContract;

import dagger.Component;

@ActivityScope
@Component(dependencies = AppComponent.class, modules = MvpModule.class)
public interface ActivityComponent {
    void inject(ActivityMain mainActivity);

    MVPContract.Presenter getMainPresenter();
}

package com.presentation_layer.fasteritaly.activity.application;

import android.app.Application;

import io.realm.Realm;

public class Init_Realm_Application extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}

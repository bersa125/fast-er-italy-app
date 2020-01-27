package com.presentation_layer.fasteritaly.view;

import android.content.Intent;
import android.net.Uri;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

public interface MainPageView {

    int ER_NEXT_ME_ACTIVITY=0;
    int ER_NEXT_ADDR_ACTIVITY=1;
    int DRUG_NEXT_ME_ACTIVITY=2;
    int DRUG_NEXT_ADDR_ACTIVITY=3;
    int HISTORY_ACTIVITY=4;
    int ADDRESSES_ACTIVITY =6;
    int SETTINGS_ACTIVITY=7;
    int LOGIN_ACTIVITY=10;
    int SYSTEM_SETTINGS_PERMISSIONS_APPLICATION=11;

    int REQUEST_LOGIN = 0;
    int REQUEST_PERMISSION_FINE_LOCATION=1;

    //Launches another activity
    void launchActivity(int i);
    //closes the app (background)
    void closeApp();
    //closes the app (no background)
    void exitApp();
    //disconnects the current user and returns to the login screen
    void disconnect();
    //changes the aspect based of the current screen configuration
    void changeAspect();

    void updateDrawerName(String name);
    void updateDrawerMail(String mail);
    void updateDrawerPhoto(Uri photo);
    void unlockScreenLoading(String message);

    void checkPermissions();
    void checkInternetFunctionality();
    void stopcheckInternetFunctionality();

    Object getActivity();

    Intent addExtraToIntent(Map<String, Boolean> values, Intent intent);
}

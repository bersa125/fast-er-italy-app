package com.presentation_layer.fasteritaly.view;

public interface SplashLoginView {
    int REQUEST_SPLASH_LOGIN = 0;
    void closeApp();
    void callAuthentication();
    void revokeConnection();
    void returnToMain(int result);
}

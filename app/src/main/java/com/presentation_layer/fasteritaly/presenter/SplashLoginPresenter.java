package com.presentation_layer.fasteritaly.presenter;

import com.presentation_layer.fasteritaly.interactor.SplashLoginInteractor;
import com.presentation_layer.fasteritaly.view.SplashLoginView;

import static android.app.Activity.RESULT_OK;

public class SplashLoginPresenter implements SplashLoginInteractor.onSplashLoginActivityInteractionListener {

    private SplashLoginView view;
    private SplashLoginInteractor interactor;

    public SplashLoginPresenter(SplashLoginView v, SplashLoginInteractor i){
        view= v;
        interactor=i;
    }

    public void onCreate(){
        onDisconnectRequest();
    }

    @Override
    public void onDisconnectRequest() {
        view.revokeConnection();
    }

    @Override
    public void onAuthenticationRequest() {
        view.callAuthentication();
    }

    @Override
    public void onCloseupRequest() {
        view.closeApp();
    }

    @Override
    public void onReturnToMain() {
        view.returnToMain(RESULT_OK);
    }
}

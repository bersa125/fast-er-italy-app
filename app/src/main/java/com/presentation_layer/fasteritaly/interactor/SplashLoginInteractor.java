package com.presentation_layer.fasteritaly.interactor;

public class SplashLoginInteractor {

    public interface onSplashLoginActivityInteractionListener{
        void onDisconnectRequest();
        void onAuthenticationRequest();
        void onCloseupRequest();
        void onReturnToMain();
    }

}

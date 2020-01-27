package com.presentation_layer.fasteritaly.presenter;

import android.content.Intent;
import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;
import com.presentation_layer.fasteritaly.interactor.MainPageInteractor;
import com.presentation_layer.fasteritaly.view.MainPageView;
import com.presentation_layer.fasteritaly.view.SettingsView;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class MainPagePresenter implements MainPageInteractor.onMainPageActivityInteractionListener {

    private MainPageView view;
    private MainPageInteractor interactor;

    private boolean LoginIsActive=false;

    //HashMaps for Extra information for child activity initializations
    private Map<String, Boolean> SearchERNextMe_Intent_Extra = new HashMap<String, Boolean>() {
        {
            put("SEARCH_TYPE",false);//false = E.R., true = Drugstores
            put("ON_ADDRESS", false);
        }
    };
    private Map<String, Boolean> SearchERNextAddress_Intent_Extra = new HashMap<String, Boolean>() {
        {
            put("SEARCH_TYPE",false);//false = E.R., true = Drugstores
            put("ON_ADDRESS", true);
        }
    };
    private Map<String, Boolean> SearchDrugsNextMe_Intent_Extra = new HashMap<String, Boolean>() {
        {
            put("SEARCH_TYPE",true);//false = E.R., true = Drugstores
            put("ON_ADDRESS", false);
        }
    };
    private Map<String, Boolean> SearchDrugsNextAddress_Intent_Extra = new HashMap<String, Boolean>() {
        {
            put("SEARCH_TYPE",true);//false = E.R., true = Drugstores
            put("ON_ADDRESS", true);
        }
    };


    public MainPagePresenter(MainPageView main, MainPageInteractor interactor){
        this.view =main;
        this.interactor =interactor;
    }

    //Activity interactions that can be implemented
    public void onDestroy() {
        view =null;
    }
    public void onResume(){ LoginIsActive = false; onScreenChange(); onCheckAndSetup();}
    public void onCreate(){
        onScreenChange();
    }
    public void onStart(FirebaseUser currentUser) {
        if(currentUser!=null) {
            LoginIsActive = false;
            interactor.saveCurrentFirebaseUser(this, currentUser);
        }
    }
    public void onPause() {
        view.stopcheckInternetFunctionality();
    }

    @Override
    public void onScreenChange() {
        view.changeAspect();
    }

    @Override
    public void onDisconnectRequest(int type) {
        if(!LoginIsActive) {
            LoginIsActive = true;
            if (type == RESULT_OK) {
                interactor.saveCurrentFirebaseUser(this, null);
                view.launchActivity(view.LOGIN_ACTIVITY);
            } else {
                view.launchActivity(view.LOGIN_ACTIVITY);
            }
        }
    }

    @Override
    public Intent onIntentCreation(Intent intent, int request) {
        switch(request){
            case MainPageView.ER_NEXT_ME_ACTIVITY:
                return view.addExtraToIntent(this.SearchERNextMe_Intent_Extra,intent);
            case MainPageView.ER_NEXT_ADDR_ACTIVITY:
                return view.addExtraToIntent(this.SearchERNextAddress_Intent_Extra,intent);
            case MainPageView.DRUG_NEXT_ME_ACTIVITY:
                return view.addExtraToIntent(this.SearchDrugsNextMe_Intent_Extra,intent);
            case MainPageView.DRUG_NEXT_ADDR_ACTIVITY:
                return view.addExtraToIntent(this.SearchDrugsNextAddress_Intent_Extra,intent);
            case MainPageView.SETTINGS_ACTIVITY:
                intent.putExtra("PAGE",SettingsView.MAIN);
                return intent;
            case MainPageView.ADDRESSES_ACTIVITY:
                intent.putExtra("PAGE",SettingsView.ADDRESSES);
                return intent;

        }
        return intent;
    }

    @Override
    public Object getActivity() {
        return view.getActivity();
    }

    @Override
    public void onUserNameCollected(String username) {
        view.updateDrawerName(username);
    }

    @Override
    public void onUserMailCollected(String mail) {
        view.updateDrawerMail(mail);
    }

    @Override
    public void onUserPhotoCollected(Uri photo) {
        view.updateDrawerPhoto(photo);
    }

    @Override
    public void onCloseupRequest() {
        view.closeApp();
    }

    @Override
    public void onPermissionSettingsCall() {
        view.launchActivity(view.SYSTEM_SETTINGS_PERMISSIONS_APPLICATION);
    }

    @Override
    public void onExitRequest() {
        view.exitApp();
    }

    @Override
    public void onActivityLaunchRequest(int i) {
        view.stopcheckInternetFunctionality();
        view.launchActivity(i);
    }

    @Override
    public void onCheckAndSetup() {
        interactor.getLoggedUserName(this);
        interactor.getLoggedUserEmail(this);
        interactor.getLoggedUserUriPhoto(this);
        interactor.loadUserSettings(this);
        LoginIsActive=false;
        view.checkPermissions();
        view.checkInternetFunctionality();
    }

    @Override
    public void onSettingsReady(String message) {
        try {
            view.unlockScreenLoading(message);
        }catch (Exception e){}
    }


}

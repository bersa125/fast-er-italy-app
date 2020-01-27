package com.presentation_layer.fasteritaly.interactor;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.business_logic.fasteritaly.asynctask.SettingsLoader;
import com.example.fasteritaly.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;

import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_CANCELED;

public class MainPageInteractor {

    private SettingsLoader SettingsTask=null;
    private boolean SettingsLaunched=false;

    public interface onMainPageActivityInteractionListener{
        //Interface of the main page exposed actions
        void onScreenChange();
        void onDisconnectRequest(int type);
        void onCloseupRequest();
        void onPermissionSettingsCall();
        void onExitRequest();
        void onActivityLaunchRequest(int i);
        void onCheckAndSetup();
        void onSettingsReady(String message);
        Intent onIntentCreation(Intent intent, int request);//Adds Automatically extra information to the intent;

        Object getActivity();

        //Responses from business
        void onUserNameCollected(String username);
        void onUserMailCollected(String mail);
        void onUserPhotoCollected(Uri photo);
    }

    //Get model's informations
    public void getLoggedUserName(onMainPageActivityInteractionListener listener){
        if(CommonAccessData.getInstance().getCurrentFirebaseUser() !=null)
            listener.onUserNameCollected(CommonAccessData.getInstance().getCurrentFirebaseUser().getDisplayName());
        else
            listener.onDisconnectRequest(RESULT_CANCELED);
    }
    public void getLoggedUserEmail(onMainPageActivityInteractionListener listener){
        if(CommonAccessData.getInstance().getCurrentFirebaseUser()!=null)
            listener.onUserMailCollected(CommonAccessData.getInstance().getCurrentFirebaseUser().getEmail());
        else
            listener.onDisconnectRequest(RESULT_CANCELED);
    }
    public void getLoggedUserUriPhoto(onMainPageActivityInteractionListener listener){
        if(CommonAccessData.getInstance().getCurrentFirebaseUser()!=null)
            listener.onUserPhotoCollected(CommonAccessData.getInstance().getCurrentFirebaseUser().getPhotoUrl());
        else
            listener.onDisconnectRequest(RESULT_CANCELED);
    }
    public void loadUserSettings(final onMainPageActivityInteractionListener listener){
        if(!SettingsLaunched){
            SettingsLaunched=true;
            try {
                CommonAccessData.getInstance().getCurrentFirebaseUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                    @Override
                    public void onSuccess(GetTokenResult getTokenResult) {
                        //Launch the request to the server
                        SettingsTask =new SettingsLoader(listener, getTokenResult.getToken());
                        SettingsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                });
            } catch (Exception e) {
                listener.onSettingsReady(((Activity) listener.getActivity()).getString(R.string.auth_error));
            }
        }else{
            if(this.SettingsTask!=null){
                try {
                    if (SettingsTask.get(100, TimeUnit.MILLISECONDS) != null){
                        SettingsTask=null;
                        try {
                            CommonAccessData.getInstance().getCurrentFirebaseUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                                @Override
                                public void onSuccess(GetTokenResult getTokenResult) {
                                    //Launch the request to the server
                                    SettingsTask =new SettingsLoader(listener, getTokenResult.getToken());
                                    SettingsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            });
                        } catch (Exception e) {
                            listener.onSettingsReady(((Activity) listener.getActivity()).getString(R.string.auth_error));
                        }
                    }
                }catch(Exception e){ listener.onSettingsReady(null);}
            }
        }

    }
    //Save firebase informations
    public void saveCurrentFirebaseUser(onMainPageActivityInteractionListener listener, FirebaseUser currentUser) {
        CommonAccessData.getInstance().setCurrentUser(currentUser);
        listener.onCheckAndSetup();
    }

}

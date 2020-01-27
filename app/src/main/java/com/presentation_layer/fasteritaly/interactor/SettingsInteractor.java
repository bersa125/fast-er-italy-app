package com.presentation_layer.fasteritaly.interactor;

import android.os.AsyncTask;
import android.os.Bundle;

import com.business_logic.fasteritaly.asynctask.SettingsAddressManipulator;
import com.business_logic.fasteritaly.asynctask.SettingsUpdater;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.GetTokenResult;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.presentation_layer.fasteritaly.view.SettingsView;

import java.util.List;

public class SettingsInteractor {
    public interface onSettingsActivityInteractionListener{

        void changeContent(int choice);

        void onSettingsUpdate();
        void onSettingsModification(Bundle newSettings);
        void onSettingsViewUpdate(Bundle settings);

        void onAddressModify(String newAddress, int oldposition);
        void onAddressModified(String newAddress, int oldposition);
        void onAddressInsertion(String address);
        void onAddressInserted(String address);
        void onAddressDelete(int position);
        void onAddressDeleted(int position);
        void onAddressUpdate();
        void onAddressViewUpdate(List<String> addresses);

        void onResultMessage(int result);

        Object getActivityContext();

    }

    public void getSettings(onSettingsActivityInteractionListener listener){
        Bundle sets=new Bundle();
        sets.putInt("RESULTS_SHOWED", CommonAccessData.getInstance().getResultsShown());
        sets.putBoolean("TRACKING",CommonAccessData.getInstance().isTrackPosition());
        listener.onSettingsViewUpdate(sets);
    }
    public void updateSettings(final onSettingsActivityInteractionListener listener, final Bundle newSettings){
        try{
            CommonAccessData.getInstance().getCurrentFirebaseUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {
                    //Launch the request to the server
                    new SettingsUpdater(listener,getTokenResult.getToken(),newSettings.getInt("RESULTS_SHOWED"),newSettings.getBoolean("TRACKING")).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }catch(Exception e){listener.onResultMessage(SettingsView.ERROR);}
    }


    public void getAddresses(final onSettingsActivityInteractionListener listener){
        try{
            CommonAccessData.getInstance().getCurrentFirebaseUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {
                    //Launch the request to the server
                    new SettingsAddressManipulator(listener,getTokenResult.getToken()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }catch(Exception e){listener.onResultMessage(SettingsView.ERROR);}
    }
    public void modifyAddress(final onSettingsActivityInteractionListener listener, final String newAddress, final String oldAddress, final int oldposition){
        try{
            CommonAccessData.getInstance().getCurrentFirebaseUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {
                    //Launch the request to the server
                    new SettingsAddressManipulator(listener,getTokenResult.getToken(),oldAddress,newAddress,oldposition).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }catch(Exception e){listener.onResultMessage(SettingsView.ERROR);}
    }
    public void addAddress(final onSettingsActivityInteractionListener listener, final String newAddress){//Questo comunica con il backend direttamente
        try{
            CommonAccessData.getInstance().getCurrentFirebaseUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {
                    //Launch the request to the server
                    new SettingsAddressManipulator(listener,getTokenResult.getToken(),newAddress).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }catch(Exception e){listener.onResultMessage(SettingsView.ERROR);}
    }
    public void deleteAddress(final onSettingsActivityInteractionListener listener, final String oldAddress, final int oldposition){
        try{
            CommonAccessData.getInstance().getCurrentFirebaseUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {
                    //Launch the request to the server
                    new SettingsAddressManipulator(listener,getTokenResult.getToken(),oldAddress,oldposition).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }catch(Exception e){listener.onResultMessage(SettingsView.ERROR);}
    }
}

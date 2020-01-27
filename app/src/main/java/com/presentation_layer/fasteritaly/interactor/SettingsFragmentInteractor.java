package com.presentation_layer.fasteritaly.interactor;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.business_logic.fasteritaly.asynctask.AsyncGeocodingForSettingsSearch;
import java.util.List;

public class SettingsFragmentInteractor {

    private  AsyncGeocodingForSettingsSearch actualSearch;
    public interface onSettingsFragmentInteractionListener{

        //only on init
        void changeAspect(int choice);

        void onChangeAspect(int choice);
        void onAddressImmission(String searched);
        void onAddressResults(List<String> results);

        void onSettingsUpdate();
        void onSettingsModify(Bundle newSettings);
        void onAddressModify(String newAddress, int oldposition);
        void onAddressInsertion(String address);
        void onAddressDelete(int position);
        void onAddressUpdate();
    }

    public void findPlaces(onSettingsFragmentInteractionListener listener,Object context, String address){
        if(actualSearch==null) {
            actualSearch = new AsyncGeocodingForSettingsSearch(listener, address, (Context) context);
            actualSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            actualSearch.cancel(true);
            actualSearch = new AsyncGeocodingForSettingsSearch(listener, address, (Context) context);
            actualSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

}

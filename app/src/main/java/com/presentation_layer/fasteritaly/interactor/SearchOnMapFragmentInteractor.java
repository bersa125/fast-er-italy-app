package com.presentation_layer.fasteritaly.interactor;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import com.business_logic.fasteritaly.asynctask.AsyncGeocodingForCenterPoint;
import com.business_logic.fasteritaly.asynctask.FindNearPlaces;
import com.google.android.gms.maps.model.LatLng;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;

import java.util.LinkedList;
import java.util.List;

public class SearchOnMapFragmentInteractor {

    public interface onSearchOnMapFragmentInteractionListener{
        //Calls the async computation to find markers
        void onCentralLocationSet();
        //Passes the central point informations to correctly find informations
        void onCentralLocationSearchRequest(Object toConvert);
        void onSetCentralLocation(LatLng coordinates,String address);
        //How to react on events
        void onSetSelectedItem(Bundle data);
        void onSelectedItem(Bundle data);
        void onItemAdd(List<Bundle> data);
        void onLoadingStatus(boolean status,boolean small);
        //getContext from view
        Context getViewContext();
        LatLng getCurrentPosition();
    }


    public void getCorrectViewPosition(onSearchOnMapFragmentInteractionListener listener,String address){
        new AsyncGeocodingForCenterPoint(listener,address).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);;
    }
    public void getCorrectViewPosition(onSearchOnMapFragmentInteractionListener listener,Location coordinates){
        new AsyncGeocodingForCenterPoint(listener,coordinates).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}

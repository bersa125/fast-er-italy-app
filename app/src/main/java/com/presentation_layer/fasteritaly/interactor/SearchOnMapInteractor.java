package com.presentation_layer.fasteritaly.interactor;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.business_logic.fasteritaly.asynctask.FindNearPlaces;
import com.business_logic.fasteritaly.asynctask.OnMapAddressManipulator;
import com.business_logic.fasteritaly.service.Tracking_Service;
import com.business_logic.fasteritaly.util.NetworkHTTPRequester;
import com.example.fasteritaly.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.GetTokenResult;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.presentation_layer.fasteritaly.view.SearchOnMapView;

import java.util.LinkedList;
import java.util.List;

public class SearchOnMapInteractor {

    private FindNearPlaces searchTask;
    private LatLng coordinates;
    private boolean currentType=false;

    public SearchOnMapInteractor(){}

    public interface onSearchOnMapInteractionListener{
        int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};

        void onSetAddresses(String no_address_found, String choose_an_address,String[] previous);
        void onAddressesTaken(String[] addresses);
        void onTabsSetup();
        void onChildRequest(boolean type,LatLng coordinates, String address);


        void updateChildFragmentsOnPosition(int SpinnerItemID);
        void updateChildFragmentsOnSelectedPoint(Bundle object);
        void updateChildOnLoading(boolean status,boolean small);
        void updateChildOnResults(List<Bundle> objects);
        LatLng getCurrentPosition();

        //Controls the launch of new Intents
        void onActionRequest(int i, @Nullable Bundle object);

        void launchService();

        Context getViewContext();
        boolean isOnAddress();
    }

    public void launchBackgroundControl(onSearchOnMapInteractionListener listener){//Tries to launch the background tab (notification), based on the settings
        if(!this.isTrackingServiceRunning(listener,Tracking_Service.class) && CommonAccessData.getInstance().isTrackPosition() && !listener.isOnAddress() && ContextCompat.checkSelfPermission(listener.getViewContext(), Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED)
            listener.getViewContext().startService(new Intent(listener.getViewContext(), Tracking_Service.class));
        else{
            if(ContextCompat.checkSelfPermission(listener.getViewContext(), Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) listener.getViewContext(), Manifest.permission.FOREGROUND_SERVICE)) {
                    ActivityCompat.requestPermissions((Activity) listener.getViewContext(),
                            new String[]{Manifest.permission.FOREGROUND_SERVICE},
                            SearchOnMapView.REQUEST_PERMISSION_FOREGROUND_SERVICE);
                }
            }
        }

    }

    public void getCurrentAccountSavedAdresses(final onSearchOnMapInteractionListener listener, final String no_address_found, final String choose_an_address, final String[] previous){
        try{
            CommonAccessData.getInstance().getCurrentFirebaseUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {
                    //Launch the request to the server
                    new OnMapAddressManipulator(listener,getTokenResult.getToken(),no_address_found,choose_an_address,previous).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }catch(Exception e){
            List<String> AddressData=new LinkedList<>();
            AddressData.add(no_address_found);
            listener.onAddressesTaken(AddressData.toArray(new String[AddressData.size()]));
        }
    }

    public void getNearest_ERs(final onSearchOnMapInteractionListener listener, final LatLng coordinates, final String address){
        if(currentType){
            searchTask=null;
            currentType=false;
        }
        if(searchTask==null) {
            searchTask = new FindNearPlaces(listener, listener.getViewContext(), FindNearPlaces.placeHospital, this.coordinates, address, CommonAccessData.getInstance().getResultsShown());
            this.coordinates=coordinates;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }catch (Exception e){
                        try {
                            searchTask.cancel(true);
                        }catch (Exception es){}
                        try{
                            searchTask = new FindNearPlaces(listener, listener.getViewContext(), FindNearPlaces.placeHospital, coordinates, address, CommonAccessData.getInstance().getResultsShown());
                            searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }catch (Exception es){}
                    }
                }
            }, 100);
        }else{
            if(this.coordinates!=null){
                if(!this.coordinates.equals(coordinates)) {
                    searchTask.cancel(true);
                    this.coordinates = coordinates;
                    searchTask = new FindNearPlaces(listener, listener.getViewContext(), FindNearPlaces.placeHospital, this.coordinates, address, CommonAccessData.getInstance().getResultsShown());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } catch (Exception e) {
                                try {
                                    searchTask.cancel(true);
                                }catch (Exception es){}
                                try{
                                    searchTask = new FindNearPlaces(listener, listener.getViewContext(), FindNearPlaces.placeHospital, coordinates, address, CommonAccessData.getInstance().getResultsShown());
                                    searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }catch (Exception es){}

                            }
                        }
                    }, 100);
                }else{
                    this.coordinates=null;
                }
            }else{
                searchTask.cancel(true);
                this.coordinates = coordinates;
                searchTask = new FindNearPlaces(listener, listener.getViewContext(), FindNearPlaces.placeHospital, this.coordinates, address, CommonAccessData.getInstance().getResultsShown());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } catch (Exception e) {
                            try {
                                searchTask.cancel(true);
                            }catch (Exception es){}
                            try{
                                searchTask = new FindNearPlaces(listener, listener.getViewContext(), FindNearPlaces.placeHospital, coordinates, address, CommonAccessData.getInstance().getResultsShown());
                                searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }catch (Exception es){}
                        }
                    }
                }, 100);
            }
        }
    }
    public void getNearest_Drugstores(final onSearchOnMapInteractionListener listener, final LatLng coordinates, final String address){

        if(!currentType){
            searchTask=null;
            currentType=true;
        }
        if(searchTask==null) {
            searchTask = new FindNearPlaces(listener, listener.getViewContext(), FindNearPlaces.placePharmacy, this.coordinates, address, CommonAccessData.getInstance().getResultsShown());
            this.coordinates=coordinates;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }catch (Exception e){
                        try {
                            searchTask.cancel(true);
                        }catch (Exception es){}
                        try{
                            searchTask = new FindNearPlaces(listener, listener.getViewContext(), FindNearPlaces.placePharmacy, coordinates, address, CommonAccessData.getInstance().getResultsShown());
                            searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }catch (Exception es){}
                    }
                }
            }, 100);
        }else{
            if(this.coordinates!=null) {
                if (!this.coordinates.equals(coordinates)) {
                    searchTask.cancel(true);
                    this.coordinates = coordinates;
                    searchTask = new FindNearPlaces(listener, listener.getViewContext(), FindNearPlaces.placePharmacy, this.coordinates, address, CommonAccessData.getInstance().getResultsShown());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } catch (Exception e) {
                                try {
                                    searchTask.cancel(true);
                                }catch (Exception es){}
                                try{
                                    searchTask = new FindNearPlaces(listener, listener.getViewContext(), FindNearPlaces.placePharmacy, coordinates, address, CommonAccessData.getInstance().getResultsShown());
                                    searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }catch (Exception es){}
                            }
                        }
                    }, 100);
                }else{
                    this.coordinates=null;
                }
            }else{
                searchTask.cancel(true);
                this.coordinates = coordinates;
                searchTask = new FindNearPlaces(listener, listener.getViewContext(), FindNearPlaces.placePharmacy, this.coordinates, address, CommonAccessData.getInstance().getResultsShown());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } catch (Exception e) {
                            try {
                                searchTask.cancel(true);
                            }catch (Exception es){}
                            try{
                                searchTask = new FindNearPlaces(listener, listener.getViewContext(), FindNearPlaces.placePharmacy, coordinates, address, CommonAccessData.getInstance().getResultsShown());
                                searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }catch (Exception es){}
                        }
                    }
                }, 100);
            }
        }
    }

    public void stopTasks() {
        if(searchTask!=null){
            searchTask.cancel(true);
            coordinates=null;
            searchTask=null;
        }
    }

    private boolean isTrackingServiceRunning(onSearchOnMapInteractionListener listener,Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) listener.getViewContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}

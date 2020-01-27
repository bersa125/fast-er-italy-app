package com.presentation_layer.fasteritaly.presenter;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.business_logic.fasteritaly.util.NetworkHTTPRequester;
import com.google.android.gms.maps.model.LatLng;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.presentation_layer.fasteritaly.interactor.SearchOnMapInteractor;
import com.presentation_layer.fasteritaly.view.SearchOnMapFragmentView;
import com.presentation_layer.fasteritaly.view.SearchOnMapView;

import java.util.List;

import static com.presentation_layer.fasteritaly.view.SearchOnMapView.CALL_ADD_NEW_ADDRESS;
import static com.presentation_layer.fasteritaly.view.SearchOnMapView.CALL_GOOGLE_MAPS_INTENT;
import static com.presentation_layer.fasteritaly.view.SearchOnMapView.CALL_LOCATION_INFORMATIONS;

public class SearchOnMapPresenter implements SearchOnMapInteractor.onSearchOnMapInteractionListener {

    private SearchOnMapView view;
    private SearchOnMapInteractor interactor;

    public SearchOnMapPresenter(SearchOnMapView v, SearchOnMapInteractor i){
        view=v;
        interactor=i;
    }

    public void onCreate(){
        view.checkPermissions();
        view.launchGPSRecognition();
        view.checkInternetFunctionality();
        view.setCorrectView(view.getType(),view.getOnAddress());
        onTabsSetup();
    }
    public void onResume(int SpinnerItem) {
        view.setCorrectView(view.getType(),view.getOnAddress());
        view.launchGPSRecognition();
        view.checkPermissions();
        view.checkInternetFunctionality();
        //updateChildFragmentsOnPosition(SpinnerItem);
        //updateChildFragmentsOnSelectedPoint(null);
    }
    public void onBackPressed(){
        //CommonAccessData.getInstance().setAddressInSearch(null);
        interactor.stopTasks();
        view.stopGPSRecognition();
        view.stopcheckInternetFunctionality();
    }
    public void onDestroy() {
        interactor.stopTasks();
        view.stopGPSRecognition();
        view.stopcheckInternetFunctionality();
    }

    @Override
    public void onSetAddresses(String no_address_found, String choose_an_address, String[] previous) {
        interactor.getCurrentAccountSavedAdresses(this,no_address_found, choose_an_address, previous);
    }

    @Override
    public void onAddressesTaken(String[] addresses) {
        view.setRegisteredAddresses(addresses);
    }

    @Override
    public void onTabsSetup() {
        view.setTabbedViewAdapter(this.TAB_TITLES);
    }

    @Override
    public void onChildRequest(boolean type, LatLng coordinates, String address) {
        if(!type){
            interactor.getNearest_ERs(this,coordinates,address);
        }else{
            interactor.getNearest_Drugstores(this,coordinates,address);
        }
    }

    @Override
    public void updateChildFragmentsOnPosition(int SpinnerItemID) {
        if(view!=null){
            for (Object i : view.getActiveFragments()) {
                if (i instanceof SearchOnMapFragmentView) {
                    if(SpinnerItemID==0){
                        ((SearchOnMapFragmentView) i).clearResults();
                    }else {
                        if (view.getOnAddress())
                            ((SearchOnMapFragmentView) i).setCenterPosition(view.getCurrentAddress());
                        else
                            ((SearchOnMapFragmentView) i).setCenterPosition(view.getCurrentGPSPosition());
                    }
                }
            }
        }
    }

    @Override
    public void updateChildFragmentsOnSelectedPoint(Bundle object) {
        if(view!=null){
            for (Object i : view.getActiveFragments()) {
                if (i instanceof SearchOnMapFragmentView) {
                    ((SearchOnMapFragmentView) i).onItemSelected(object);
                }
            }
        }
    }

    @Override
    public void updateChildOnLoading(boolean status, boolean small) {
        if(view!=null){
            for (Object i : view.getActiveFragments()) {
                if (i instanceof SearchOnMapFragmentView) {
                    ((SearchOnMapFragmentView) i).showLoadingBar(status,small);
                }
            }
        }
    }

    @Override
    public void updateChildOnResults(List<Bundle> objects) {
        if(view!=null){
            for (Object i : view.getActiveFragments()) {
                if (i instanceof SearchOnMapFragmentView) {
                    ((SearchOnMapFragmentView) i).addResults(objects);
                }
            }
        }
    }

    @Override
    public LatLng getCurrentPosition() {
        if(view!=null){
            for (Object i : view.getActiveFragments()) {
                if (i instanceof SearchOnMapFragmentView) {
                    return ((SearchOnMapFragmentView) i).getCenterCoordinates();
                }
            }
        }
        return null;
    }


    @Override
    public void onActionRequest(int i, @Nullable Bundle object) {
        switch (i){
            case CALL_LOCATION_INFORMATIONS:
                view.launchDetailsActivity(object);
                break;
            case CALL_ADD_NEW_ADDRESS:
                view.launchAddressActivity();
                break;
            case CALL_GOOGLE_MAPS_INTENT:
                interactor.launchBackgroundControl(this);
                view.launchGoogleMapsIntent(object);
                break;
        }
    }

    @Override
    public void launchService() {
        interactor.launchBackgroundControl(this);
    }

    @Override
    public Context getViewContext() {
        return (Context)view.getContext();
    }

    @Override
    public boolean isOnAddress() {
        if(view!=null){
            return view.getOnAddress();
        }
        return false;
    }


}

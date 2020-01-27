package com.presentation_layer.fasteritaly.presenter;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.presentation_layer.fasteritaly.interactor.SearchOnMapFragmentInteractor;
import com.presentation_layer.fasteritaly.view.SearchOnMapFragmentView;
import com.presentation_layer.fasteritaly.view.SearchOnMapView;

import java.util.List;

public class SearchOnMapFragmentPresenter  implements SearchOnMapFragmentInteractor.onSearchOnMapFragmentInteractionListener {

    private SearchOnMapFragmentView view;
    private SearchOnMapFragmentInteractor interactor;

    public SearchOnMapFragmentPresenter(SearchOnMapFragmentView v, SearchOnMapFragmentInteractor i){
        view=v;
        interactor=i;
    }

    public void onDetach() {
        view.clearResults();
        view=null;
    }

    @Override
    public void onCentralLocationSet() {
        if(view.getAttachedView()!=null){
            if(view.getAttachedView() instanceof SearchOnMapView) {
                SearchOnMapView activity=(SearchOnMapView) view.getAttachedView();
                activity.requestCheckForNearAddresses(activity.getType(),view.getCenterCoordinates(), view.getCenterAddress());
            }
        }
    }

    @Override
    public void onCentralLocationSearchRequest(Object toConvert) {
        if(toConvert instanceof String){
            interactor.getCorrectViewPosition(this,(String) toConvert);
        }else if (toConvert instanceof Location){
            interactor.getCorrectViewPosition(this, (Location) toConvert);
        }
    }

    @Override
    public void onSetCentralLocation(LatLng coordinates, String address) {
        try {
            view.clearResults();
            view.setCenterPosition(coordinates, address);
        }catch (Exception e){}
    }

    @Override
    public void onSetSelectedItem(Bundle data) {
        if(view.getAttachedView()!=null) {
            if (view.getAttachedView() instanceof SearchOnMapView) {
                ((SearchOnMapView) view.getAttachedView()).setSelectedItem(data);
            }
        }
    }

    @Override
    public void onSelectedItem(Bundle data) {
        if(view.getAttachedView()!=null) {
            if (view.getAttachedView() instanceof SearchOnMapView) {
                if(data!=null) {
                    data.putString("CURRENT_ADDRESS", view.getCenterAddress());
                    data.putDouble("CURRENT_LAT", view.getCenterCoordinates().latitude);
                    data.putDouble("CURRENT_LONG", view.getCenterCoordinates().longitude);
                }
                ((SearchOnMapView)view.getAttachedView()).launchChooseActionDialog(data);
            }
        }
    }

    @Override
    public void onItemAdd(List<Bundle> data) {
        if(view!=null)
            view.addResults(data);
    }

    @Override
    public void onLoadingStatus(boolean status, boolean small) {
        if(view!=null){
            view.showLoadingBar(status,small);
        }
    }

    @Override
    public Context getViewContext() {
        if(view!=null)
            return (Context) view.getAttachedView();
        else return null;
    }

    @Override
    public LatLng getCurrentPosition() {
        if(view!=null){
            return view.getCenterCoordinates();
        }
        return null;
    }


}

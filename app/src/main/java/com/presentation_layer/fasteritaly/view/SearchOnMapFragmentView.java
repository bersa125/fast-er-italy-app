package com.presentation_layer.fasteritaly.view;

import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SearchOnMapFragmentView {

    //Removes all previous added results
    void clearResults();
    //Adds all he informations in order to add a new result
    void addResult(Bundle data);
    void addResults(List<Bundle> data);
    //Set the position as the center one
    void setCenterPosition(String address);
    void setCenterPosition(Location coordinates);
    void setCenterPosition(LatLng coordinates, String address);

    void onItemSelected(Bundle object);
    void showLoadingBar(boolean status,boolean small);

    LatLng getCenterCoordinates();
    String getCenterAddress();
    //Object to be the more general as possible
    Object getAttachedView();

}

package com.presentation_layer.fasteritaly.view;

import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface SearchOnMapView {

    static int CALL_LOCATION_INFORMATIONS=1;
    static int CALL_GOOGLE_MAPS_INTENT=2;
    static int CALL_ADD_NEW_ADDRESS=3;

    int REQUEST_PERMISSION_FINE_LOCATION=1;
    int REQUEST_PERMISSION_FOREGROUND_SERVICE = 2;

    void setCorrectView(boolean searchType, boolean isAddressUsed);
    void setRegisteredAddresses(String[] addresses);
    void setTabbedViewAdapter(int[] Tabs);
    void launchDetailsActivity(Bundle Object); //ObjectType : true if E.R., false if Drugstore
    void launchGoogleMapsIntent(Bundle Object);//To define
    void launchAddressActivity();

    void launchChooseActionDialog(Bundle object);
    void setSelectedItem(Bundle object);

    String getCurrentAddress();

    void launchGPSRecognition();
    void stopGPSRecognition();
    Location getCurrentGPSPosition();

    List<Object> getActiveFragments();

    boolean getType();
    boolean getOnAddress();
    Object getContext();

    void checkPermissions();
    void checkInternetFunctionality();
    void stopcheckInternetFunctionality();

    void requestCheckForNearAddresses(boolean type, LatLng coordinates, String address);

}

package com.business_logic.fasteritaly.asynctask;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.presentation_layer.fasteritaly.interactor.SearchOnMapFragmentInteractor;

import java.util.List;

public class AsyncGeocodingForCenterPoint extends AsyncTask<Void, Void, Object[]> {

    private SearchOnMapFragmentInteractor.onSearchOnMapFragmentInteractionListener listener;
    private Geocoder coder;

    private Location location;
    private String address;

    public AsyncGeocodingForCenterPoint(SearchOnMapFragmentInteractor.onSearchOnMapFragmentInteractionListener l, String address){
        listener=l;
        this.address=address;
    }

    public AsyncGeocodingForCenterPoint(SearchOnMapFragmentInteractor.onSearchOnMapFragmentInteractionListener l, Location address){
        listener=l;
        location=address;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object[] doInBackground(Void... voids) {
        LatLng restL;
        String add;
        if(address!=null){
            restL=getLatLngFromAddress(address,listener.getViewContext(),3);
            add=address;
        }else{
            restL=new LatLng(location.getLatitude(),location.getLongitude());
            add=getAddressFromLatLng(restL,listener.getViewContext(),3);
        }
        return new Object[]{restL,add};
    }

    @Override
    protected void onPostExecute(Object[] result){
        listener.onSetCentralLocation((LatLng) result[0],(String)result[1]);
        super.onPostExecute(result);
    }


    private String getAddressFromLatLng(LatLng coordinates, Context ctx, int tentatives){

        if(coder==null && ctx!=null)
            coder = new Geocoder(ctx);
        List<Address> address;
        try {
            address = coder.getFromLocation(coordinates.latitude,coordinates.longitude,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            //location.getThoroughfare() = nome via
            //location.getLocality() =  nome comune
            //location.getAdminArea() = nome regione
            //location.getSubAdminArea()= provincia di nomeprovincia
            //location.getFeatureName() = numero civico
            return location.getThoroughfare()+" "+location.getFeatureName()+", "+location.getLocality()+" "+location.getAdminArea();
        }catch(Exception e){
            e.printStackTrace();
            coder=null;
            if(tentatives>0) {
                return getAddressFromLatLng(coordinates, ctx,tentatives-1);
            }
            return null;
        }
    }

    private LatLng getLatLngFromAddress(String strAddress, Context ctx, int tentatives){
        if(coder==null && ctx!=null)
            coder = new Geocoder(ctx);
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();
            return new LatLng((location.getLatitude()/* * 1E6*/), (location.getLongitude() /* * 1E6*/));
        }catch(Exception e){
            e.printStackTrace();
            coder=null;
            if(tentatives>0) {
                return getLatLngFromAddress(strAddress, ctx,tentatives-1);
            }
            return null;
        }
    }
}

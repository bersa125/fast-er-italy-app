package com.business_logic.fasteritaly.asynctask;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.presentation_layer.fasteritaly.interactor.SearchOnMapFragmentInteractor;
import com.presentation_layer.fasteritaly.interactor.SettingsFragmentInteractor;

import java.util.LinkedList;
import java.util.List;

public class AsyncGeocodingForSettingsSearch extends AsyncTask<Void, Void, List<String>> {

    private SettingsFragmentInteractor.onSettingsFragmentInteractionListener listener;
    private boolean executed=false;
    private Geocoder coder;

    private String address;
    private Context context;

    public AsyncGeocodingForSettingsSearch(SettingsFragmentInteractor.onSettingsFragmentInteractionListener l, String address, Context ctx){
        listener=l;
        this.address=address;
        context=ctx;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<String> doInBackground(Void... voids) {
        if(executed)
            return null;
        executed=true;
        return getAddressesFromAddress(address,context,3);
    }

    @Override
    protected void onPostExecute(List<String> result){
        listener.onAddressResults(result);
        super.onPostExecute(result);
    }

    private List<String> getAddressesFromAddress(String strAddress, Context ctx, int tentatives){
        if(coder==null)
            coder = new Geocoder(ctx);
        List<String> res=new LinkedList<>();
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress,10);
            if (address==null) {
                return null;
            }
            for(Address location: address ){
                //location.getThoroughfare() = nome via
                //location.getLocality() =  nome comune
                //location.getAdminArea() = nome regione
                //location.getSubAdminArea()= provincia di nomeprovincia
                //location.getFeatureName() = numero civico
                res.add(location.getAddressLine(0));
            }
        }catch(Exception e){
            e.printStackTrace();
            coder=null;
            if(tentatives>0) {
                return getAddressesFromAddress(strAddress,ctx,tentatives-1);
            }
        }
        return res;
    }
}

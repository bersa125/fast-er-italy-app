package com.business_logic.fasteritaly.asynctask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;

import com.business_logic.fasteritaly.data_helper.SearchPhotoService;
import com.google.android.gms.maps.model.LatLng;
import com.model.fasteritaly.Hospital;
import com.model.fasteritaly.Pharmacy;
import com.model.fasteritaly.singleton_and_helpers.ModelBundleAdapter;
import com.presentation_layer.fasteritaly.interactor.PlaceInfoInteractor;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class PlacePhotoLoader extends AsyncTask <Void, Void, String> {

    private PlaceInfoInteractor.onPlaceInfoInteractionListener listener;
    private boolean executed=false;
    private Bundle object;

    public PlacePhotoLoader(PlaceInfoInteractor.onPlaceInfoInteractionListener l, Bundle object){
        listener=l;
        this.object=object;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        if(executed)
            return null;
        executed=true;
        String res=null;
        if(object.getBoolean("TYPE")){//Pharmacy
            Pharmacy p= ModelBundleAdapter.getPharmacyFromBundle(object);
            res=SearchPhotoService.getInstance().getImageByPlaceCompleteAddress(p.getPlaceName()+" "+p.getAddress().getAddress()+"",listener.getActivityContext());
        }else{
            Hospital h=ModelBundleAdapter.getHospitalFromBundle(object);
            String placeName="Ospedale "+h.getPlaceName()+" entrata";
            if(placeName.contains("Pol. Univ.")){
                placeName="Ospedale "+placeName.substring(("Pol. Univ.").length())+" entrata";
            }
            res=SearchPhotoService.getInstance().getImageByPlaceCompleteAddress(placeName+" "+h.getAddress().getAddress()+"",listener.getActivityContext());
        }
        return res;
    }

    @Override
    protected void onPostExecute(String result){
        if(result!=null){//Aggiungo
            object.putString("PLACE_PHOTO",result);
            listener.onPhotoReady(object);
        }
        super.onPostExecute(result);
    }

    private Bitmap getBitmapFromURL(String imgUrl,int tentatives) {
        try {
            URL url = new URL(imgUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception e) {
            if(tentatives>0)
                return getBitmapFromURL(imgUrl,tentatives-1);
            return null;
        }
    }

    private String getAddressDataFromLatLng(LatLng coordinates, Context context, int tentatives) {//In this case returns the region
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        try {
            address = coder.getFromLocation(coordinates.latitude,coordinates.longitude,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            return location.getLocality()+" "+location.getAdminArea();
        }catch(Exception e){
            if(tentatives>0)
                return getAddressDataFromLatLng(coordinates, context, tentatives-1);
            return null;
        }
    }
}

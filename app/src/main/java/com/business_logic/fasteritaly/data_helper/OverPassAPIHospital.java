package com.business_logic.fasteritaly.data_helper;


import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OverPassAPIHospital {

    private String nome;
    private double longitude;
    private double latitude;



    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }



    private OverPassAPIHospital(){}

    public static OverPassAPIHospital dataToContainer(JSONObject data) throws JSONException {
        OverPassAPIHospital result=new OverPassAPIHospital();
        result.setLatitude(Double.parseDouble(data.getString("lat")));
        result.setLongitude(Double.parseDouble(data.getString("lon")));
        JSONObject inner=data.getJSONObject("tags");
        result.setNome(inner.getString("name"));
        try {
            if(inner.getString("emergency").equals("no")){
                result.latitude=0;
            }
        }catch (Exception e){}
        if(result.latitude==0 || result.longitude==0){
            throw new RuntimeException();
        }
        return result;
    }

    public static OverPassAPIHospital tryDataToContainer(String node, JSONArray lat_lon_array, JSONObject data) throws JSONException {
        OverPassAPIHospital result=new OverPassAPIHospital();
        JSONObject inner=data.getJSONObject("tags");
        result.setNome(inner.getString("name"));
        for(int i=0;i<lat_lon_array.length();i++){
            JSONObject coo=(JSONObject) lat_lon_array.get(i);
            if(coo.getString("id").equals(node)){
                result.setLatitude(Double.parseDouble(coo.getString("lat")));
                result.setLongitude(Double.parseDouble(coo.getString("lon")));
                break;
            }
        }
        try {
            if(inner.getString("emergency").equals("no")){
                result.latitude=0;
            }
        }catch (Exception e){}
        if(result.latitude==0 || result.longitude==0){
            throw new RuntimeException();
        }
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof OverPassAPIHospital){
            OverPassAPIHospital a=(OverPassAPIHospital) obj;
            return a.nome.equals(this.nome) && a.longitude==this.longitude && a.latitude==this.latitude;
        }
        return false;
    }
}

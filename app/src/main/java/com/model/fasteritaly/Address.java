package com.model.fasteritaly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Address extends RealmObject {
    @PrimaryKey
    private String address;

    private double latitude;
    private double longitude;

    public Address(){
    }

    public Address(String a, double lat, double longi){
        address=a;
        latitude=lat;
        longitude=longi;
    }

    public Address(String a, LatLng coo){
        address=a;
        latitude=coo.latitude;
        longitude=coo.longitude;
    }

    public String getAddress(){
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LatLng getCoordinates(){
        return new LatLng(latitude,longitude);
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @NonNull
    @Override
    public String toString() {
        return address+" "+latitude+" "+longitude;
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Address){
            Address a=(Address) obj;
            return a.address.equals(this.address)&&a.latitude==this.latitude&&a.longitude==this.longitude;
        }
        return false;
    }
}

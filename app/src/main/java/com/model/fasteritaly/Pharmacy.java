package com.model.fasteritaly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Pharmacy {

    private String placeName;

    private boolean open=false;
    private String[] openingTimes=new String[]{"",""};
    private String[] closingTimes=new String[]{"",""};

    private Address address;

    public Pharmacy(){}

    public Pharmacy(String place, Address address){
        placeName=place;
        this.address=address;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setOpeningTimes(String[] openingTimes) {
        this.openingTimes = openingTimes;
    }

    public void setClosingTimes(String[] closingTimes) {
        this.closingTimes = closingTimes;
    }

    public String[] getOpeningTimes() {
        return openingTimes;
    }

    public String[] getClosingTimes() {
        return closingTimes;
    }

    public boolean isOpen() {
        return open;
    }

    public String getPlaceName() {
        return placeName;
    }

    public Address getAddress() {
        return address;
    }

    public void changeAddress(Address address){
        this.address=address;
    }

    @NonNull
    @Override
    public String toString() {
        return address.toString()+" "+placeName;
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Address){
            Pharmacy a=(Pharmacy) obj;
            return a.address.equals(this.address)&&a.placeName.equals(this.placeName);
        }
        return false;
    }
}

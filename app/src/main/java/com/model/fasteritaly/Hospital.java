package com.model.fasteritaly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Hospital extends RealmObject {
    @PrimaryKey
    private String placeName;

    private Address address;
    private String updateDate=null;

    private int Red_WaitQueue=-1;
    private int Red_TreatQueue=-1;
    private int Red_ObsQueue=-1;

    private int Yellow_WaitQueue=-1;
    private int Yellow_TreatQueue=-1;
    private int Yellow_ObsQueue=-1;

    private int Green_WaitQueue=-1;
    private int Green_TreatQueue=-1;
    private int Green_ObsQueue=-1;

    private int White_WaitQueue=-1;
    private int White_TreatQueue=-1;
    private int White_ObsQueue=-1;

    private int NonExec_WaitQueue=-1;
    private int NonExec_TreatQueue=-1;
    private int NonExec_ObsQueue=0;

    public Hospital(){}

    public Hospital(String place, Address address){
        this.placeName=place;
        this.address=address;
    }

    public String getPlaceName() {
        return placeName;
    }

    public Address getAddress() {
        return address;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public int getRed_WaitQueue() {
        return Red_WaitQueue;
    }

    public int getRed_TreatQueue() {
        return Red_TreatQueue;
    }

    public int getRed_ObsQueue() {
        return Red_ObsQueue;
    }

    public int getYellow_WaitQueue() {
        return Yellow_WaitQueue;
    }

    public int getYellow_TreatQueue() {
        return Yellow_TreatQueue;
    }

    public int getYellow_ObsQueue() {
        return Yellow_ObsQueue;
    }

    public int getGreen_WaitQueue() {
        return Green_WaitQueue;
    }

    public int getGreen_TreatQueue() {
        return Green_TreatQueue;
    }

    public int getGreen_ObsQueue() {
        return Green_ObsQueue;
    }

    public int getWhite_WaitQueue() {
        return White_WaitQueue;
    }

    public int getWhite_TreatQueue() {
        return White_TreatQueue;
    }

    public int getWhite_ObsQueue() {
        return White_ObsQueue;
    }

    public int getNonExec_WaitQueue() {
        return NonExec_WaitQueue;
    }

    public int getNonExec_TreatQueue() {
        return NonExec_TreatQueue;
    }

    public int getNonExec_ObsQueue() {
        return NonExec_ObsQueue;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public void setRed_WaitQueue(int red_WaitQueue) {
        Red_WaitQueue = red_WaitQueue;
    }

    public void setRed_TreatQueue(int red_TreatQueue) {
        Red_TreatQueue = red_TreatQueue;
    }

    public void setRed_ObsQueue(int red_ObsQueue) {
        Red_ObsQueue = red_ObsQueue;
    }

    public void setYellow_WaitQueue(int yellow_WaitQueue) {
        Yellow_WaitQueue = yellow_WaitQueue;
    }

    public void setYellow_TreatQueue(int yellow_TreatQueue) {
        Yellow_TreatQueue = yellow_TreatQueue;
    }

    public void setYellow_ObsQueue(int yellow_ObsQueue) {
        Yellow_ObsQueue = yellow_ObsQueue;
    }

    public void setGreen_WaitQueue(int green_WaitQueue) {
        Green_WaitQueue = green_WaitQueue;
    }

    public void setGreen_TreatQueue(int green_TreatQueue) {
        Green_TreatQueue = green_TreatQueue;
    }

    public void setGreen_ObsQueue(int green_ObsQueue) {
        Green_ObsQueue = green_ObsQueue;
    }

    public void setWhite_WaitQueue(int white_WaitQueue) {
        White_WaitQueue = white_WaitQueue;
    }

    public void setWhite_TreatQueue(int white_TreatQueue) {
        White_TreatQueue = white_TreatQueue;
    }

    public void setWhite_ObsQueue(int white_ObsQueue) {
        White_ObsQueue = white_ObsQueue;
    }

    public void setNonExec_WaitQueue(int nonExec_WaitQueue) {
        NonExec_WaitQueue = nonExec_WaitQueue;
    }

    public void setNonExec_TreatQueue(int nonExec_TreatQueue) {
        NonExec_TreatQueue = nonExec_TreatQueue;
    }

    public void setNonExec_ObsQueue(int nonExec_ObsQueue) {
        NonExec_ObsQueue = nonExec_ObsQueue;
    }

    public void changeAddress(Address address){
        this.address=address;
    }

    @NonNull
    @Override
    public String toString() {
        return address.toString()+" "+placeName+" "+updateDate;
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Address){
            Hospital a=(Hospital) obj;
            return a.address.equals(this.address)&&a.placeName.equals(this.placeName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.getPlaceName() != null ? this.getPlaceName().hashCode() : 0);
        return hash;
    }

}

package com.model.fasteritaly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserEvaluation extends RealmObject {

    @PrimaryKey
    private String date;
    
    private int waitVote=0;
    private int structVote=0;
    private int serviceVote=0;
    private User user;
    private Hospital hospital;

    public UserEvaluation(){}
    public UserEvaluation(String date, int wV, int sV, int seV, User u, Hospital h){
        this.date=date;
        this.waitVote=wV;
        this.structVote=sV;
        this.serviceVote=seV;
        user=u;
        hospital=h;
    }
    public UserEvaluation(int wV, int sV, int seV, User u, Hospital h){
        this.date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format( new Timestamp(System.currentTimeMillis()));
        this.waitVote=wV;
        this.structVote=sV;
        this.serviceVote=seV;
        user=u;
        hospital=h;
    }
    public UserEvaluation(User u, Hospital h){
        this.date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format( new Timestamp(System.currentTimeMillis()));
        user=u;
        hospital=h;
    }

    public String getDate() {
        return date;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public User getUser() {
        return user;
    }

    public int getWaitVote() {
        return waitVote;
    }

    public int getStructVote() {
        return structVote;
    }

    public int getServiceVote() {
        return serviceVote;
    }

    public void setWaitVote(int waitVote) {
        this.waitVote = waitVote;
    }

    public void setStructVote(int structVote) {
        this.structVote = structVote;
    }

    public void setServiceVote(int serviceVote) {
        this.serviceVote = serviceVote;
    }


    @NonNull
    @Override
    public String toString() {
        return user.toString()+" "+hospital.toString()+" "+date;
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Address){
            UserEvaluation a=(UserEvaluation) obj;
            return a.hospital.equals(this.hospital)&&a.user.equals(this.user)&&a.date.equals(this.date);
        }
        return false;
    }

}

package com.model.fasteritaly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {

    @PrimaryKey
    private String userID;
    private RealmList<Address> registeredAddresses=new RealmList<>();

    public User(){}
    public User(String ID){
        userID=ID;
        registeredAddresses=new RealmList<>();
    }
    public User(String ID, RealmList<Address> addresses){
        userID=ID;
        registeredAddresses=new RealmList<>();
        for(Address address:addresses){
            registeredAddresses.add(address);
        }
    }

    public String getUserID(){
        return  userID;
    }
    public List<Address> getRegisteredAddresses(){
        return registeredAddresses;
    }
    public void addAddress(Address address){
        registeredAddresses.add(address);
    }

    @NonNull
    @Override
    public String toString() {
        return userID;
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Address){
            User a=(User) obj;
            return a.userID.equals(this.userID);
        }
        return false;
    }

}

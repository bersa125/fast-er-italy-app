package com.model.fasteritaly.singleton_and_helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseUser;
import com.model.fasteritaly.Address;
import com.model.fasteritaly.Hospital;
import com.model.fasteritaly.Pharmacy;
import com.model.fasteritaly.User;
import com.model.fasteritaly.UserEvaluation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

public class CommonAccessData {
    private static CommonAccessData ourInstance = new CommonAccessData();

    //Current auth user
    private User user;
    private FirebaseUser currentUser;
    private boolean authHappened = false;
    private String addressInSearch = null;

    private Realm realm;


    //User options
    private int resultsShown = 5;
    private boolean trackPosition = false;


    //Memory representation
    Map<String, Hospital> hospitals = new HashMap<>();
    Map<String, Pharmacy> pharmacies = new HashMap<>();


    public static CommonAccessData getInstance() {
        if (ourInstance == null) {
            ourInstance = new CommonAccessData();
        } else {
            if (ourInstance.hospitals == null) {
                ourInstance.hospitals = new HashMap<>();
            }
            if (ourInstance.pharmacies == null) {
                ourInstance.pharmacies = new HashMap<>();
            }
        }
        return ourInstance;
    }

    private CommonAccessData() {
    }

    //User temporary
    public FirebaseUser getCurrentFirebaseUser() {
        return currentUser;
    }

    public boolean isAuthHappened() {
        return authHappened;
    }

    //Settings
    public boolean isTrackPosition() {
        return trackPosition;
    }

    public int getResultsShown() {
        return resultsShown;
    }

    public void setResultsShown(int resultsShown) {
        this.resultsShown = resultsShown;
    }

    public void setTrackPosition(boolean trackPosition, Activity activity) {
        if (trackPosition) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                this.trackPosition = true;
        } else {
            this.trackPosition = false;
        }
    }

    //Only temporary and static
    public String getAddressInSearch() {
        return addressInSearch;
    }

    public void setAddressInSearch(String addressInSearch) {
        this.addressInSearch = addressInSearch;
    }

    public Hospital getHospitalByName(String placeName) {
        return hospitals.get(placeName);
    }

    public Pharmacy getPharmacyByName(String placeName) {
        return pharmacies.get(placeName);
    }

    public void putHospital(Hospital h) {
        hospitals.put(h.getPlaceName() + h.getAddress().getAddress(), h);
    }

    public void putPharmacy(Pharmacy p) {
        pharmacies.put(p.getPlaceName() + p.getAddress().getAddress(), p);
    }


    //Saved in memory (or mixed)
    public void setCurrentUser(final FirebaseUser currentUser) {
        authHappened = false;
        if(this.currentUser!=null){
            if(!this.currentUser.getUid().equals(currentUser.getUid()))
                deleteUser(this.currentUser.getUid());
        }
        this.currentUser = currentUser;
        if (this.currentUser != null) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm bgRealm) {
                            if (bgRealm.where(User.class).equalTo("userID", currentUser.getUid()).findFirst() == null) {
                                User user = new User(currentUser.getUid());
                                bgRealm.copyToRealmOrUpdate(user);
                                Log.println(Log.INFO, "Realm", "User correctly created");
                            } else {
                                Log.println(Log.INFO, "Realm", "User already in memory");
                            }
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            Log.println(Log.INFO, "Realm", "User correctly instantiated");
                        }
                    });
                }
            });
            authHappened = true;
        }

    }

    public void putFinalHospital(final Hospital h) {

        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm bgRealm) {
                        if (bgRealm.where(Hospital.class).equalTo("address.address", h.getAddress().getAddress()).findFirst() == null) {
                            Address add = bgRealm.where(Address.class).equalTo("address", h.getAddress().getAddress()).and().greaterThan("latitude", 0.0).and().greaterThan("longitude", 0.0).findFirst();
                            if (add == null) {
                                add = new Address(h.getAddress().getAddress(), h.getAddress().getLatitude(), h.getAddress().getLongitude());
                                bgRealm.copyToRealmOrUpdate(add);
                            }
                            Hospital hos = new Hospital(h.getPlaceName(), add);
                            hos.changeAddress(add);
                            hos.setUpdateDate(h.getUpdateDate());
                            hos.setNonExec_WaitQueue(h.getNonExec_WaitQueue());
                            hos.setWhite_WaitQueue(h.getWhite_WaitQueue());
                            hos.setGreen_WaitQueue(h.getGreen_WaitQueue());
                            hos.setYellow_WaitQueue(h.getYellow_WaitQueue());
                            hos.setRed_WaitQueue(h.getRed_WaitQueue());
                            hos.setGreen_ObsQueue(h.getGreen_ObsQueue());
                            hos.setGreen_TreatQueue(h.getGreen_TreatQueue());
                            hos.setNonExec_ObsQueue(h.getNonExec_ObsQueue());
                            hos.setNonExec_TreatQueue(h.getNonExec_TreatQueue());
                            hos.setRed_ObsQueue(h.getRed_ObsQueue());
                            hos.setRed_TreatQueue(h.getRed_TreatQueue());
                            hos.setWhite_ObsQueue(h.getWhite_ObsQueue());
                            hos.setWhite_TreatQueue(h.getWhite_TreatQueue());
                            hos.setYellow_ObsQueue(h.getYellow_ObsQueue());
                            hos.setYellow_TreatQueue(h.getYellow_TreatQueue());
                            bgRealm.copyToRealmOrUpdate(hos);
                            Log.println(Log.INFO, "Realm", "Hospital correctly created");
                        } else {
                            Hospital hos = bgRealm.where(Hospital.class).equalTo("address.address", h.getAddress().getAddress()).findFirst();
                            if(!hos.getPlaceName().equals(h.getPlaceName())){
                                hos.deleteFromRealm();
                                hos=h;
                            }else {
                                hos.setUpdateDate(h.getUpdateDate());
                                hos.setNonExec_WaitQueue(h.getNonExec_WaitQueue());
                                hos.setWhite_WaitQueue(h.getWhite_WaitQueue());
                                hos.setGreen_WaitQueue(h.getGreen_WaitQueue());
                                hos.setYellow_WaitQueue(h.getYellow_WaitQueue());
                                hos.setRed_WaitQueue(h.getRed_WaitQueue());
                                hos.setGreen_ObsQueue(h.getGreen_ObsQueue());
                                hos.setGreen_TreatQueue(h.getGreen_TreatQueue());
                                hos.setNonExec_ObsQueue(h.getNonExec_ObsQueue());
                                hos.setNonExec_TreatQueue(h.getNonExec_TreatQueue());
                                hos.setRed_ObsQueue(h.getRed_ObsQueue());
                                hos.setRed_TreatQueue(h.getRed_TreatQueue());
                                hos.setWhite_ObsQueue(h.getWhite_ObsQueue());
                                hos.setWhite_TreatQueue(h.getWhite_TreatQueue());
                                hos.setYellow_ObsQueue(h.getYellow_ObsQueue());
                                hos.setYellow_TreatQueue(h.getYellow_TreatQueue());
                            }
                            bgRealm.copyToRealmOrUpdate(hos);
                            Log.println(Log.INFO, "Realm", "Hospital already in memory");
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Log.println(Log.INFO, "Realm", "Hospital correctly instantiated");
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

    public void addUserEvaluation(final UserEvaluation e) {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            if (currentUser != null) {
                                User user = realm.where(User.class).equalTo("userID", currentUser.getUid()).findFirst();
                                if (user != null) {
                                    if (e.getUser().equals(user)) {
                                        if (realm.where(UserEvaluation.class).equalTo("user.userID", e.getUser().getUserID()).and().equalTo("hospital.placeName", e.getHospital().getPlaceName()).and().equalTo("date", e.getDate()).findFirst() == null) {
                                            Hospital hos = realm.where(Hospital.class).equalTo("placeName", e.getHospital().getPlaceName()).and().equalTo("address.address", e.getHospital().getAddress().getAddress()).findFirst();
                                            if (hos == null) {
                                                Address add = realm.where(Address.class).equalTo("address", e.getHospital().getAddress().getAddress()).and().greaterThan("latitude", 0).and().greaterThan("longitude", 0).findFirst();
                                                if (add == null) {
                                                    add = new Address(e.getHospital().getAddress().getAddress(), e.getHospital().getAddress().getLatitude(), e.getHospital().getAddress().getLongitude());
                                                    realm.copyToRealmOrUpdate(add);
                                                }
                                                hos = new Hospital(e.getHospital().getPlaceName(), add);
                                                hos.setUpdateDate(e.getHospital().getUpdateDate());
                                                hos.setNonExec_WaitQueue(e.getHospital().getNonExec_WaitQueue());
                                                hos.setWhite_WaitQueue(e.getHospital().getWhite_WaitQueue());
                                                hos.setGreen_WaitQueue(e.getHospital().getGreen_WaitQueue());
                                                hos.setYellow_WaitQueue(e.getHospital().getYellow_WaitQueue());
                                                hos.setRed_WaitQueue(e.getHospital().getRed_WaitQueue());
                                                hos.setGreen_ObsQueue(e.getHospital().getGreen_ObsQueue());
                                                hos.setGreen_TreatQueue(e.getHospital().getGreen_TreatQueue());
                                                hos.setNonExec_ObsQueue(e.getHospital().getNonExec_ObsQueue());
                                                hos.setNonExec_TreatQueue(e.getHospital().getNonExec_TreatQueue());
                                                hos.setRed_ObsQueue(e.getHospital().getRed_ObsQueue());
                                                hos.setRed_TreatQueue(e.getHospital().getRed_TreatQueue());
                                                hos.setWhite_ObsQueue(e.getHospital().getWhite_ObsQueue());
                                                hos.setWhite_TreatQueue(e.getHospital().getWhite_TreatQueue());
                                                hos.setYellow_ObsQueue(e.getHospital().getYellow_ObsQueue());
                                                hos.setYellow_TreatQueue(e.getHospital().getYellow_TreatQueue());
                                                realm.copyToRealmOrUpdate(hos);
                                            }
                                            UserEvaluation eva = new UserEvaluation(e.getDate(), e.getWaitVote(), e.getStructVote(), e.getServiceVote(), user, hos);
                                            realm.copyToRealmOrUpdate(eva);
                                            Log.println(Log.INFO, "Realm", "Vote created");
                                        } else {
                                            UserEvaluation eva = realm.where(UserEvaluation.class).equalTo("user.userID", e.getUser().getUserID()).and().equalTo("hospital.placeName", e.getHospital().getPlaceName()).and().equalTo("date", e.getDate()).findFirst();
                                            eva.setWaitVote(e.getWaitVote());
                                            eva.setStructVote(e.getStructVote());
                                            eva.setServiceVote(e.getServiceVote());
                                            realm.copyToRealmOrUpdate(eva);
                                            Log.println(Log.INFO, "Realm", "Vote already in memory");
                                        }
                                    }
                                }
                            }
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            Log.println(Log.INFO, "Realm", "Vote correctly instantiated");
                        }
                    }, new Realm.Transaction.OnError() {
                        @Override
                        public void onError(Throwable error) {
                            error.printStackTrace();
                        }
                    });
                } // This is your code
            };
            mainHandler.post(myRunnable);
    }

    public User getUser() {
        realm = Realm.getDefaultInstance();
        User res = realm.where(User.class).equalTo("userID", currentUser.getUid()).findFirst();
        return res;
    }

    public List<Address> getUserAddresses(){
        List<Address> res=new LinkedList<>();
        realm = Realm.getDefaultInstance();
        User ret = realm.where(User.class).equalTo("userID", currentUser.getUid()).findFirst();
        for(Address a:ret.getRegisteredAddresses()){
            res.add(a);
        }
        return res;
    }

    public void closeGetRealm(){
        try {
            realm.close();
        }catch (Exception e){}
    }

    public void setUserEvaluations(List<UserEvaluation> model_results) {
        for (UserEvaluation e : model_results) {
            addUserEvaluation(e);
        }
    }

    public List<Hospital> getRegisteredHospitals() {
        realm = Realm.getDefaultInstance();
        List<Hospital> res = realm.where(Hospital.class).findAll();
        return res;
    }

    public void removeUserEvaluation(final UserEvaluation e) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.where(UserEvaluation.class).equalTo("user.userID", e.getUser().getUserID()).and().equalTo("hospital.placeName", e.getHospital().getPlaceName()).and().equalTo("date", e.getDate()).findFirst().deleteFromRealm();
            realm.commitTransaction();
        }catch (Exception es){}
        realm.close();
    }

    public void addUserAddress(final Address address, final Address oldAddress) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if (currentUser != null) {
                            User user = realm.where(User.class).equalTo("userID", currentUser.getUid()).findFirst();
                            if (oldAddress != null) {//modify
                                if (realm.where(Address.class).equalTo("address", oldAddress.getAddress()).findFirst() != null) {
                                    for (Address e : user.getRegisteredAddresses()) {
                                        if (e.equals(oldAddress)) {
                                            e.setAddress(address.getAddress());
                                            e.setLatitude(address.getLatitude());
                                            e.setLongitude(address.getLongitude());
                                            realm.copyToRealmOrUpdate(e);
                                            break;
                                        }
                                    }
                                }
                                Log.println(Log.INFO, "Realm", "Address modification done");
                            } else {//create
                                boolean add = true;
                                //System.err.println(user.getRegisteredAddresses().size());
                                for (Address addr : user.getRegisteredAddresses()) {
                                    if (addr.getAddress().equals(address.getAddress())) {
                                        add = false;
                                        break;
                                    }
                                }
                                if (add) {
                                    Address new_address = new Address(address.getAddress(), address.getLatitude(), address.getLongitude());
                                    realm.copyToRealmOrUpdate(new_address);
                                    user.addAddress(new_address);
                                    Log.println(Log.INFO, "Realm", "Address added");
                                }
                            }
                            realm.copyToRealmOrUpdate(user);
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        Log.println(Log.INFO, "Realm", "Address correctly instantiated");
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

    public void removeUserAddress(final Address address) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.where(Address.class).equalTo("address", address.getAddress()).findFirst().deleteFromRealm();
            realm.commitTransaction();
        }catch (Exception es){}
        realm.close();
    }


    public List<UserEvaluation> getUserEvaluations() {
        List<UserEvaluation> evaluations=new LinkedList<>();
        realm=Realm.getDefaultInstance();
        for(UserEvaluation e:realm.where(UserEvaluation.class).equalTo("user.userID", realm.where(User.class).equalTo("userID", currentUser.getUid()).findFirst().getUserID()).findAll()){
            evaluations.add(e);
        }
        return  evaluations;
    }


    private void deleteUser(String uid){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.where(User.class).equalTo("userID", uid).findFirst().deleteFromRealm();
            realm.commitTransaction();
        }catch (Exception es){}
        realm.close();
    }

}

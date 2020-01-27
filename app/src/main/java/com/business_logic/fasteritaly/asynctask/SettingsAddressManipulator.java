package com.business_logic.fasteritaly.asynctask;

import android.os.AsyncTask;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.business_logic.fasteritaly.util.NetworkHTTPRequester;
import com.model.fasteritaly.Address;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.presentation_layer.fasteritaly.interactor.SettingsInteractor;
import com.presentation_layer.fasteritaly.view.SettingsView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SettingsAddressManipulator extends AsyncTask<Void, Object, Object> {

    private SettingsInteractor.onSettingsActivityInteractionListener listener;
    private boolean executed=false;

    private boolean responded=false;

    private String token;
    private String address;
    private String newAddress;
    private int position;
    private int type;

    private String extra_response;

    public SettingsAddressManipulator(SettingsInteractor.onSettingsActivityInteractionListener l, String token){
        listener=l;
        this.token=token;
        type=0;//get All
    }

    public SettingsAddressManipulator(SettingsInteractor.onSettingsActivityInteractionListener l, String token, String a, String na, int pos){
        listener=l;
        this.token=token;
        address=a;
        newAddress=na;
        position=pos;
        type=1;//modify address
    }

    public SettingsAddressManipulator(SettingsInteractor.onSettingsActivityInteractionListener l, String token, String a){
        listener=l;
        this.token=token;
        address=a;
        type=2;//add address
    }
    public SettingsAddressManipulator(SettingsInteractor.onSettingsActivityInteractionListener l, String token, String a, int pos){
        listener=l;
        this.token=token;
        address=a;
        position=pos;
        type=3;//delete address
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        switch (type){
            case 0:
                NetworkHTTPRequester.getInstance().makeGetStringRequest((String)values[0],(Response.Listener<String>)values[1],(Response.ErrorListener)values[2],"DATABASE");
                break;
            case 1:
                NetworkHTTPRequester.getInstance().makePutStringRequest((String)values[0],(Response.Listener<String>)values[1],(Response.ErrorListener)values[2],"DATABASE");
                break;
            case 2:
                NetworkHTTPRequester.getInstance().makePostStringRequest((String)values[0],(Response.Listener<String>)values[1],(Response.ErrorListener)values[2],"DATABASE");
                break;
            case 3:
                NetworkHTTPRequester.getInstance().makeDeleteStringRequest((String)values[0],(Response.Listener<String>)values[1],(Response.ErrorListener)values[2],"DATABASE");
                break;
        }
        super.onProgressUpdate(values);
    }

    @Override
    protected Object doInBackground(Void... voids) {
        if(executed)
            return null;
        executed=true;
        final Response.Listener<String> listener=new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                NetworkHTTPRequester.getInstance().freeRequestSlot("DATABASE");
                extra_response=(String)response;
                responded =true;
            }
        };
        Response.ErrorListener errorListener=new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(type==0){
                    Response.ErrorListener errorListener1 = new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            NetworkHTTPRequester.getInstance().freeRequestSlot("DATABASE");
                            responded = true;
                        }
                    };
                    String Addresses_backend = "https://"+"CHANGE_TO_FIREBASE_DATABASE_LINK"+".com/user/" + CommonAccessData.getInstance().getCurrentFirebaseUser().getUid() + "/addresses.json?auth=" + token;
                    publishProgress(Addresses_backend, listener, errorListener1);
                    NetworkHTTPRequester.getInstance().freeRequestSlot("DATABASE");
                }else
                    responded =true;
            }
        };;
        String Addresses_backend="https://"+"CHANGE_TO_BACKEND_LINK"+".com/addresses";
        switch (type){
            case 0:
                Addresses_backend=Addresses_backend+"?firebase_token="+token;
                break;
            case 1:
                Addresses_backend=Addresses_backend+"/id?firebase_token="+token+"&address="+this.address+"&new_address="+this.newAddress;
                break;
            case 2:
                Addresses_backend=Addresses_backend+"?firebase_token="+token+"&address="+this.address;
                break;
            case 3:
                Addresses_backend=Addresses_backend+"/id?firebase_token="+token+"&address="+this.address;
                break;
        }
        this.publishProgress(Addresses_backend,listener,errorListener);
        while(!responded){
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {}
        }
        try {
            //System.err.println(extra_response);
            int responseCode = new JSONObject(extra_response).getInt("code");
            if(responseCode==500){
                if(type==0){
                    List<String> results=new LinkedList<>();
                    JSONArray array= new JSONObject(extra_response).getJSONArray("addresses");
                    //System.err.println(array);
                    for(int i=0;i<array.length();i++){
                        JSONObject j=(JSONObject) array.get(i);
                        CommonAccessData.getInstance().addUserAddress(new Address(j.getString("address"),0,0),null);
                        results.add(j.getString("address"));
                    }
                    return results;
                }else{
                    if(type==2){
                        CommonAccessData.getInstance().addUserAddress(new Address(this.address,0,0),null);
                    }else if(type==3){
                        CommonAccessData.getInstance().removeUserAddress(new Address(this.address,0,0));
                    }else if(type==1){
                        for(Address add : CommonAccessData.getInstance().getUserAddresses()){
                            if(add.getAddress().equals(address)){
                                CommonAccessData.getInstance().addUserAddress(new Address(this.newAddress,0,0),new Address(address,0,0));
                                break;
                            }
                        }
                    }
                    return "OK";//Staple
                }
            }else{
                if(responseCode==409)
                    return "REJECTED";
                else
                    return "ERROR";
            }
        }catch(Exception e){
            try {
                if(type==0) {
                    List<String> results = new LinkedList<>();
                    Iterator<String> valuesIt = new JSONObject(extra_response).keys();
                    while (valuesIt.hasNext()) {
                        JSONObject ob=new JSONObject(extra_response).getJSONObject(valuesIt.next());
                        results.add(ob.getString("address"));
                        CommonAccessData.getInstance().addUserAddress(new Address(ob.getString("address"),0,0),null);
                    }
                    return results;
                }
            } catch (Exception es) {
                List<String> results=new LinkedList<>();
                for(Address address:CommonAccessData.getInstance().getUserAddresses()){
                    results.add(address.getAddress());
                }
                CommonAccessData.getInstance().closeGetRealm();
                if(!results.isEmpty()){
                    return results;
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result){
        if(result!=null) {
            switch (type) {
                case 0:
                    listener.onAddressViewUpdate((List<String>) result);
                    break;
                case 1:
                    if(result.toString().equals("OK")) {
                        listener.onAddressModified(this.newAddress, position);
                        listener.onResultMessage(SettingsView.ALL_DONE);
                    }else if(result.toString().equals("REJECTED"))
                        listener.onResultMessage(SettingsView.REJECTED);
                    else
                        listener.onResultMessage(SettingsView.ERROR);

                    break;
                case 2:
                    if(result.toString().equals("OK")) {
                        listener.onAddressInserted(address);
                        listener.onResultMessage(SettingsView.ALL_DONE);
                    }else if(result.toString().equals("REJECTED"))
                        listener.onResultMessage(SettingsView.REJECTED);
                    else
                        listener.onResultMessage(SettingsView.ERROR);
                    break;
                case 3:
                    listener.onAddressDeleted(position);
                    listener.onResultMessage(SettingsView.ALL_DONE);
                    break;
            }
        }else{
            if(type==0){
                listener.onAddressViewUpdate(new LinkedList<String>());
            }
            listener.onResultMessage(SettingsView.ERROR);
        }
        super.onPostExecute(result);
    }
}
package com.business_logic.fasteritaly.asynctask;

import android.os.AsyncTask;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.business_logic.fasteritaly.util.NetworkHTTPRequester;
import com.model.fasteritaly.Address;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.presentation_layer.fasteritaly.interactor.SearchOnMapInteractor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class OnMapAddressManipulator extends AsyncTask<Void, Object, List<String>> {

    private SearchOnMapInteractor.onSearchOnMapInteractionListener listener;
    private boolean executed = false;

    private boolean responded = false;

    private String token;

    private String extra_response;

    private String no_address_found;
    private String choose_an_address;
    private String[] previous;

    public OnMapAddressManipulator(SearchOnMapInteractor.onSearchOnMapInteractionListener l, String token, String no_address_found, String choose_an_address, String[] previous) {
        listener = l;
        this.token = token;
        this.no_address_found = no_address_found;
        this.choose_an_address = choose_an_address;
        this.previous = previous;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        NetworkHTTPRequester.getInstance().makeGetStringRequest((String) values[0], (Response.Listener<String>) values[1], (Response.ErrorListener) values[2],"DATABASE");
        super.onProgressUpdate(values);
    }

    @Override
    protected List<String> doInBackground(Void... voids) {
        if (executed)
            return null;
        executed = true;
        final Response.Listener<String> listener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                NetworkHTTPRequester.getInstance().freeRequestSlot("DATABASE");
                extra_response = (String) response;
                responded = true;
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
            }
        };
        String Addresses_backend = "https://"+"CHANGE_TO_BACKEND_LINK"+".com/addresses?firebase_token=" + token;
        this.publishProgress(Addresses_backend, listener, errorListener);
        while (!responded) {
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
            }
        }
        try {
            //throw new RuntimeException("");
            int responseCode = new JSONObject(extra_response).getInt("code");
            if (responseCode == 500) {
                List<String> results = new LinkedList<>();
                JSONArray array = new JSONObject(extra_response).getJSONArray("addresses");
                //System.err.println(array);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject j = (JSONObject) array.get(i);
                    results.add(j.getString("address"));
                    CommonAccessData.getInstance().addUserAddress(new Address(j.getString("address"),0,0),null);
                }
                return results;
            }
        } catch (Exception e) {
            try {
                //throw new RuntimeException("");
                List<String> results = new LinkedList<>();
                Iterator<String> valuesIt = new JSONObject(extra_response).keys();
                while (valuesIt.hasNext()) {
                    JSONObject ob=new JSONObject(extra_response).getJSONObject(valuesIt.next());
                    results.add(ob.getString("address"));
                    CommonAccessData.getInstance().addUserAddress(new Address(ob.getString("address"),0,0),null);
                }
                return results;
            } catch (Exception es) {
                List<String> results = new LinkedList<>();
                try {
                    for (Address address : CommonAccessData.getInstance().getUserAddresses()) {
                        results.add(address.getAddress());
                    }
                    CommonAccessData.getInstance().closeGetRealm();
                }catch(Exception ed){}
                return results;
            }
        }
        return new LinkedList<>();
    }

    @Override
    protected void onPostExecute(List<String> AddressData) {
        if (AddressData.isEmpty()) {
            ((LinkedList<String>) AddressData).addFirst(no_address_found);
        } else {
            ((LinkedList<String>) AddressData).addFirst(choose_an_address);
        }
        if (AddressData.size() != previous.length) {
            listener.onAddressesTaken(AddressData.toArray(new String[AddressData.size()]));
        } else {
            //Checks if elements differ
            for (String s : AddressData) {
                boolean different = false;
                for (String d : previous) {
                    different = true;
                    if (d.equals(s)) {
                        different = false;
                        break;
                    }
                }
                if (different) {
                    listener.onAddressesTaken(AddressData.toArray(new String[AddressData.size()]));
                    break;
                }
            }
        }
        super.onPostExecute(AddressData);
    }
}

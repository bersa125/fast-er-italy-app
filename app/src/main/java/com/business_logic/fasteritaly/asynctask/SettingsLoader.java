package com.business_logic.fasteritaly.asynctask;

import android.app.Activity;
import android.os.AsyncTask;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.business_logic.fasteritaly.util.NetworkHTTPRequester;
import com.example.fasteritaly.R;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.presentation_layer.fasteritaly.interactor.MainPageInteractor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SettingsLoader extends AsyncTask <Void, Object, String> {

    private MainPageInteractor.onMainPageActivityInteractionListener listener;
    private boolean executed=false;
    private boolean responded = false;

    private String token;
    private String extra_response;

    public SettingsLoader(MainPageInteractor.onMainPageActivityInteractionListener l, String token){
        listener=l;
        this.token=token;
        //System.err.println(token);
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Object... values) {
        if(values.length<4)
            NetworkHTTPRequester.getInstance().makeGetStringRequest((String) values[0], (Response.Listener<String>) values[1], (Response.ErrorListener) values[2],"DATABASE");
        else
            NetworkHTTPRequester.getInstance().makePostStringRequest((String) values[0], (Response.Listener<String>) values[1], (Response.ErrorListener) values[2],"DATABASE");
        super.onProgressUpdate(values);
    }

    @Override
    protected String doInBackground(Void... voids) {
        if(executed)
            return null;
        executed=true;
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
                String Addresses_backend = "https://"+"CHANGE_TO_FIREBASE_DATABASE_LINK"+".com/user/" + CommonAccessData.getInstance().getCurrentFirebaseUser().getUid() + "/settings.json?auth=" + token;
                publishProgress(Addresses_backend, listener, errorListener1);
                NetworkHTTPRequester.getInstance().freeRequestSlot("DATABASE");
            }
        };
        String Addresses_backend = "https://"+"CHANGE_TO_BACKEND_LINK"+".com/settings?firebase_token=" + token;
        this.publishProgress(Addresses_backend, listener, errorListener);
        while (!responded) {
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
            }
        }
        int responseCode=-1;
        try {
            responseCode = new JSONObject(extra_response).getInt("code");
            if (responseCode == 500) {
                CommonAccessData.getInstance().setResultsShown(new JSONObject(extra_response).getJSONObject("settings").getJSONObject("setting").getInt("results_shown"));
                CommonAccessData.getInstance().setTrackPosition(new JSONObject(extra_response).getJSONObject("settings").getJSONObject("setting").getBoolean("tracking"),((Activity)this.listener.getActivity()));
                if(new JSONObject(extra_response).getJSONObject("settings").getJSONObject("setting").getBoolean("tracking") && !CommonAccessData.getInstance().isTrackPosition()){
                    return ((Activity)this.listener.getActivity()).getString(R.string.tracking_not_active);
                }
            }
        } catch (Exception e) {
            if(responseCode!=-1){
                responded=false;
                Response.ErrorListener errorListener1= new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        responded=true;
                    }
                };
                Addresses_backend = Addresses_backend+"&results_shown="+CommonAccessData.getInstance().getResultsShown()+"&tracking="+CommonAccessData.getInstance().isTrackPosition();
                this.publishProgress(Addresses_backend, listener, errorListener1,1);
                while (!responded) {
                    try {
                        Thread.sleep(700);
                    } catch (InterruptedException eghj) {
                    }
                }
            }else {
                try {
                    Iterator<String> valuesIt = new JSONObject(extra_response).keys();
                    while (valuesIt.hasNext()) {
                        String value=valuesIt.next();
                        CommonAccessData.getInstance().setResultsShown(new JSONObject(extra_response).getJSONObject(value).getInt("results_shown"));
                        CommonAccessData.getInstance().setTrackPosition(new JSONObject(extra_response).getJSONObject(value).getBoolean("tracking"),((Activity)this.listener.getActivity()));
                        if(new JSONObject(extra_response).getJSONObject(value).getBoolean("tracking") && !CommonAccessData.getInstance().isTrackPosition()){
                            return ((Activity)this.listener.getActivity()).getString(R.string.tracking_not_active);
                        }
                    }
                } catch (Exception es) {
                    return ((Activity)this.listener.getActivity()).getString(R.string.auth_error);
                }
            }
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result){
        if(result.equals(""))
            listener.onSettingsReady(null);
        else
            listener.onSettingsReady(result);
    }
}

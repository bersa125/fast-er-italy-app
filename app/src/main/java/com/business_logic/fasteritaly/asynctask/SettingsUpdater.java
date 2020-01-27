package com.business_logic.fasteritaly.asynctask;

import android.app.Activity;
import android.os.AsyncTask;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.business_logic.fasteritaly.util.NetworkHTTPRequester;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.presentation_layer.fasteritaly.interactor.SettingsInteractor;
import com.presentation_layer.fasteritaly.view.SettingsView;

import org.json.JSONObject;

public class SettingsUpdater extends AsyncTask<Void, Object, String> {

    private SettingsInteractor.onSettingsActivityInteractionListener listener;
    private boolean executed=false;
    private boolean responded = false;

    private String token;
    private String extra_response;

    private int results_shown;
    private boolean tracking;

    public SettingsUpdater (SettingsInteractor.onSettingsActivityInteractionListener l, String token, int shown, boolean track){
        listener=l;
        this.token=token;
        results_shown=shown;
        tracking=track;
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Object... values) {
        if(values.length<4)
            NetworkHTTPRequester.getInstance().makePutStringRequest((String) values[0], (Response.Listener<String>) values[1], (Response.ErrorListener) values[2],"DATABASE");
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
                NetworkHTTPRequester.getInstance().freeRequestSlot("DATABASE");
                responded=true;
            }
        };
        String Addresses_backend = "https://"+"CHANGE_TO_BACKEND_LINK"+".com/settings/id?firebase_token="+token+"&results_shown="+this.results_shown+"&tracking="+this.tracking;
        this.publishProgress(Addresses_backend, listener, errorListener);
        while (!responded) {
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
            }
        }
        try {
            //System.err.println(extra_response);
            int responseCode = new JSONObject(extra_response).getInt("code");
            if (responseCode != 500) {
                responded=false;
                Response.ErrorListener errorListener1= new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        responded=true;
                    }
                };
                Addresses_backend = "https://"+"CHANGE_TO_BACKEND_LINK"+".com/settings?firebase_token="+token+"&results_shown="+this.results_shown+"&tracking="+this.tracking;
                this.publishProgress(Addresses_backend, listener, errorListener1,1);
                while (!responded) {
                    try {
                        Thread.sleep(700);
                    } catch (InterruptedException eghj) {
                    }
                }
            }
            responseCode = new JSONObject(extra_response).getInt("code");
            if (responseCode != 500) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result){
        if(result==null) {
            CommonAccessData.getInstance().setResultsShown(results_shown);
            CommonAccessData.getInstance().setTrackPosition(tracking,(Activity) listener.getActivityContext());
            if(tracking && !CommonAccessData.getInstance().isTrackPosition()){
                listener.onResultMessage(SettingsView.REJECTED);
            }
        }else{
            listener.onResultMessage(SettingsView.ERROR);
        }
    }
}

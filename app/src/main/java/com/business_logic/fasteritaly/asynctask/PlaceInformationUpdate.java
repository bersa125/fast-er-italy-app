package com.business_logic.fasteritaly.asynctask;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.business_logic.fasteritaly.data_helper.ERStatusService;
import com.business_logic.fasteritaly.data_helper.OverPassAPIHospitalService;
import com.business_logic.fasteritaly.util.NetworkHTTPRequester;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.GetTokenResult;
import com.model.fasteritaly.Hospital;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.model.fasteritaly.singleton_and_helpers.ModelBundleAdapter;
import com.presentation_layer.fasteritaly.interactor.PlaceInfoInteractor;
import com.presentation_layer.fasteritaly.view.PlaceInfoFragmentView;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class PlaceInformationUpdate extends AsyncTask <Void, Void, Bundle> {

    private PlaceInfoInteractor.onPlaceInfoInteractionListener listener;
    private boolean executed=false;
    private boolean done=false;
    private Bundle object;
    private String token=null;

    public PlaceInformationUpdate(PlaceInfoInteractor.onPlaceInfoInteractionListener l, Bundle object){
        listener=l;
        this.object=object;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bundle doInBackground(Void... voids) {
        if(executed)
            return null;
        executed=true;
        if(object.getInt("REQUESTER")== PlaceInfoFragmentView.TYPE_STATE_ER) {
            final Hospital h = ModelBundleAdapter.getHospitalFromBundle(object);
            String region=getAddressDataFromLatLng(h.getAddress().getCoordinates(),listener.getActivityContext(),3);
            Response.Listener<String> listenerO=new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    ERStatusService.getInstance().updateERInfo(response,h,listener.getActivityContext());
                    NetworkHTTPRequester.getInstance().freeRequestSlot("HOSPITALS");
                    done=true;
                }
            };
            Response.ErrorListener errorListener=new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkHTTPRequester.getInstance().freeRequestSlot("HOSPITALS");
                }
            };
            NetworkHTTPRequester.getInstance().makeGetStringRequest(ERStatusService.getInstance().getServiceLink(region),listenerO,errorListener,"HOSPITALS");
            while(!done){
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {}
            }
            return ModelBundleAdapter.getBundleRepresentation(h);
        }else{
            if(object.getInt("REQUESTER")== PlaceInfoFragmentView.TYPE_ER_GENERAL_VOTES){
                this.AddVoteInformation(object);
            }
            return object;
        }
    }

    @Override
    protected void onPostExecute(Bundle result){
        listener.onUpdateChildNotificationRequest(object.getInt("REQUESTER"),result);
        super.onPostExecute(result);
    }

    private String getAddressDataFromLatLng(LatLng coordinates, Context context, int tentatives) {//In this case returns the region
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        try {
            address = coder.getFromLocation(coordinates.latitude,coordinates.longitude,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            return location.getAdminArea();
        }catch(Exception e){
            if(tentatives>0)
                return getAddressDataFromLatLng(coordinates, context, tentatives-1);
            return null;
        }
    }
    private void AddVoteInformation(final Bundle hospital){
        final String[] extra_response = {null};
        if(token==null) {
            try {
                CommonAccessData.getInstance().getCurrentFirebaseUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                    @Override
                    public void onSuccess(GetTokenResult getTokenResult) {
                        //Launch the request to the server
                        token = getTokenResult.getToken();
                    }
                });
            } catch (Exception e) {
                token = "";
            }
        }
        while(token==null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {token="";}
        }
        if(token.equals("")){
            token=null;
            return;
        }else{
            String Evaluations_backend="https://"+"CHANGE_TO_BACKEND_LINK"+".com/evaluations?firebase_token="+token+"&hospital_placename="+hospital.getString("PLACE")+"&hospital_address="+hospital.getString("ADDRESS")+"&avg=true";
            final Response.Listener<String> listener1=new Response.Listener<String>() {
                @Override
                public void onResponse(final String response) {
                    extra_response[0]=response;
                    NetworkHTTPRequester.getInstance().freeRequestSlot("DATABASE");
                }
            };
            Response.ErrorListener errorListener=new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(!isCancelled()) {
                        String Evaluations_backend = "https://"+"CHANGE_TO_FIREBASE_DATABASE_LINK"+".com/hospital_votes.json?auth=" + token+"&orderBy=\"hospital\"&equalTo=\""+hospital.getString("PLACE")+"\"";
                        Response.ErrorListener errorListener=new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                extra_response[0] ="";
                                NetworkHTTPRequester.getInstance().freeRequestSlot("DATABASE");
                            }
                        };
                        NetworkHTTPRequester.getInstance().makeGetStringRequest(Evaluations_backend,listener1,errorListener,"DATABASE");
                    }else {
                        extra_response[0] ="";
                    }
                    NetworkHTTPRequester.getInstance().freeRequestSlot("DATABASE");
                }
            };
            NetworkHTTPRequester.getInstance().makeGetStringRequest(Evaluations_backend,listener1,errorListener,"DATABASE");
            while(extra_response[0]==null){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    extra_response[0]="";
                }
            }
            try{
                int responseCode = new JSONObject(extra_response[0]).getInt("code");
                if(responseCode==500){
                    hospital.putInt("AVG_VOTE_WAIT",(int)new JSONObject(extra_response[0]).getDouble("avg_wait_vote"));
                    hospital.putInt("AVG_VOTE_STRUCT",(int)new JSONObject(extra_response[0]).getDouble("avg_struct_vote"));
                    hospital.putInt("AVG_VOTE_SERVICE",(int)new JSONObject(extra_response[0]).getDouble("avg_service_vote"));
                    hospital.putInt("AVG_VOTE",(int)((hospital.getInt("AVG_VOTE_WAIT")+hospital.getInt("AVG_VOTE_STRUCT")+hospital.getInt("AVG_VOTE_SERVICE"))/3));
                }
            }catch (Exception e){
                try{
                    int votes=0;
                    int wait_vote_sum=0;
                    int struct_vote_sum=0;
                    int service_vote_sum=0;
                    Iterator<String> valuesIt = new JSONObject(extra_response[0]).keys();
                    while (valuesIt.hasNext()) {
                        JSONObject j = new JSONObject(extra_response[0]).getJSONObject(valuesIt.next());
                        if(j.getString("hospital").equals(hospital.getString("PLACE")) && j.getString("address").equals(hospital.getString("ADDRESS"))){
                            votes++;
                            wait_vote_sum+=j.getInt("wait_vote");
                            struct_vote_sum+=j.getInt("struct_vote");
                            service_vote_sum+=j.getInt("service_vote");
                        }
                    }
                    hospital.putInt("AVG_VOTE_WAIT",(int)(wait_vote_sum/votes));
                    hospital.putInt("AVG_VOTE_STRUCT",(int)(struct_vote_sum/votes));
                    hospital.putInt("AVG_VOTE_SERVICE",(int)(service_vote_sum/votes));
                    hospital.putInt("AVG_VOTE",(int)((hospital.getInt("AVG_VOTE_WAIT")+hospital.getInt("AVG_VOTE_STRUCT")+hospital.getInt("AVG_VOTE_SERVICE"))/3));
                }catch (Exception es){}
            }
        }
    }
}

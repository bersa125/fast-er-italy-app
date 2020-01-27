package com.business_logic.fasteritaly.asynctask;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.business_logic.fasteritaly.util.NetworkHTTPRequester;
import com.google.android.gms.maps.model.LatLng;
import com.model.fasteritaly.Hospital;
import com.model.fasteritaly.UserEvaluation;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.model.fasteritaly.singleton_and_helpers.ModelBundleAdapter;
import com.presentation_layer.fasteritaly.interactor.HistoryInteractor;
import com.presentation_layer.fasteritaly.interactor.PlaceInfoInteractor;
import com.presentation_layer.fasteritaly.interactor.SettingsInteractor;
import com.presentation_layer.fasteritaly.view.HistoryView;
import com.presentation_layer.fasteritaly.view.PlaceInfoView;
import com.presentation_layer.fasteritaly.view.SettingsView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HistoryManipulator extends AsyncTask<Void, Object, Object> {

    private HistoryInteractor.onHistoryActivityInteractionListener listener;
    private PlaceInfoInteractor.onPlaceInfoInteractionListener listener_post;
    private boolean executed=false;

    private boolean responded=false;


    private Geocoder coder;
    private String token;
    private Bundle data;
    private int[] votes;
    private List<Bundle> previous_votes;
    private boolean average;

    private UserEvaluation created;

    private int type;

    private String extra_response;

    public HistoryManipulator(HistoryInteractor.onHistoryActivityInteractionListener l, String token, List<Bundle> passed){
        listener=l;
        this.token=token;
        previous_votes=passed;
        average=false;
        type=0;//get All for user
    }

    public HistoryManipulator(HistoryInteractor.onHistoryActivityInteractionListener l, String token, Bundle ev){
        listener=l;
        this.token=token;
        data=ev;
        type=1;//modify address
    }

    public HistoryManipulator(PlaceInfoInteractor.onPlaceInfoInteractionListener l, String token, Bundle h, int[] votes){
        listener_post=l;
        this.token=token;
        data=h;
        this.votes=votes;
        type=2;//add address
    }
    public HistoryManipulator(HistoryInteractor.onHistoryActivityInteractionListener l, String token, Bundle ev,boolean delete){
        listener=l;
        this.token=token;
        data=ev;
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
                    String Evaluations_backend = "https://"+"CHANGE_TO_FIREBASE_DATABASE_LINK"+".com/user/" + CommonAccessData.getInstance().getCurrentFirebaseUser().getUid() + "/votes.json?auth=" + token;
                    publishProgress(Evaluations_backend, listener, errorListener1);
                    NetworkHTTPRequester.getInstance().freeRequestSlot("DATABASE");
                }else
                    responded =true;
            }
        };;
        String Evaluations_backend="https://"+"CHANGE_TO_BACKEND_LINK"+".com/evaluations";
        switch (type){
            case 0:
                Evaluations_backend=Evaluations_backend+"?firebase_token="+token+"&avg="+average;
                break;
            case 1:
                created= ModelBundleAdapter.getUserEvaluationFromBundle(data);
                Evaluations_backend=Evaluations_backend+"/id?firebase_token="+token+"&hospital_placename="+created.getHospital().getPlaceName()+"&hospital_address="+created.getHospital().getAddress().getAddress()+"&timestamp="+created.getDate()+"&wait_vote="+created.getWaitVote()+"&struct_vote="+created.getStructVote()+"&service_vote="+created.getServiceVote();
                break;
            case 2:
                Hospital pointed= ModelBundleAdapter.getHospitalFromBundle(data);
                created= new UserEvaluation(this.votes[0],this.votes[1],this.votes[2],CommonAccessData.getInstance().getUser(),pointed);
                created=ModelBundleAdapter.getUserEvaluationFromBundle(ModelBundleAdapter.getBundleRepresentation(created));
                CommonAccessData.getInstance().closeGetRealm();
                Evaluations_backend=Evaluations_backend+"?firebase_token="+token+"&hospital_placename="+created.getHospital().getPlaceName()+"&hospital_address="+created.getHospital().getAddress().getAddress()+"&timestamp="+created.getDate()+"&wait_vote="+created.getWaitVote()+"&struct_vote="+created.getStructVote()+"&service_vote="+created.getServiceVote();
                break;
            case 3:
                created= ModelBundleAdapter.getUserEvaluationFromBundle(data);
                Evaluations_backend=Evaluations_backend+"/id?firebase_token="+token+"&hospital_placename="+created.getHospital().getPlaceName()+"&hospital_address="+created.getHospital().getAddress().getAddress()+"&timestamp="+created.getDate();
                break;
        }
        this.publishProgress(Evaluations_backend,listener,errorListener);
        while(!responded){
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {}
        }
        try {
            int responseCode = new JSONObject(extra_response).getInt("code");
            if(responseCode==500){
                if(type==0){
                    List<Bundle> results=new LinkedList<>();
                    List<UserEvaluation> model_results=new LinkedList<>();
                    JSONArray array= new JSONObject(extra_response).getJSONArray("history");
                    //System.err.println(array);
                    for(int i=0;i<array.length();i++){
                        JSONObject j=(JSONObject) array.get(i);
                        Hospital h=null;
                        if(CommonAccessData.getInstance().getHospitalByName(j.getString("hospital")+j.getString("address"))!=null){
                            h=CommonAccessData.getInstance().getHospitalByName(j.getString("hospital")+j.getString("address"));
                        }else{
                            LatLng obtainedCoordinates=this.getLatLngFromAddress(j.getString("address"),this.listener.getActivityContext(),3);
                            com.model.fasteritaly.Address address=new com.model.fasteritaly.Address(j.getString("address"),obtainedCoordinates);
                            h=new Hospital(j.getString("hospital"),address);
                            CommonAccessData.getInstance().putHospital(h);
                        }
                        UserEvaluation eva=new UserEvaluation(j.getString("date"),j.getInt("wait_vote"),j.getInt("struct_vote"),j.getInt("service_vote"),CommonAccessData.getInstance().getUser(),h);
                        results.add(ModelBundleAdapter.getBundleRepresentation(eva));
                        model_results.add(eva);
                    }
                    Collections.sort(results, new Comparator<Bundle>() {
                        @Override
                        public int compare(Bundle o1, Bundle o2) {
                            return o1.getString("DATE").compareTo(o2.getString("DATE"));
                        }
                    });
                    CommonAccessData.getInstance().setUserEvaluations(model_results);
                    return results;
                }else{
                    if(type==1){
                        CommonAccessData.getInstance().addUserEvaluation(created);
                    }else{
                        if(type==2){
                            CommonAccessData.getInstance().addUserEvaluation(created);
                        }else{
                            if(type==3){
                                CommonAccessData.getInstance().removeUserEvaluation(created);
                            }
                        }
                    }
                }
            }
            return responseCode + "";
        }catch(Exception e){
            e.printStackTrace();
            try {
                if(type==0) {
                    List<Bundle> results=new LinkedList<>();
                    List<UserEvaluation> model_results=new LinkedList<>();
                    Iterator<String> valuesIt = new JSONObject(extra_response).keys();
                    while (valuesIt.hasNext()) {
                        JSONObject j=new JSONObject(extra_response).getJSONObject(valuesIt.next());
                        Hospital h=null;
                        if(CommonAccessData.getInstance().getHospitalByName(j.getString("hospital")+j.getString("address"))!=null){
                            h=CommonAccessData.getInstance().getHospitalByName(j.getString("hospital")+j.getString("address"));
                        }else{
                            LatLng obtainedCoordinates=this.getLatLngFromAddress(j.getString("address"),this.listener.getActivityContext(),3);
                            com.model.fasteritaly.Address address=new com.model.fasteritaly.Address(j.getString("address"),obtainedCoordinates);
                            h=new Hospital(j.getString("hospital"),address);
                            CommonAccessData.getInstance().putHospital(h);
                        }
                        UserEvaluation eva=new UserEvaluation(j.getString("timestamp"),j.getInt("wait_vote"),j.getInt("struct_vote"),j.getInt("service_vote"),CommonAccessData.getInstance().getUser(),h);
                        results.add(ModelBundleAdapter.getBundleRepresentation(eva));
                        model_results.add(eva);
                    }
                    CommonAccessData.getInstance().setUserEvaluations(model_results);
                    return results;
                }
            } catch (Exception es) {
                es.printStackTrace();
                if(type==0) {
                    List<Bundle> res=new LinkedList<>();
                    for(UserEvaluation eva: CommonAccessData.getInstance().getUserEvaluations()){
                        res.add(ModelBundleAdapter.getBundleRepresentation(eva));
                    }
                    Collections.sort(res, new Comparator<Bundle>() {
                        @Override
                        public int compare(Bundle o1, Bundle o2) {
                            return o1.getString("DATE").compareTo(o2.getString("DATE"));
                        }
                    });
                    return res;
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
                    listener.onHistoryUpdate((List<Bundle>) result);
                    break;
                case 1:
                    if(result.toString().equals("500"))
                        listener.onReturnValue(HistoryView.ALL_DONE,type,data);
                    else if(result.toString().equals("409")){
                        listener.onReturnValue(HistoryView.REJECTED_MODIFY,-1,data);
                    }else{
                        listener.onReturnValue(HistoryView.ERROR,-1,data);
                    }
                    break;
                case 2:
                    if(result.toString().equals("500"))
                        listener_post.onVoteSubmitted(PlaceInfoView.ALL_DONE,ModelBundleAdapter.getBundleRepresentation(created));
                    else if(result.toString().equals("409")){
                        listener_post.onVoteSubmitted(PlaceInfoView.REJECTED,null);
                    }else{
                        listener_post.onVoteSubmitted(PlaceInfoView.ERROR,null);
                    }
                    break;
                case 3:
                    if(result.toString().equals("500"))
                        listener.onReturnValue(HistoryView.ALL_DONE,type,data);
                    else if(result.toString().equals("204")){
                        listener.onReturnValue(HistoryView.REJECTED,-1,data);
                    }else{
                        listener.onReturnValue(HistoryView.ERROR,-1,data);
                    }
                    break;
            }
        }else{
            if(type==2){
                listener_post.onVoteSubmitted(PlaceInfoView.ERROR,null);
            }else {
                listener.onReturnValue(HistoryView.ERROR,-1, data);
            }
        }
        super.onPostExecute(result);
    }

    private LatLng getLatLngFromAddress(String strAddress, Context ctx, int tentatives){
        if(coder==null && ctx!=null)
            coder = new Geocoder(ctx);
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();
            return new LatLng((location.getLatitude()/* * 1E6*/), (location.getLongitude() /* * 1E6*/));
        }catch(Exception e){
            e.printStackTrace();
            coder=null;
            if(tentatives>0) {
                return getLatLngFromAddress(strAddress, ctx,tentatives-1);
            }
            return null;
        }
    }
}

package com.business_logic.fasteritaly.asynctask;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.business_logic.fasteritaly.data_helper.ERStatusService;
import com.business_logic.fasteritaly.data_helper.OpenPharmacyService;
import com.business_logic.fasteritaly.data_helper.OverPassAPIHospitalService;
import com.business_logic.fasteritaly.util.NetworkHTTPRequester;
import com.example.fasteritaly.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.GetTokenResult;
import com.model.fasteritaly.Hospital;
import com.model.fasteritaly.Pharmacy;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;
import com.model.fasteritaly.singleton_and_helpers.ModelBundleAdapter;
import com.presentation_layer.fasteritaly.interactor.SearchOnMapInteractor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FindNearPlaces extends AsyncTask<Void, Object, ArrayList<Bundle>>{

    public static String placeHospital="hospital";
    public static String placePharmacy="pharmacy";

    private Geocoder coder;

    private SearchOnMapInteractor.onSearchOnMapInteractionListener listener;
    private Context context;
    private String places;
    private LatLng location;
    private String locationAddress;
    private int searchLimit=5;

    private boolean limitRegion=true;
    private String token;


    private int regionsDone=0;
    private boolean transactionCompleted =false;
    private boolean executed=false;

    private Hospital best;
    private Hospital worst;


    public FindNearPlaces(SearchOnMapInteractor.onSearchOnMapInteractionListener l, Context context, String places, LatLng loc, String address, int limit) {
        this.context = context;
        this.places = places;
        this.location=loc;
        searchLimit=limit;
        locationAddress=address;
        listener=l;
    }

    private FindNearPlaces(SearchOnMapInteractor.onSearchOnMapInteractionListener l, Context context, String places, LatLng loc, String address, int limit, boolean moreRegions) {
        this.context = context;
        this.places = places;
        this.location=loc;
        locationAddress=address;
        searchLimit=limit;
        listener=l;
        limitRegion=moreRegions;
    }

    @Override
    protected void onPostExecute(ArrayList<Bundle> result) {
        if(places.equals(placeHospital)) {
            if (listener.getCurrentPosition() != null) {
                if (listener.getCurrentPosition().equals(this.location)) {
                    try {
                        listener.updateChildOnLoading(false, false);
                        listener.updateChildOnResults(result);
                        if (this.limitRegion) {
                            new FindNearPlaces(listener, context, places, location, locationAddress, searchLimit, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else
                            listener.updateChildOnLoading(false, true);
                    }catch(Exception e){}
                }
            }
        }else{
            listener.updateChildOnLoading(false,false);
            listener.updateChildOnResults(result);
        }
        super.onPostExecute(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(limitRegion) {
            listener.updateChildOnLoading(false, true);
            listener.updateChildOnLoading(true, false);
        }else
            listener.updateChildOnLoading(true,true);
    }


    @Override
    protected ArrayList<Bundle> doInBackground (Void...arg0){//Gather data from datiOpen for the hospitals
        if(executed)
            return new ArrayList<>();
        executed=true;
        ArrayList<Bundle> results=new ArrayList<>();
        try {
            if (places.equals(placeHospital)) {
                if(location==null){
                    location=getLatLngFromAddress(locationAddress,context,3);
                }

                List<String> regions=getAdjacentRegions(getAddressDataFromLatLng(location,context,3),limitRegion);
                List<Hospital> OverpassResults=new ArrayList<>();
                final List<Hospital> ERStatusResults=new ArrayList<>();
                List<Hospital> result=new ArrayList<>();

                Map<Object,Integer> tentatives=new HashMap<>();
                Map<Map<String,JSONObject>, String> refusedRegions=new HashMap<>();
                Queue<String> failedRegionRequests=new ConcurrentLinkedQueue<>();
                Queue<Map<String,JSONObject>> failedErrorRequests=new ConcurrentLinkedQueue<>();

                for(final String region:regions){
                    //Work on Overpass
                    if(OverPassAPIHospitalService.getInstance().getCreatedHospitals(region)!=null){
                        //System.err.println(OverPassAPIHospitalService.getInstance().getCreatedHospitals(region).size());
                        boolean useStored=true;
                        if(OverPassAPIHospitalService.getInstance().getCreatedHospitals(region).isEmpty()){
                            useStored=false;
                        }
                        for(Hospital h: OverPassAPIHospitalService.getInstance().getCreatedHospitals(region)){
                            if(h==null){
                                useStored=false;
                                break;
                            }
                        }
                        if(!useStored){
                            this.makeRegionRequest(region,OverpassResults,failedErrorRequests,failedRegionRequests,refusedRegions);
                        }else {
                            OverpassResults.addAll(OverPassAPIHospitalService.getInstance().getCreatedHospitals(region));
                            regionsDone++;
                        }
                    }else{
                        this.makeRegionRequest(region,OverpassResults,failedErrorRequests,failedRegionRequests,refusedRegions);
                    }
                    //Work on ER status
                    if(ERStatusService.getInstance().getServiceLink(region)!=null && !isCancelled()){
                        Response.Listener<String> listenerO=new Response.Listener<String>() {
                            @Override
                            public void onResponse(final String response) {
                                new AsyncTask<Void,Void,Void>(){
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        try {
                                            ERStatusService.getInstance().memorizeHospitalsFromJsonResult(response,region,context,ERStatusResults);
                                        }catch(Exception e){}
                                        regionsDone++;
                                        return null;
                                    }
                                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                                NetworkHTTPRequester.getInstance().freeRequestSlot("HOSPITALS");
                            }
                        };
                        Response.ErrorListener errorListener=new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                regionsDone++;
                                NetworkHTTPRequester.getInstance().freeRequestSlot("HOSPITALS");
                            }
                        };
                        publishProgress(ERStatusService.getInstance().getServiceLink(region),listenerO,errorListener);
                    }else{
                        regionsDone++;
                    }
                }
                while(regionsDone<regions.size()*2){
                    try {
                        Thread.sleep(100);
                    }catch(Exception e){}
                    if(this.isCancelled()){
                        break;
                    }
                    if(!failedRegionRequests.isEmpty()){
                        String region=failedRegionRequests.poll();
                        if(tentatives.get(region)==null){
                            tentatives.put(region,0);
                        }
                        if(tentatives.get(region)<2){
                            tentatives.put(region,tentatives.get(region)+1);
                            this.makeRegionRequest(region,OverpassResults,failedErrorRequests,failedRegionRequests,refusedRegions);
                        }else
                            regionsDone++;
                    }
                    if(!failedErrorRequests.isEmpty()){
                        Map<String,JSONObject> refused=failedErrorRequests.poll();
                        if(tentatives.get(refused)==null){
                            tentatives.put(refused,0);
                        }
                        if(tentatives.get(refused)<2){
                            tentatives.put(refused,tentatives.get(refused)+1);
                            if(refusedRegions.get(refused)!=null)
                                this.makeRefusedRequest(refused,refusedRegions.get(refused),OverpassResults,failedErrorRequests,refusedRegions);
                            else
                                regionsDone++;
                        }else
                            regionsDone++;
                    }
                }
                List<Hospital> OrderedOverpassResults=OverPassAPIHospitalService.getInstance().getFinalOrderedResults(location, OverpassResults,(int)(searchLimit*1.5),context);//Correction for some issues with final results
                ERStatusService.getInstance().mergeResults(result,OrderedOverpassResults,ERStatusResults,location,context,searchLimit);
                findBestAndWorstWaitQueue(result);
                for (Hospital h : result) {
                    CommonAccessData.getInstance().putHospital(h);
                    CommonAccessData.getInstance().putFinalHospital(h);
                    Bundle bundle = ModelBundleAdapter.getBundleRepresentation(h);
                    if (this.best != null) {
                        if (h.getPlaceName().equals(this.best.getPlaceName())) {
                            bundle.putBoolean("BEST_QUEUE", true);
                        } else {
                            if (h.getPlaceName().equals(this.worst.getPlaceName())) {
                                bundle.putBoolean("WORST_QUEUE", true);
                            }
                        }
                    }

                    this.AddVoteInformation(bundle);

                    results.add(bundle);
                }
            } else if (places.equals(placePharmacy)) {
                final ArrayList<Pharmacy> pharmacies=new ArrayList<>();
                Response.Listener listener=new Response.Listener() {
                    @Override
                    public void onResponse(final Object response) {
                        new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected Void doInBackground(Void... voids) {
                                try {
                                    pharmacies.addAll(OpenPharmacyService.getPharmaciesFromQuery((String) response, context, searchLimit));
                                    transactionCompleted =true;
                                }catch (Exception e){transactionCompleted =true;}
                                return null;
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        NetworkHTTPRequester.getInstance().freeRequestSlot("PHARMACIES");
                    }
                };
                Response.ErrorListener errorListener=new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        transactionCompleted =true;
                        NetworkHTTPRequester.getInstance().freeRequestSlot("PHARMACIES");
                    }
                };
                publishProgress(OpenPharmacyService.getServiceLink(),listener,errorListener,OpenPharmacyService.composeDataRequest(location,locationAddress,context));
                while(!transactionCompleted){
                    Thread.sleep(400);
                }
                for(Pharmacy p: pharmacies){
                    results.add(ModelBundleAdapter.getBundleRepresentation(p));
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return results;
    }

    private void findBestAndWorstWaitQueue(List<Hospital> hospitals){
        for(Hospital h:hospitals) {
            int queue = h.getGreen_WaitQueue() + h.getYellow_WaitQueue() + h.getRed_WaitQueue() + h.getWhite_WaitQueue() + h.getNonExec_WaitQueue();
            if (queue >= 0) {
                if (this.best == null) {
                    best = h;
                    worst = h;
                } else {
                    if (queue >= worst.getGreen_WaitQueue() + worst.getYellow_WaitQueue() + worst.getRed_WaitQueue() + worst.getWhite_WaitQueue() + worst.getNonExec_WaitQueue()) {
                        worst = h;
                    } else {
                        if (queue <= best.getGreen_WaitQueue() + best.getYellow_WaitQueue() + best.getRed_WaitQueue() + best.getWhite_WaitQueue() + best.getNonExec_WaitQueue()) {
                            best = h;
                        }
                    }
                }
            }
        }
    }

    private List<String> getAdjacentRegions(String region, boolean limitRegion) {
        List<String> results=new LinkedList<>();
        if(region!=null) {
            results.add(region);
            if(!limitRegion) {
                if (region.toLowerCase().equals("lazio")) {
                    results.add("Toscana");
                    results.add("Umbria");
                    results.add("Campania");
                    results.add("Molise");
                    results.add("Abruzzo");
                }
                if (region.toLowerCase().equals("calabria")) {
                    results.add("Basilicata");
                }
                if (region.toLowerCase().equals("puglia")) {
                    results.add("Molise");
                    results.add("Campania");
                    results.add("Basilicata");
                }
                if (region.toLowerCase().equals("campania")) {
                    results.add("Lazio");
                    results.add("Molise");
                    results.add("Puglia");
                    results.add("Basilicata");
                }
                if (region.toLowerCase().equals("molise")) {
                    results.add("Lazio");
                    results.add("Abruzzo");
                    results.add("Puglia");
                    results.add("Campania");
                }
                if (region.toLowerCase().equals("basilicata")) {
                    results.add("Campania");
                    results.add("Puglia");
                    results.add("Calabria");
                }
                if (region.toLowerCase().equals("abruzzo")) {
                    results.add("Marche");
                    results.add("Lazio");
                    results.add("Molise");
                }
                if (region.toLowerCase().equals("marche")) {
                    results.add("Emilia-Romagna");
                    results.add("Toscana");
                    results.add("Umbria");
                    results.add("Abruzzo");
                }
                if (region.toLowerCase().equals("umbria")) {
                    results.add("Marche");
                    results.add("Toscana");
                    results.add("Lazio");
                }
                if (region.toLowerCase().equals("toscana")) {
                    results.add("Liguria");
                    results.add("Emilia-Romagna");
                    results.add("Marche");
                    results.add("Umbria");
                    results.add("Lazio");
                }
                if (region.toLowerCase().equals("liguria")) {
                    results.add("Piemonte");
                    results.add("Emilia-Romagna");
                    results.add("Toscana");
                }
                if (region.toLowerCase().equals("emilia-romagna")) {
                    results.add("Veneto");
                    results.add("Lombardia");
                    results.add("Piemonte");
                    results.add("Liguria");
                    results.add("Toscana");
                    results.add("Marche");
                }
                if (region.toLowerCase().equals("trentino-alto adige")) {
                    results.add("Lombardia");
                    results.add("Veneto");
                }
                if (region.toLowerCase().equals("friuli-venezia giulia")) {
                    results.add("Veneto");
                }
                if (region.toLowerCase().equals("piemonte")) {
                    results.add("Valle d'Aosta");
                    results.add("Liguria");
                    results.add("Lombardia");
                    results.add("Piemonte");
                    results.add("Emilia-Romagna");
                }
                if (region.toLowerCase().equals("lombardia")) {
                    results.add("Piemonte");
                    results.add("Emilia-Romagna");
                    results.add("Trentino-Alto Adige");
                    results.add("Veneto");
                }
                if (region.toLowerCase().equals("veneto")) {
                    results.add("Emilia-Romagna");
                    results.add("Trentino-Alto Adige");
                    results.add("Lombardia");
                    results.add("Friuli-Venezia Giulia");
                }
                if (region.toLowerCase().equals("valle d'aosta")) {
                    results.add("Piemonte");
                }
            }
        }
        return results;
    }
    private String getAddressDataFromLatLng(LatLng coordinates, Context context, int tentatives) {//In this case returns the region
        if(coder==null)
            coder = new Geocoder(context);
        List<Address> address;
        try {
            address = coder.getFromLocation(coordinates.latitude,coordinates.longitude,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            return location.getAdminArea();
        }catch(Exception e){
            e.printStackTrace();
            coder=null;
            if(tentatives>0)
                return getAddressDataFromLatLng(coordinates, context, tentatives-1);
            return null;
        }
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

    private void makeRegionRequest(final String region, final List<Hospital> OverpassResults, final Queue<Map<String,JSONObject>> failedErrorRequests, final Queue<String> failedRegionRequests, final Map<Map<String,JSONObject>, String> refusedRegions){
        final Response.Listener<String> listenerO=new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            if(!isCancelled()) {
                                makeRefusedRequest(OverPassAPIHospitalService.getInstance().memorizeHospitalsFromJSONresult(response, region, OverpassResults),region,OverpassResults,failedErrorRequests,refusedRegions);
                            }else
                                regionsDone++;
                        }catch(Exception e){ regionsDone++;}
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                NetworkHTTPRequester.getInstance().freeRequestSlot("OVERPASS-API");
            }
        };
        Response.ErrorListener errorListener=new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                OverPassAPIHospitalService.getInstance().changeServerID();
                if(!isCancelled()){
                    failedRegionRequests.add(region);
                }else {
                    regionsDone++;
                }
                NetworkHTTPRequester.getInstance().freeRequestSlot("OVERPASS-API");
            }
        };
        if(!isCancelled()) {
            if(NetworkHTTPRequester.getInstance().getActiveRequests("OVERPASS-API")<4)
                publishProgress(OverPassAPIHospitalService.getInstance().getHospitalperRegionLink(region), listenerO, errorListener);
            else
                failedRegionRequests.add(region);
        }else
            regionsDone++;
    }

    private void makeRefusedRequest(final Map<String, JSONObject> refused, final String region, final List<Hospital> OverpassResults, final Queue<Map<String,JSONObject>> failedErrorRequests, final Map<Map<String,JSONObject>, String> refusedRegions){
        final Response.Listener<String> listener1=new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            OverPassAPIHospitalService.getInstance().memorizeOtherHospitalsfromJSONsecondChanceResult(refused, region, response, OverpassResults);
                        }catch(Exception e){}
                        regionsDone++;
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                NetworkHTTPRequester.getInstance().freeRequestSlot("OVERPASS-API");
            }
        };
        Response.ErrorListener errorListener=new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                OverPassAPIHospitalService.getInstance().changeServerID();
                if(!isCancelled()) {
                    OverPassAPIHospitalService.getInstance().changeServerID();
                    failedErrorRequests.add(refused);
                    refusedRegions.put(refused,region);
                }else {
                    regionsDone++;
                }
                NetworkHTTPRequester.getInstance().freeRequestSlot("OVERPASS-API");
            }
        };
        if(!isCancelled())
            if(NetworkHTTPRequester.getInstance().getActiveRequests("OVERPASS-API")<4)
                publishProgress(OverPassAPIHospitalService.getInstance().createNodesRequest(refused.keySet()), listener1, errorListener);
            else{
                failedErrorRequests.add(refused);
                refusedRegions.put(refused,region);
            }
        else
            regionsDone++;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        if(values.length==3)
            NetworkHTTPRequester.getInstance().makeGetStringRequest((String)values[0],(Response.Listener<String>)values[1],(Response.ErrorListener)values[2],"OVERPASS-API");
        else
            NetworkHTTPRequester.getInstance().makePostStringRequest((String)values[0],(Response.Listener<String>)values[1],(Response.ErrorListener)values[2],(Map<String, String>)values[3],"PHARMACIES");
        super.onProgressUpdate(values);
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
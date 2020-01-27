package com.business_logic.fasteritaly.data_helper;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.model.fasteritaly.Hospital;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ERStatusService {
    private static ERStatusService instance;

    private Map<String, LatLng> ERCoordinates=new HashMap<>();
    private Map<String, String> ERAddress=new HashMap<>();
    private String ServiceLinkLazio="http://dati.lazio.it/catalog/api/action/datastore_search?resource_id=12c31624-f1a4-4874-a903-8954549ddb81";

    public static ERStatusService getInstance() {
        if(instance==null){
            instance=new ERStatusService();
        }else{
            if(instance.ERCoordinates==null){
                instance.ERCoordinates=new HashMap<>();
            }
            if(instance.ERAddress ==null){
                instance.ERAddress=new HashMap<>();
            }
            if(instance.ServiceLinkLazio==null){
                instance.ServiceLinkLazio="http://dati.lazio.it/catalog/api/action/datastore_search?resource_id=12c31624-f1a4-4874-a903-8954549ddb81";
            }
        }
        return instance;
    }

    private ERStatusService(){}

    public void updateERInfo(String json, Hospital h, Context context){
        if(getAddressDataFromLatLng(h.getAddress().getCoordinates(),context,3).toLowerCase().equals("lazio")){
            try {
                LatLng hospitalCoordinates=h.getAddress().getCoordinates();
                JSONArray array=new JSONObject(json).getJSONObject("result").getJSONArray("records");
                for(int i=0;i<array.length();i++){
                    ERStatusLazio status=ERStatusLazio.dataToContainer((JSONObject)array.get(i));
                    LatLng statusCoordinates;
                    if(ERCoordinates.get((status.getNome()+status.getComune()).toLowerCase())!=null){
                        statusCoordinates=ERCoordinates.get((status.getNome()+status.getComune()).toLowerCase());
                    }else{
                        if(status.getNome().contains("Pol. Univ.")){
                            statusCoordinates=getLatLngFromAddress("Ospedale "+status.getNome().substring(("Pol. Univ.").length())+" "+status.getComune()+" Lazio", context,3);
                        }else{
                            statusCoordinates=getLatLngFromAddress(status.getNome()+" "+status.getComune()+" Lazio", context,3);
                        }
                        ERCoordinates.put((status.getNome()+status.getComune()).toLowerCase(),statusCoordinates);
                    }
                    String statusAddress;
                    if(ERAddress.get((status.getNome()+status.getComune()).toLowerCase())!=null){
                        statusAddress=ERAddress.get((status.getNome()+status.getComune()).toLowerCase());
                    }else{
                        statusAddress=getAddressFromLatLng(statusCoordinates,context,3);
                        ERAddress.put((status.getNome()+status.getComune()).toLowerCase(),statusAddress);
                    }
                    //System.err.println(h.getPlaceName()+" "+h.getAddress().getTown()+" "+h.getAddress().getRegion()+"  "+status.getNome()+" "+status.getComune()+" Lazio :"+hospitalCoordinates.equals(statusCoordinates)+" "+hospitalCoordinates+" "+statusCoordinates);
                    //System.err.println(h.getPlaceName()+" "+h.getAddress().getTown()+" "+h.getAddress().getRegion()+"  "+status.getNome()+" "+status.getComune()+" Lazio :"+result.getAddress().getAddress().equals(statusAddress)+" "+result.getAddress().getAddress()+" "+statusAddress);
                    if(hospitalCoordinates.equals(statusCoordinates) || h.getAddress().getAddress().equals(statusAddress) || SphericalUtil.computeDistanceBetween(hospitalCoordinates,statusCoordinates)<800){
                        h.setUpdateDate(status.getUpdateDate());
                        h.setRed_WaitQueue(status.getRed_WaitQueue());
                        h.setYellow_WaitQueue(status.getYellow_WaitQueue());
                        h.setGreen_WaitQueue(status.getGreen_WaitQueue());
                        h.setWhite_WaitQueue(status.getWhite_WaitQueue());
                        h.setNonExec_WaitQueue(status.getNonExec_WaitQueue());
                        h.setRed_TreatQueue(status.getRed_TreatQueue());
                        h.setYellow_TreatQueue(status.getYellow_TreatQueue());
                        h.setGreen_TreatQueue(status.getGreen_TreatQueue());
                        h.setWhite_TreatQueue(status.getWhite_TreatQueue());
                        h.setNonExec_TreatQueue(status.getNonExec_TreatQueue());
                        h.setRed_ObsQueue(status.getRed_ObsQueue());
                        h.setYellow_ObsQueue(status.getYellow_ObsQueue());
                        h.setGreen_ObsQueue(status.getGreen_ObsQueue());
                        h.setWhite_ObsQueue(status.getWhite_ObsQueue());
                        h.setNonExec_ObsQueue(status.getNonExec_ObsQueue());
                        break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void mergeResults(List<Hospital> finalResults,List<Hospital> OverpassHospitals, List<Hospital> ERQueueResults, final LatLng currentPosition,Context context, int limit){
        for(Hospital h: OverpassHospitals){
            try {
                boolean add = true;
                for (Hospital result : ERQueueResults) {
                    //System.err.println(SphericalUtil.computeDistanceBetween(hospitalCoordinates,result.getAddress().getCoordinates())+" "+h.getPlaceName()+" "+result.getPlaceName());
                    if (SphericalUtil.computeDistanceBetween( h.getAddress().getCoordinates(), result.getAddress().getCoordinates()) < 800) {
                        result.changeAddress(h.getAddress());//Better address
                        add = false;
                        break;
                    }
                }
                if (add) {
                    ERQueueResults.add(h);
                }
            }catch(Exception e){}
        }
        Collections.sort(ERQueueResults, new Comparator<Hospital>() {
            @Override
            public int compare(Hospital o1, Hospital o2) {
                return Double.compare(SphericalUtil.computeDistanceBetween(currentPosition, o1.getAddress().getCoordinates()),SphericalUtil.computeDistanceBetween(currentPosition, o2.getAddress().getCoordinates()));
            }
        });
        for(int i=0;i<Math.min(limit,ERQueueResults.size());i++){//Address finalization
            Hospital result=ERQueueResults.get(i);
            finalResults.add(result);
        }
    }
    public String getServiceLink(String region){
        if(region.toLowerCase().equals("lazio")){
            return ServiceLinkLazio;
        }
        return null;
    }
    public void memorizeHospitalsFromJsonResult(String json, String region, Context context, List<Hospital> results){
        if(region.toLowerCase().equals("lazio")){
            try {
                JSONArray array = new JSONObject(json).getJSONObject("result").getJSONArray("records");
                for (int i = 0; i < array.length(); i++) {
                    try {
                        ERStatusLazio status = ERStatusLazio.dataToContainer((JSONObject) array.get(i));
                        LatLng statusCoordinates;
                        if (ERCoordinates.get((status.getNome() + status.getComune()).toLowerCase()) != null) {
                            statusCoordinates = ERCoordinates.get((status.getNome() + status.getComune()).toLowerCase());
                        } else {
                            if (status.getNome().contains("Pol. Univ.")) {
                                statusCoordinates = getLatLngFromAddress("Ospedale " + status.getNome().substring(("Pol. Univ.").length()) + " " + status.getComune() + " Lazio", context, 3);
                            } else {
                                statusCoordinates = getLatLngFromAddress(status.getNome() + " " + status.getComune() + " Lazio", context, 3);
                            }
                            ERCoordinates.put((status.getNome() + status.getComune()).toLowerCase(), statusCoordinates);
                        }
                        String statusAddress;
                        if (ERAddress.get((status.getNome() + status.getComune()).toLowerCase()) != null) {
                            statusAddress = ERAddress.get((status.getNome() + status.getComune()).toLowerCase());
                        } else {
                            statusAddress = getAddressFromLatLng(statusCoordinates, context, 3);
                            ERAddress.put((status.getNome() + status.getComune()).toLowerCase(), statusAddress);
                        }
                        com.model.fasteritaly.Address add = new com.model.fasteritaly.Address(statusAddress, statusCoordinates);
                        Hospital result = new Hospital(status.getNome(), add);
                        result.setUpdateDate(status.getUpdateDate());
                        result.setRed_WaitQueue(status.getRed_WaitQueue());
                        result.setYellow_WaitQueue(status.getYellow_WaitQueue());
                        result.setGreen_WaitQueue(status.getGreen_WaitQueue());
                        result.setWhite_WaitQueue(status.getWhite_WaitQueue());
                        result.setNonExec_WaitQueue(status.getNonExec_WaitQueue());
                        result.setRed_TreatQueue(status.getRed_TreatQueue());
                        result.setYellow_TreatQueue(status.getYellow_TreatQueue());
                        result.setGreen_TreatQueue(status.getGreen_TreatQueue());
                        result.setWhite_TreatQueue(status.getWhite_TreatQueue());
                        result.setNonExec_TreatQueue(status.getNonExec_TreatQueue());
                        result.setRed_ObsQueue(status.getRed_ObsQueue());
                        result.setYellow_ObsQueue(status.getYellow_ObsQueue());
                        result.setGreen_ObsQueue(status.getGreen_ObsQueue());
                        result.setWhite_ObsQueue(status.getWhite_ObsQueue());
                        result.setNonExec_ObsQueue(status.getNonExec_ObsQueue());
                        results.add(result);
                    } catch (Exception ess) {
                    }
                }
            } catch (Exception e) {
            }
        }

    }


    private String getAddressFromLatLng(LatLng coordinates, Context ctx, int tentatives){
        Geocoder coder = new Geocoder(ctx);
        List<Address> address;
        try {
            address = coder.getFromLocation(coordinates.latitude,coordinates.longitude,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            //location.getThoroughfare() = nome via
            //location.getLocality() =  nome comune
            //location.getAdminArea() = nome regione
            //location.getSubAdminArea()= provincia di nomeprovincia
            //location.getFeatureName() = numero civico
            return location.getThoroughfare()+" "+location.getFeatureName()+", "+location.getLocality()+" "+location.getAdminArea();
        }catch(Exception e){
            e.printStackTrace();
            if(tentatives>0) {
                return getAddressFromLatLng(coordinates, ctx,tentatives-1);
            }
            return null;
        }
    }

    private LatLng getLatLngFromAddress(String strAddress, Context ctx, int tentatives){
        Geocoder coder = new Geocoder(ctx);
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
            System.err.println( strAddress);
            e.printStackTrace();
            if(tentatives>0) {
                return getLatLngFromAddress(strAddress, ctx,tentatives-1);
            }
            return null;
        }
    }

    protected String getJSON(String url) {
        return getUrlContents(url);
    }

    private String getUrlContents(String theUrl) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(theUrl);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()), 8);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
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

}

package com.business_logic.fasteritaly.data_helper;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.model.fasteritaly.Hospital;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OverPassAPIHospitalService {

    private static OverPassAPIHospitalService instance;

    private Map<String, Boolean> allHospitals=new HashMap<>();
    private Map<String,Set<Hospital>> RegionHospital=new HashMap<>();

    private int ServerID=0;


    private OverPassAPIHospitalService(){
    }

    public static OverPassAPIHospitalService getInstance() {
        if(instance==null){
            instance=new OverPassAPIHospitalService();
        }else{
            if(instance.allHospitals==null){
                instance.allHospitals=new HashMap<>();
            }
            if(instance.RegionHospital==null){
                instance.RegionHospital=new HashMap<>();
            }
        }
        return instance;
    }


    public void changeServerID(){
        ServerID=(ServerID+1)%5;
    }

    public int getServerID() {
        return ServerID;
    }

    public ArrayList<Hospital> getFinalOrderedResults(final LatLng coordinates, List<Hospital> Totalresults, int limit, Context context ){
        ArrayList<Hospital> results=new ArrayList<>();
        Collections.sort(Totalresults, new Comparator<Hospital>() {
            @Override
            public int compare(Hospital o1, Hospital o2) {
                return Double.compare(SphericalUtil.computeDistanceBetween(coordinates, o1.getAddress().getCoordinates()), SphericalUtil.computeDistanceBetween(coordinates, o2.getAddress().getCoordinates()));
            }
        });
        //System.err.println(Totalresults.size());
        for(int i=0;i<Math.min(limit*(Totalresults.size()<100?3:(Totalresults.size()<200?2:1.7)),Totalresults.size());i++){//Address finalization
            Hospital result=Totalresults.get(i);
            if(CommonAccessData.getInstance().getHospitalByName(result.getPlaceName())!=null){
                result=CommonAccessData.getInstance().getHospitalByName(result.getPlaceName());
            }else{//Use geocoding to find the correct address
                result.changeAddress(new com.model.fasteritaly.Address(getAddressFromLatLng(result.getAddress().getCoordinates(), context, 3), result.getAddress().getLatitude(), result.getAddress().getLongitude()));
                CommonAccessData.getInstance().putHospital(result);
            }
            results.add(result);
        }
        return results;
    }
    public Set<Hospital> getCreatedHospitals(String region){
        try {
            if (allHospitals.get(region.toLowerCase()) != null) {
                try {
                    if (RegionHospital.get(region.toLowerCase()).isEmpty()) {
                        allHospitals.remove(region.toLowerCase());
                        RegionHospital.remove(region.toLowerCase());
                        return null;
                    } else
                        return RegionHospital.get(region.toLowerCase());
                } catch (Exception e) {
                    RegionHospital = new HashMap<>();
                    return null;
                }
            } else {
                return null;
            }
        }catch (Exception er){
            allHospitals=new HashMap<>();
            return null;
        }
    }
    public Map<String, JSONObject> memorizeHospitalsFromJSONresult(String json, String region, List<Hospital> Totalresults){
        Map<String, JSONObject> secondChance = new HashMap<>();
        try{
            JSONArray array = new JSONObject(json).optJSONArray("elements");
            for (int i = 0; i < array.length(); i++) {
                try {
                    OverPassAPIHospital data = OverPassAPIHospital.dataToContainer((JSONObject) array.get(i));
                    com.model.fasteritaly.Address address = new com.model.fasteritaly.Address(data.getNome() + " " + region, data.getLatitude(), data.getLongitude());
                    Hospital hospital = new Hospital(data.getNome(), address);
                    if (RegionHospital.get(region.toLowerCase()) != null) {
                        RegionHospital.get(region.toLowerCase()).add(hospital);
                    } else {
                        Set<Hospital> list = new HashSet<>();
                        list.add(hospital);
                        RegionHospital.put(region.toLowerCase(), list);
                    }
                    Totalresults.add(hospital);

                } catch (Exception e) {
                    try {
                        secondChance.put(((JSONObject) array.get(i)).getJSONArray("nodes").get(0).toString(), (JSONObject) array.get(i));
                    } catch (Exception ex) {}
                    try{
                        secondChance.put(((JSONObject) array.get(i)).getString("id"), (JSONObject) array.get(i));
                    }catch(Exception ex) {}
                }
            }
            allHospitals.put(region.toLowerCase(), true);
        }catch(Exception e){}
        return secondChance;
    }

    public void memorizeOtherHospitalsfromJSONsecondChanceResult( Map<String,JSONObject> secondChance,String region ,String json, List<Hospital> Totalresults){
                if (!secondChance.isEmpty()) {
                    try {
                        //System.err.println(json);
                        JSONArray array = new JSONObject(json).optJSONArray("elements");
                        for (String node : secondChance.keySet()) {
                            try {
                                OverPassAPIHospital data = OverPassAPIHospital.tryDataToContainer(node, array, secondChance.get(node));
                                com.model.fasteritaly.Address address = new com.model.fasteritaly.Address(data.getNome() + " " + region, data.getLatitude(), data.getLongitude());
                                Hospital hospital = new Hospital(data.getNome(), address);
                                if (RegionHospital.get(region.toLowerCase()) != null) {
                                    RegionHospital.get(region.toLowerCase()).add(hospital);
                                } else {
                                    Set<Hospital> list = new HashSet<>();
                                    list.add(hospital);
                                    RegionHospital.put(region.toLowerCase(), list);
                                }
                                Totalresults.add(hospital);
                            } catch (Exception ex) {
                            }
                        }
                    } catch (Exception e) {
                    }
                }
    }

    private String getOverpassProvider(int i){
        switch (i){
            default:
                return null;
            case 0:
                return "https://overpass-api.de/api/interpreter";
            case 1:
                return "https://overpass.kumi.systems/api/interpreter";
            case 2:
                return "https://lz4.overpass-api.de/api/interpreter";
            case 3:
                return "https://z.overpass-api.de/api/interpreter";
            case 4:
                return "http://overpass.openstreetmap.fr/api/interpreter";
        }
    }
    public String getHospitalperRegionLink(String region) {
        if(region!=null) {
            if(region.toLowerCase().equals("lazio")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600040784%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("calabria")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283601783980%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("puglia")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600040095%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("campania")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600040218%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("molise")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600041256%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("basilicata")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600040137%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("abruzzo")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600053937%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("marche")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600053060%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("umbria")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600042004%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("toscana")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600041977%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("liguria")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600301482%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("emilia-romagna")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600042611%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("trentino-alto adige")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600045757%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("friuli-venezia giulia")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600179296%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("piemonte")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600044874%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("lombardia")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600044879%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("veneto")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600043648%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("valle d'aosta")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600045155%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("sardegna")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283606847723%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
            if(region.toLowerCase().equals("sicilia")){
                return getOverpassProvider(ServerID)+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Barea%283600039152%29%2D%3E%2EsearchArea%3B%28node%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Bway%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3Brelation%5B%22amenity%22%3D%22hospital%22%5D%28area%2EsearchArea%29%3B%29%3Bout%3B%0A";
            }
        }
        return null;
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

    public String createNodesRequest(Set<String> nodes){
        String prelink=getOverpassProvider(getServerID())+"?data=%5Bout%3Ajson%5D%5Btimeout%3A60%5D%3Bnode%28id%3A";
        String postLink="%29%3Bout%3B%0A";
        String center="";//%2C as comma
        int i=0;
        for(String node : nodes){
            if(i<nodes.size()-1){
                center=center+node+",";
            }else{
                center=center+node;
            }
            i++;
        }
        return prelink+center+postLink;
    }
}


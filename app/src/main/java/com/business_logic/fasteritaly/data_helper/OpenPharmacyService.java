package com.business_logic.fasteritaly.data_helper;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.model.fasteritaly.Pharmacy;
import com.model.fasteritaly.singleton_and_helpers.CommonAccessData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OpenPharmacyService {

    private static String serviceLink="https://www.farmaciediturno.org/ricercaditurno.asp";

    public static String getServiceLink(){
        if(serviceLink==null){
            serviceLink="https://www.farmaciediturno.org/ricercaditurno.asp";
        }
        return  serviceLink;
    }

    public static Map<String,String> composeDataRequest(LatLng coordinates, String currentAddress, Context ctx){//Control if it is empty
        Map<String,String> res=new HashMap<>();
        try {
            res.put("giorno", "1".trim());
            res.put("md", "Avvia+la+ricerca".trim());
            if(currentAddress!=null){
                res.put("indirizzo",currentAddress.replaceAll("\\s","+").trim());
            }else{
                res.put("indirizzo", getAddressFromLatLng(coordinates,ctx,3).replaceAll("\\s","+").trim());
            }
            Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int hours = cal.get(Calendar.HOUR_OF_DAY);
            int minutes=cal.get(Calendar.MINUTE);
            res.put("orario",(hours+(minutes>=30?"30":"00")).trim());
        }catch (Exception e){
            res.clear();
        }
        return res;
    }

    public static List<Pharmacy> getPharmaciesFromQuery(String html, Context ctx,int limit){
        List<Pharmacy> res=new ArrayList<>();
        Document doc= Jsoup.parse(html);
        Elements elements = doc.getElementsByTag("tr");
        for(Element e:elements){
            Document inner=Jsoup.parse(e.html());
            Elements innerElements=inner.getElementsByTag("tr");
            for(Element e1:innerElements){
                try {
                    OpenPharmacy o = OpenPharmacy.parseNewOpenPharmacy(Jsoup.parse(e1.html()));
                    Pharmacy result;
                    if(CommonAccessData.getInstance().getPharmacyByName(o.getNome()+o.getIndirizzo())!=null){
                        result=CommonAccessData.getInstance().getPharmacyByName(o.getNome()+o.getIndirizzo());
                    }else{
                        //System.err.println(o.getIndirizzo());
                        com.model.fasteritaly.Address address=new com.model.fasteritaly.Address(o.getIndirizzo(), getLatLngFromAddress(o.getIndirizzo(),ctx,3));
                        result=new Pharmacy(o.getNome(),address);
                        CommonAccessData.getInstance().putPharmacy(result);
                    }
                    SearchPhotoService.getInstance().addImageToPlace(result.getPlaceName()+" "+result.getAddress().getAddress(),o.getImmagine());
                    String[] op_times=o.getOrari().split("\\s");
                    if(op_times.length>1){
                        result.setOpeningTimes(new String[]{op_times[0].split("-")[0],op_times[1].split("-")[0]});
                        result.setClosingTimes(new String[]{op_times[0].split("-")[1],op_times[1].split("-")[1]});
                    }else{
                        result.setOpeningTimes(new String[]{op_times[0].split("-")[0],""});
                        result.setClosingTimes(new String[]{op_times[0].split("-")[1],""});
                    }
                    if(!(isCurrentHourInInterval(result.getOpeningTimes()[0],result.getClosingTimes()[0])||isCurrentHourInInterval(result.getOpeningTimes()[1],result.getClosingTimes()[1])) && o.getTurno()!=null){
                        try {
                            op_times = o.getTurno().split("\\s");
                            if (op_times.length > 1) {
                                result.setOpeningTimes(new String[]{op_times[0].split("-")[0], op_times[1].split("-")[0]});
                                result.setClosingTimes(new String[]{op_times[0].split("-")[1], op_times[1].split("-")[1]});
                            } else {
                                result.setOpeningTimes(new String[]{op_times[0].split("-")[0], ""});
                                result.setClosingTimes(new String[]{op_times[0].split("-")[1], ""});
                            }
                        }catch (Exception eg){}
                    }
                    res.add(result);
                }catch(Exception ess){
                    //System.err.println(e1.html());
                    //ess.printStackTrace();
                }
                if(res.size()==limit){
                    break;
                }
            }
        }
        return res;
    }

    private static LatLng getLatLngFromAddress(String strAddress, Context ctx, int tentatives){
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
            if(tentatives>0) {
                return getLatLngFromAddress(strAddress, ctx,tentatives-1);
            }
            return null;
        }
    }
    private static String getAddressFromLatLng(LatLng coordinates, Context ctx, int tentatives){
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

    public static boolean isCurrentHourInInterval(String initialTime, String finalTime) {
        try {
            Calendar now = Calendar.getInstance();

            int hour = now.get(Calendar.HOUR_OF_DAY); // Get hour in 24 hour format
            int minute = now.get(Calendar.MINUTE);
            int hour1 = Integer.parseInt(initialTime.split(":")[0]);
            int minute1 = Integer.parseInt(initialTime.split(":")[1]);
            int hour2 = Integer.parseInt(finalTime.split(":")[0]);
            int minute2 = Integer.parseInt(finalTime.split(":")[1]);


            if (hour >= hour1 && hour <= hour2) {
                if (hour == hour1) {
                    if (minute >= minute1) {

                        return true;
                    }
                } else {
                    if (hour == hour2) {
                        if (minute <= minute2) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }catch(Exception e){}
        return false;
    }
}

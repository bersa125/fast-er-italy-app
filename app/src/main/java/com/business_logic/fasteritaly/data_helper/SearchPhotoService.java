package com.business_logic.fasteritaly.data_helper;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class SearchPhotoService {
    private static final int HTTP_REQUEST_TIMEOUT = 3 * 600000;
    private static SearchPhotoService instance;


    private Map<String, String> SearchToResult=new HashMap<>();

    private SearchPhotoService(){}

    public static SearchPhotoService getInstance() {
        if(instance==null){
            instance=new SearchPhotoService();
        }else{
            if(instance.SearchToResult==null){
                instance.SearchToResult=new HashMap<>();
            }
        }
        return instance;
    }

    public String getImageByPlaceCompleteAddress(String address, Context ctx){
        if(SearchToResult.get(address)!=null){
            return SearchToResult.get(address);
        }else{
            //TODO se esiste un servizio gratuito e piu affidabile usa quello altrimenti ciccia e vai solo di farmacie
            /*
            Customsearch customsearch= null;
            try {
                customsearch = new Customsearch(new NetHttpTransport(),new JacksonFactory(), new HttpRequestInitializer() {
                    public void initialize(HttpRequest httpRequest) {
                        try {
                            // set connect and read timeouts
                            httpRequest.setConnectTimeout(HTTP_REQUEST_TIMEOUT);
                            httpRequest.setReadTimeout(HTTP_REQUEST_TIMEOUT);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            List<Result> resultList=null;
            try {
                Customsearch.Cse.List list=customsearch.cse().list(address);
                list.setKey(ctx.getString(R.string.google_api_key));
                list.setCx("009495262850451702046:y0vu9wa828k");
                list.setImgSize("medium");
                list.setFileType("jpg");
                list.setSearchType("image");
                Search results=list.execute();
                resultList=results.getItems();
            }
            catch (  Exception e) {
                e.printStackTrace();
            }
            if(resultList!=null){
                if(resultList.isEmpty()){
                    return null;
                }else{
                    SearchToResult.put(address,resultList.get(0).getLink());
                    return resultList.get(0).getLink();
                }
            }else{
                return null;
            }
            */
            return null;
        }
    }

    public void addImageToPlace(String place, String linkImage){//Added to help pharmacies
        if(linkImage!=null) {
            SearchToResult.put(place, linkImage);
        }
    }

}

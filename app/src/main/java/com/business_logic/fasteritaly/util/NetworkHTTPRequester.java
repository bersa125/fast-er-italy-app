package com.business_logic.fasteritaly.util;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import static com.facebook.FacebookSdk.getCacheDir;

public class NetworkHTTPRequester {
    private static NetworkHTTPRequester ourInstance = new NetworkHTTPRequester();

    private Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024 * 3 );//1 Mb
    private Network network = new BasicNetwork(new HurlStack());
    private RequestQueue requestQueue=new RequestQueue(cache,network);
    private boolean queueStarted=false;

    private Map<String, Integer> requestsStatusbyTag=new HashMap<>();

    public static NetworkHTTPRequester getInstance() {
        if(ourInstance==null){
            ourInstance = new NetworkHTTPRequester();
        }else{
            if(ourInstance.cache==null){
                ourInstance.cache = new DiskBasedCache(getCacheDir(), 1024 * 1024 * 3 );
            }
            if(ourInstance.network==null){
                ourInstance.network = new BasicNetwork(new HurlStack());
            }
            if(ourInstance.requestsStatusbyTag==null){
                ourInstance.requestsStatusbyTag=new HashMap<>();
            }
            if(ourInstance.requestQueue==null){
                ourInstance.requestQueue=new RequestQueue(ourInstance.cache,ourInstance.network);
                ourInstance.queueStarted=false;
            }
        }
        if(!ourInstance.queueStarted){
            ourInstance.queueStarted=true;
            ourInstance.requestQueue.start();
        }
        return ourInstance;
    }

    public void makeGetStringRequest(String url, Response.Listener<String> listener, Response.ErrorListener error, String tag){
        StringRequest stringRequest = new StringRequest(Request.Method.GET,url,listener,error);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setTag(tag);
        requestQueue.add(stringRequest);
        addActiveRequest(tag);
    }

    public void makePostStringRequest(String url, Response.Listener<String> listener, Response.ErrorListener error, final Map<String,String> data, String tag){
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url,listener,error){
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return data;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000, 3, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
        stringRequest.setTag(tag);
        requestQueue.add(stringRequest);
        addActiveRequest(tag);
    }

    public void makePostStringRequest(String url, Response.Listener<String> listener, Response.ErrorListener error, String tag){
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url,listener,error);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setTag(tag);
        requestQueue.add(stringRequest);
        addActiveRequest(tag);
    }

    public void makePutStringRequest(String url, Response.Listener<String> listener, Response.ErrorListener error, String tag){
        StringRequest stringRequest = new StringRequest(Request.Method.PUT,url,listener,error);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setTag(tag);
        requestQueue.add(stringRequest);
        addActiveRequest(tag);
    }

    public void makeDeleteStringRequest(String url, Response.Listener<String> listener, Response.ErrorListener error, String tag){
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE,url,listener,error);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setTag(tag);
        requestQueue.add(stringRequest);
        addActiveRequest(tag);
    }

    public int getActiveRequests(String tag){
        if(tag!=null){
            if(this.requestsStatusbyTag.get(tag)!=null){
               return this.requestsStatusbyTag.get(tag);
            }
        }
        return 0;
    }
    public void freeRequestSlot(String tag){
        if(tag!=null){
            if(this.requestsStatusbyTag.get(tag)!=null){
                this.requestsStatusbyTag.put(tag,this.requestsStatusbyTag.get(tag)-1);
            }
        }
    }
    private void addActiveRequest(String tag){
        if(tag!=null){
            if(this.requestsStatusbyTag.get(tag)!=null){
                this.requestsStatusbyTag.put(tag,this.requestsStatusbyTag.get(tag)+1);
            }else{
                this.requestsStatusbyTag.put(tag,1);
            }
        }
    }


    private NetworkHTTPRequester() {}


}

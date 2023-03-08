package it.univaq.app.carapp.Utility.Volley;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import it.univaq.app.carapp.Service.DataLayerListenerService;

public class RequestVolley {
    private RequestQueue queue;
    private RequestVolley(Context context){
        queue = Volley.newRequestQueue(context);
    }
    private volatile static RequestVolley instance = null;
    public final static String ID_HOST_CARAPP = "192.168.1.74";

    public synchronized static RequestVolley getInstance(Context context) {
        if(instance == null) {
            synchronized (RequestVolley.class) {
                if(instance == null) instance = new RequestVolley(context);
            }
        }
        return instance;
    }

    public void doGetRequest(String urlAddress, OnCompleteCallback callback) {
        StringRequest request = new StringRequest(urlAddress, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(callback != null){
                    callback.onCompleted(response);
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(callback != null){
                    Log.v(DataLayerListenerService.TAG, "Errore richiesta: "+error.getLocalizedMessage());
                    callback.onCompleted(null);
                }
            }
        });
        queue.add(request);
    }

    public interface OnCompleteCallback {
        void onCompleted(String response);
    }
}

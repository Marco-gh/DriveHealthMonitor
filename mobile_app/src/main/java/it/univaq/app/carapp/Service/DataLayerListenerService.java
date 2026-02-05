package it.univaq.app.carapp.Service;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import it.univaq.app.carapp.Model.Tracking;
import it.univaq.app.carapp.Utility.RoomDB.DB;
import it.univaq.app.carapp.Utility.Volley.RequestVolley;

public class DataLayerListenerService extends WearableListenerService {

    public static final String TAG = "Mobile_App_LASEDE";
    private static final String MANAGE_TRACKINGS_PATH = "/manage_trackings";
    private ConnectivityManager connectivityManager;
    private Tracking trackingToManage = null;

    @Override
    public void onCreate() {
        super.onCreate();

        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(ConnectivityManager.class);
        }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (MANAGE_TRACKINGS_PATH.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                    String message = dataMapItem.getDataMap().getString("message");
                    Gson gson = new Gson();
                    trackingToManage = gson.fromJson(message, Tracking.class);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DB.getInstance(getApplicationContext()).getSessionDAO().insert(trackingToManage);
                        }
                    }).start();
                    Log.e(TAG, "JsonFunge?: " + message);

                    //da memorizzare in un db in locale oppure mandarli on line
                    NetworkRequest networkRequest = new NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                            .build();
                    connectivityManager.requestNetwork(networkRequest, networkCallback);

                } else {
                    Log.e(TAG, "Unrecognized path: " + path);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.v(TAG, "Data deleted : " + event.getDataItem().toString());
            } else {
                Log.e(TAG, "Unknown data event Type = " + event.getType());
            }
        }
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);

            List<Tracking> listToWeb = DB.getInstance(getApplicationContext()).getSessionDAO().findAll();
            for(Tracking t : listToWeb){
                Float trBPM = null;
                Float trO2inBlood = null;
                Float accX = null;
                Float accY = null;
                Float accZ = null;
                if(t.getBpm()!=null){
                    trBPM = t.getBpm();
                }
                if(t.getAccelerometer()!=null){
                    accX = t.getAccelerometer()[0];
                    accY = t.getAccelerometer()[1];
                    accZ = t.getAccelerometer()[2];
                }
                if(t.getO2inBlood()!=null){
                    trO2inBlood = t.getO2inBlood();
                }

                String Url = "http://"+RequestVolley.ID_HOST_CARAPP+"/carapp.php?action=insert&deviceID="+t.getDeviceID()+"&date="+t.getDate()+"&bpm="+trBPM+"&o2InBlood="+trO2inBlood+"&accelerationX="+accX+"&accelerationY="+accY+"&accelerationZ="+accZ+"";
                Log.v(DataLayerListenerService.TAG, "url: "+Url);
                RequestVolley.getInstance(getApplicationContext()).doGetRequest(Url,
                        new RequestVolley.OnCompleteCallback() {
                            @Override
                            public void onCompleted(String response) {
                                Log.v(DataLayerListenerService.TAG, "Data on server");
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DB.getInstance(getApplicationContext()).getSessionDAO().remove(t);
                                    }
                                }).start();
                            }
                        });
            }
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DB.getInstance(getApplicationContext()).getSessionDAO().insert(trackingToManage);
                    Log.v(TAG, "Insert fatto di: " + trackingToManage.toString());
                    Log.v(TAG, "DATABASE IN LOCALE------------------------------------------------------");
                    for(Tracking tracking1 : DB.getInstance(getApplicationContext()).getSessionDAO().findAll()){
                        Log.v(TAG, "DB: "+tracking1.toString());
                    }
                }
            }).start();
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            final boolean unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        }
    };
}
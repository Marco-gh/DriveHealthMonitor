package it.univaq.app.carapp.Service;

import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import it.univaq.app.carapp.Model.Tracking;
import it.univaq.app.carapp.Utility.RoomDB.DB;

public class DataLayerListenerService extends WearableListenerService {

    public static final String TAG = "Mobile_App_LASEDE";
    private static final String MANAGE_TRACKINGS_PATH = "/manage_trackings";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged in StorageService");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (MANAGE_TRACKINGS_PATH.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    String message = dataMapItem.getDataMap().getString("message");
                    //da memorizzare in un db in locale oppure mandarli on line, o entrambe le cose
                    Gson gson = new Gson();
                    Tracking tracking = gson.fromJson(message, Tracking.class);

                    //Per evitare che sia null
                    tracking.setDate(LocalDateTime.now());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DB.getInstance(getApplicationContext()).getSessionDAO().insert(tracking);
                            Log.v(TAG, "Insert fatto di: " + tracking.toString());
                            Log.v(TAG, "DATABASE IN LOCALE------------------------------------------------------");
                            for(Tracking tracking1 : DB.getInstance(getApplicationContext()).getSessionDAO().findAll()){
                                Log.v(TAG, "DB: "+tracking1.toString());
                            }
                        }
                    }).start();
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
}
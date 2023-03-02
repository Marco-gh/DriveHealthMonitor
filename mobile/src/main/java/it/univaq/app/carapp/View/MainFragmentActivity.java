package it.univaq.app.carapp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import it.univaq.app.carapp.Model.Session;
import it.univaq.app.carapp.R;
import it.univaq.app.carapp.Utility.RoomDB.DB;

public class MainFragmentActivity extends AppCompatActivity implements DataClient.OnDataChangedListener {
    String datapath = "/data_path";
    String TAG = "Mobile MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainfragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged: " + dataEventBuffer);
        List<Session> sessionsToBeSaved = new ArrayList<>();
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (datapath.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    String message = dataMapItem.getDataMap().getString("message");
                    //da memorizzare in un db in locale oppure mandarli on line, o entrambe le cose
                    Log.v(TAG, "Wear activity received message: " + message);

                    Gson gson = new Gson();
                    Session session = gson.fromJson(message, Session.class);
                    sessionsToBeSaved.add(session);
                } else {
                    Log.e(TAG, "Unrecognized path: " + path);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.v(TAG, "Data deleted : " + event.getDataItem().toString());
            } else {
                Log.e(TAG, "Unknown data event Type = " + event.getType());
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(Session session : sessionsToBeSaved){
                    DB.getInstance(getApplicationContext()).getSessionDAO().insert(session);
                }
            }
        }).start();

    }

}
package it.univaq.app.carapp.View.Fragment;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import it.univaq.app.carapp.Model.Tracking;
import it.univaq.app.carapp.R;
import it.univaq.app.carapp.Service.DataLayerListenerService;
import it.univaq.app.carapp.Utility.RoomDB.DB;
import it.univaq.app.carapp.Utility.Volley.RequestVolley;

public class DetailFragment extends Fragment {

    private ConnectivityManager connectivityManager;
    private String date = "";

    private ArrayList<Tracking> trackings_this_day = new ArrayList<>();
    private ArrayList<Float> array_bpm = new ArrayList<>();
    private ArrayList<Float> array_O2InBlood = new ArrayList<>();
    private ArrayList<Float> array_accelerationX = new ArrayList<>();
    private ArrayList<Float> array_accelerationY = new ArrayList<>();
    private ArrayList<Float> array_accelerationZ = new ArrayList<>();

    private TextView textViewBPM;
    private TextView textViewMaxBpm;
    private TextView textViewMinBpm;
    private TextView textViewAverageBpm;
    private TextView textViewO2InBlood;
    private TextView textViewMaxO2IBlood;
    private TextView textViewMinO2IBlood;
    private TextView textViewAverageO2IBlood;
    private TextView textViewAcceleration;
    private TextView textViewMaxAcceleration;
    private TextView textViewMinAcceleration;
    private TextView textViewAverageAcceleration;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();
        connectivityManager = (ConnectivityManager) requireContext().getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, networkCallback);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textViewMaxBpm = view.findViewById(R.id.textViewMaxValueBpm);
        textViewMinBpm = view.findViewById(R.id.textViewMinValueBpm);
        textViewAverageBpm = view.findViewById(R.id.textViewAverageValueBpm);

        textViewMaxO2IBlood = view.findViewById(R.id.textViewMaxValueO2InBlood);
        textViewMinO2IBlood = view.findViewById(R.id.textViewMinValueO2InBlood);
        textViewAverageO2IBlood = view.findViewById(R.id.textViewAverageValueO2InBlood);

        textViewMaxAcceleration = view.findViewById(R.id.textViewMaxValueAccelerations);
        textViewMinAcceleration = view.findViewById(R.id.textViewMinValueAccelerations);
        textViewAverageAcceleration = view.findViewById(R.id.textViewAverageValueAccelerations);

        if(getArguments() != null) {
            date = getArguments().getString(ListFragment.KEY_EXTRA_DATA);
        }
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);

            String Url = "http://"+ RequestVolley.ID_HOST_CARAPP+"/carapp.php?action=query&all=true";
            RequestVolley.getInstance(getContext()).doGetRequest(Url,
                    new RequestVolley.OnCompleteCallback() {
                        @Override
                        public void onCompleted(String response) {
                            try{
                                if(response != null){
                                    JSONArray jsonArray = new JSONArray(response);
                                    Gson gson = new Gson();
                                    for (int i=0;i<jsonArray.length();i++){

                                        Tracking tracking = gson.fromJson(jsonArray.getJSONObject(i).toString(), Tracking.class);
                                        tracking.setAccelerometer(new Float[]{Float.parseFloat(jsonArray.getJSONObject(i).get("accelerationX").toString()),
                                                Float.parseFloat(jsonArray.getJSONObject(i).get("accelerationY").toString()),
                                                Float.parseFloat(jsonArray.getJSONObject(i).get("accelerationZ").toString())});
                                        String onlyDateTracking = tracking.getDate().split("T")[0];

                                        if(Objects.equals(onlyDateTracking, date)){
                                            if(tracking.getO2inBlood() != null){
                                                array_O2InBlood.add(tracking.getO2inBlood());
                                            }
                                            array_bpm.add(tracking.getBpm());
                                            array_accelerationX.add(tracking.getAccelerometer()[0]);
                                            array_accelerationY.add(tracking.getAccelerometer()[1]);
                                            array_accelerationZ.add(tracking.getAccelerometer()[2]);
                                        }
                                    }

                                    if(array_bpm.size()>0){
                                        textViewMaxBpm.setText(String.format("Max: %s", Collections.max(array_bpm)));
                                        textViewMinBpm.setText(String.format("Min: %s", Collections.min(array_bpm)));

                                        Float sum = 0.0f;
                                        for(Float f : array_bpm){
                                            sum += f;
                                        }
                                        Float averageBPM = sum/array_bpm.size();
                                        Log.v(DataLayerListenerService.TAG, "array bpm: "+ averageBPM);
                                        textViewAverageBpm.setText(String.format("Average: %s", averageBPM));
                                    }
                                    if(array_O2InBlood.size()>0){
                                        textViewMaxO2IBlood.setText(String.format("Max: %s", Collections.max(array_O2InBlood)));
                                        textViewMinO2IBlood.setText(String.format("Min: %s", Collections.min(array_O2InBlood)));

                                        Float sum = 0.0f;
                                        for(Float f : array_O2InBlood){
                                            sum += f;
                                        }
                                        Float average = sum/array_O2InBlood.size();
                                        textViewAverageO2IBlood.setText(String.format("Average: %s", average));
                                    } else if(array_O2InBlood.size()==0){
                                        textViewMaxO2IBlood.setText("---");
                                        textViewMinO2IBlood.setText("---");
                                        textViewAverageO2IBlood.setText("---");
                                    }
                                    if(array_accelerationX.size()>0 && array_accelerationY.size()>0 && array_accelerationZ.size()>0){
                                        textViewMaxAcceleration.setText(String.format("Max: %s / %s / %s", Collections.max(array_accelerationX),Collections.max(array_accelerationY),Collections.max(array_accelerationZ)));
                                        textViewMinAcceleration.setText(String.format("Min: %s / %s / %s", Collections.min(array_accelerationX),Collections.min(array_accelerationY),Collections.min(array_accelerationZ)));

                                        Float[] sum = {0.0f,0.0f,0.0f};
                                        for(Float f : array_accelerationX){
                                            sum[0] += f;
                                        }
                                        for(Float f : array_accelerationY){
                                            sum[1] += f;
                                        }
                                        for(Float f : array_accelerationZ){
                                            sum[2] += f;
                                        }
                                        Float[] average = {sum[0]/array_accelerationX.size(),sum[1]/array_accelerationX.size(),sum[2]/array_accelerationX.size()};
                                        textViewAverageAcceleration.setText(String.format("Average: %s / %s / %s", average[0], average[1], average[2]));
                                    }
                                }
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    });
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.v(DataLayerListenerService.TAG, "Data from RoomDB "+ DB.getInstance(getContext()).getSessionDAO().findAll().toString());
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

package it.univaq.app.carapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import it.univaq.app.carapp.Model.Tracking;
import it.univaq.app.carapp.Utility.FileUtility;
import it.univaq.app.carapp.databinding.ActivityMainBinding;

public class MainActivity extends Activity implements SensorEventListener {

    private ActivityMainBinding binding;
    private static final int TYPE_lifeq_lel_spo2 = 65561;
    private static final int ACCELERATION_SENSOR = Sensor.TYPE_ACCELEROMETER;
    private static final String MANAGE_TRACKINGS_PATH = "/manage_trackings";
    public final static String TAG = "Wear MainActivity";

    private boolean thereIsO2Sensor = false;
    private boolean startModeButton = true;

    private Sensor SensorBPM;
    private Sensor SensorAccelerometer;
    //private Sensor SensorOffBody;
    private Sensor sensorBloodOxygen;
    private SensorManager mSensorManager;
    private int accuracyHeartRate;

    private Tracking current_tracking;
    private ArrayList<Float> samplings_bpm = new ArrayList<>();
    private ArrayList<Float> samplings_o2InBlood = new ArrayList<>();
    private ArrayList<Float> samplings_accelerometerX = new ArrayList<>();
    private ArrayList<Float> samplings_accelerometerY = new ArrayList<>();
    private ArrayList<Float> samplings_accelerometerZ = new ArrayList<>();

    private final int mIntervalSession = 15000;
    private final int max_number_of_samplings = 3;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        current_tracking = null;
        mHandler = new Handler(getMainLooper());

        binding.imageViewCar.setVisibility(View.INVISIBLE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        binding.buttonStartStop.setText(R.string.start);

        binding.buttonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startModeButton){
                    binding.imageViewCar.setVisibility(View.VISIBLE);
                    binding.buttonStartStop.setText(R.string.stop);

                    if (mSensorManager != null) {
                        requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, 1);
                        Log.d(TAG, "permessi chiesti ");

                    }
                    setAllSensorTextViews("0");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startRepeatingTask();
                        }
                    }).start();

                    startModeButton = !startModeButton;
                }
                else{
                    binding.buttonStartStop.setText(R.string.start);

                    binding.imageViewCar.setVisibility(View.INVISIBLE);
                    mSensorManager.unregisterListener(MainActivity.this);
                    setAllSensorTextViews("-");

                    stopRepeatingTask();

                    startModeButton = !startModeButton;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager = null;
        stopRepeatingTask();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            registerSensorsInBody();
            Log.d(TAG, "permessi dati");

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /*
        if(event.sensor.getType()==Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT){
            this.offBody = event.values[0] == 0.0;
            if(offBody){
                binding.textViewOutOfBody.setVisibility(View.VISIBLE);
            }
            else{
                binding.textViewOutOfBody.setVisibility(View.INVISIBLE);
            }
        }*/
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE && accuracyHeartRate != SensorManager.SENSOR_STATUS_UNRELIABLE &&
                accuracyHeartRate != SensorManager.SENSOR_STATUS_NO_CONTACT) {

            if (samplings_bpm.size() < max_number_of_samplings && event.values[0]!=0.0) {
                samplings_bpm.add(event.values[0]);
            }
            else if(samplings_bpm.size() < max_number_of_samplings && event.values[0]==0.0){
                Log.d(TAG, "Rilevazione BPM nulla: " + event.values[0]);
            }
            else if(samplings_bpm.size() == max_number_of_samplings){
                mSensorManager.unregisterListener(this, SensorBPM);
            }

            if(event.values[0]!=0.0){
                binding.textViewBPM.setText(String.format("BPM: %s",event.values[0]));
            }
            Log.d(TAG, "Rilevazione BPM: " + event.values[0]);

        }
        else if (event.sensor.getType() == ACCELERATION_SENSOR) {
            Float x = event.values[0];
            Float y = event.values[1];
            Float z = event.values[2];

            if (samplings_accelerometerX.size()< max_number_of_samplings) {
                samplings_accelerometerX.add(x);
                samplings_accelerometerY.add(y);
                samplings_accelerometerZ.add(z);
            }
            else{
                mSensorManager.unregisterListener(this, SensorAccelerometer);
            }

            binding.textViewLinearAccelerationX.setText(String.format("X: %s",x));
            binding.textViewLinearAccelerationY.setText(String.format("Y: %s",y));
            binding.textViewLinearAccelerationZ.setText(String.format("Z: %s",z));

            Log.d(TAG, "Rilevazione accelerazione: " + String.format("X: %s, Y: %s,Z: %s",x,y,z));

        }
        else if (mSensorManager.getDefaultSensor(TYPE_lifeq_lel_spo2) != null && event.sensor.getType() == TYPE_lifeq_lel_spo2) {

            Float percentual_o2 = null;
            if (event.values[0] == 0.0 && event.values[1] != 0.0) {
                percentual_o2 = event.values[1];
            } else if (event.values[1] == 0.0 && event.values[0] != 0.0) {
                percentual_o2 = event.values[0];
            } else if (event.values[0] == 0.0 && event.values[1] == 0.0){
                percentual_o2 = 0.0f;
            }
            else {
                percentual_o2 = Math.max(event.values[0], event.values[1]);
            }

            if ( samplings_o2InBlood.size() < max_number_of_samplings && percentual_o2>40) {
                samplings_o2InBlood.add(percentual_o2);

                binding.textViewO2InBlood.setText(String.format("O2: %s", percentual_o2));
                Log.d(TAG, "Rilevazione o2: " + percentual_o2);
            }
            else if( samplings_o2InBlood.size() < max_number_of_samplings && percentual_o2<40){
                Log.d(TAG, "Rilevazione o2 troppo bassa: " + percentual_o2);
            }
            else{
                mSensorManager.unregisterListener(this, sensorBloodOxygen);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_HEART_RATE) {
            this.accuracyHeartRate = accuracy;
            Log.d(TAG, "Accuracy BPM: " + accuracy);
        }
    }

    public void setAllSensorTextViews(String s) {
        binding.textViewBPM.setText(String.format("BPM: %s", s));
        binding.textViewLinearAccelerationX.setText(String.format("X: %s", s));
        binding.textViewLinearAccelerationY.setText(String.format("Y: %s", s));
        binding.textViewLinearAccelerationZ.setText(String.format("Z: %s", s));
        if(thereIsO2Sensor){
            binding.textViewO2InBlood.setText(String.format("O2: %s", s));
        }
    }

    public void registerSensorsOutBody() {
        SensorAccelerometer = mSensorManager.getDefaultSensor(ACCELERATION_SENSOR);
        mSensorManager.registerListener(this, SensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //SensorOffBody = mSensorManager.getDefaultSensor(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT);
        //mSensorManager.registerListener(this, SensorOffBody, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void registerSensorsInBody() {
        SensorBPM = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mSensorManager.registerListener(this, SensorBPM, SensorManager.SENSOR_DELAY_NORMAL);
        if (mSensorManager.getDefaultSensor(TYPE_lifeq_lel_spo2) != null) {
            thereIsO2Sensor = true;
            sensorBloodOxygen = mSensorManager.getDefaultSensor(TYPE_lifeq_lel_spo2);
            mSensorManager.registerListener(this, sensorBloodOxygen, SensorManager.SENSOR_DELAY_NORMAL);
        }
        Log.d(TAG, "Sensori in corpo registrati, o2 presente: "+thereIsO2Sensor);
    }

    //////////////////////////////TROVARE I NODI VICINI/////////////////////////////////////////////
    private static final String MANAGE_TRACKING_CAPABILITY_NAME = "manage_tracking";
    private String manageTrackingNodeId = null;

    private void setupManageTracking() throws ExecutionException, InterruptedException {
        CapabilityInfo capabilityInfo = Tasks.await(
                Wearable.getCapabilityClient(getApplicationContext()).getCapability(
                        MANAGE_TRACKING_CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE));
        updateTranscriptionCapability(capabilityInfo);
    }

    private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();
        manageTrackingNodeId = pickBestNodeId(connectedNodes);
    }

    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    /////////////////////////////////////INVIARE DATI///////////////////////////////////////////////
    private void sendData(String message) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(MANAGE_TRACKINGS_PATH);
        dataMap.getDataMap().putString("message", message);
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();
        Log.d(TAG, "Prima dell'invio sulla rete: "+message);

        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        dataItemTask
                .addOnSuccessListener(new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d(TAG, "Message sended: " + dataItem + ", message: " + message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Sending message failed: " + e);
                        FileUtility.writeTracking(getApplicationContext(), message);
                    }
                })
        ;
    }

    ///////////////////////////////////////TASK RIPETUTI////////////////////////////////////////////
    Runnable sessionManager = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        registerSensorsOutBody();
                        registerSensorsInBody();

                        Log.d(TAG, "valore array bpm: "+samplings_bpm);
                        Log.d(TAG, "valore array o2: "+samplings_o2InBlood);
                        Log.d(TAG, "valore array X: "+samplings_accelerometerX);
                        Log.d(TAG, "valore array Y: "+samplings_accelerometerY);
                        Log.d(TAG, "valore array Z: "+samplings_accelerometerZ);


                        if(current_tracking!=null){
                            Log.d(TAG, "ci sono valori validi? -> "+current_tracking.toString());
                            visualizeAndStoresInCurrentTracking();
                            Log.d(TAG, "current_tracking: "+current_tracking.toString());
                            if(current_tracking.getBpm()!=null || current_tracking.getAccelerometer()!=null || current_tracking.getO2inBlood()!=null){
                                try {
                                    setupManageTracking();

                                    GsonBuilder gsonBuilder = new GsonBuilder();
                                    gsonBuilder.serializeSpecialFloatingPointValues();
                                    String json_current_tracking = gsonBuilder.create().toJson(current_tracking);
                                    FileUtility.writeTracking(getApplicationContext(), json_current_tracking);
                                    Log.d(TAG, "Scritto in memoria: "+json_current_tracking);
                                    Log.d(TAG, "nodo connesso: "+manageTrackingNodeId);

                                    if (manageTrackingNodeId != null) {
                                        String[] jsonArrayFromFile = FileUtility.readTrackings(getApplicationContext());
                                        if(jsonArrayFromFile != null){
                                            for(String str : jsonArrayFromFile){
                                                Log.d(TAG, "Invio dati file: "+str);
                                                sendData(str);
                                            }
                                        }
                                    }
                                } catch (ExecutionException | InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    } finally {
                        current_tracking = new Tracking(getApplicationContext());
                        samplings_bpm = new ArrayList<>();
                        samplings_o2InBlood = new ArrayList<>();
                        samplings_accelerometerX = new ArrayList<>();
                        samplings_accelerometerY = new ArrayList<>();
                        samplings_accelerometerZ = new ArrayList<>();
                        mHandler.postDelayed(sessionManager, mIntervalSession);
                    }
                }
            }).start();
        }
    };

    public void startRepeatingTask() {
        sessionManager.run();
    }

    public void stopRepeatingTask() {
        mHandler.removeCallbacks(sessionManager);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void visualizeAndStoresInCurrentTracking(){
        Float totalBPM = 0.0f;
        for(Float f : samplings_bpm){
            totalBPM += f;
        }
        if(samplings_bpm.size() > 0) {
            Float averageBPM = totalBPM / samplings_bpm.size();
            Log.d(TAG, "current_tracking BPM: "+averageBPM);
            if(!Float.isNaN(averageBPM)) {
                current_tracking.setBpm(averageBPM);
            }
            else{current_tracking.setBpm(null);}
        }

        Float totalX = 0.0f;
        Float totalY = 0.0f;
        Float totalZ = 0.0f;
        for(Float f : samplings_accelerometerX){
            if(f!=null){
                totalX += f;
            }
        }
        for(Float f : samplings_accelerometerY){
            if(f!=null){
                totalY += f;
            }
        }
        for(Float f : samplings_accelerometerZ){
            if(f!=null){
                totalZ += f;
            }
        }
        Float averageX = 0.0f;
        Float averageY = 0.0f;
        Float averageZ = 0.0f;
        if(samplings_accelerometerX.size() != 0){
            averageX = totalX/samplings_accelerometerX.size();
            averageY = totalY/samplings_accelerometerY.size();
            averageZ = totalZ/samplings_accelerometerZ.size();
            if(!Float.isNaN(averageX) && (!Float.isNaN(averageY)) && (!Float.isNaN(averageZ))) current_tracking.setAccelerometer(new Float[]{averageX, averageY, averageZ});
            else{current_tracking.setAccelerometer(new Float[]{null, null, null});}
        }

        Float totalO2 = 0.0f;
        for(Float f : samplings_o2InBlood){
            totalO2 += f;
        }
        if(samplings_o2InBlood.size() != 0) {
            Float averageO2 = totalO2 / samplings_o2InBlood.size();
            current_tracking.setO2inBlood(averageO2);
            if(!Float.isNaN(averageO2)) current_tracking.setO2inBlood(averageO2);
            else{current_tracking.setO2inBlood(null);}
        }
    }
}
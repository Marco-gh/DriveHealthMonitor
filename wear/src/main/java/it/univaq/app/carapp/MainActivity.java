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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
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

    private int mIntervalSession = 10000;
    private int number_of_samplings_start_session = 10;
    private Handler mHandler;

    private ArrayList<String> jsonArrayListFromFile = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setAllSensorTextViews("-");

        current_tracking = new Tracking(getApplicationContext());
        mHandler = new Handler(getMainLooper());

        binding.imageViewCar.setVisibility(View.INVISIBLE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        binding.buttonStop.setEnabled(false);
        binding.textViewOutOfBody.setVisibility(View.INVISIBLE);

        binding.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.imageViewCar.setVisibility(View.VISIBLE);

                binding.buttonStart.setEnabled(false);
                binding.buttonStop.setEnabled(true);
                if (mSensorManager != null) {
                    requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, 1);
                }
                setAllSensorTextViews("0");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startRepeatingTask();
                    }
                }).start();
            }
        });
        binding.buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.buttonStart.setEnabled(true);
                binding.buttonStop.setEnabled(false);
                binding.imageViewCar.setVisibility(View.INVISIBLE);
                mSensorManager.unregisterListener(MainActivity.this);
                setAllSensorTextViews("-");

                //FERMA I RILEVAMENTI
                stopRepeatingTask();
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

            if (samplings_bpm.size()< number_of_samplings_start_session) {
                samplings_bpm.add(event.values[0]);
            }
            if(samplings_bpm.size()== number_of_samplings_start_session){
                Float totalBPM = 0.0f;
                for(Float f : samplings_bpm){
                    totalBPM += f;
                }
                Float averageBPM = totalBPM/samplings_bpm.size();
                current_tracking.setBpm(averageBPM);
                mSensorManager.unregisterListener(this, SensorBPM);

                binding.textViewBPM.setText(String.valueOf(event.values[0]));
            }
        }
        else if (event.sensor.getType() == ACCELERATION_SENSOR) {
            Float x = event.values[0];
            Float y = event.values[1];
            Float z = event.values[2];

            if (current_tracking.getAccelerometer() == null && samplings_accelerometerX.size()< number_of_samplings_start_session) {
                samplings_accelerometerX.add(x);
                samplings_accelerometerY.add(y);
                samplings_accelerometerZ.add(z);
            }
            if(samplings_accelerometerX.size()== number_of_samplings_start_session){
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
                Float averageX = totalX/samplings_accelerometerX.size();
                Float averageY = totalY/samplings_accelerometerY.size();
                Float averageZ = totalZ/samplings_accelerometerZ.size();
                current_tracking.setAccelerometer(new Float[]{averageX, averageY, averageZ});
                mSensorManager.unregisterListener(this, SensorAccelerometer);

                binding.textViewLinearAccelerationX.setText(String.valueOf(x));
                binding.textViewLinearAccelerationY.setText(String.valueOf(y));
                binding.textViewLinearAccelerationZ.setText(String.valueOf(z));
            }
        }
        else if (mSensorManager.getDefaultSensor(TYPE_lifeq_lel_spo2) != null && event.sensor.getType() == TYPE_lifeq_lel_spo2) {
            Log.d(TAG, "Blood O2: " + Arrays.toString(event.values));

            Float percentual_o2 = null;
            if (event.values[0] == 0.0 && event.values[1] != 0.0) {
                percentual_o2 = event.values[1];
            } else if (event.values[1] == 0.0 && event.values[0] != 0.0) {
                percentual_o2 = event.values[1];
            } else if (event.values[0] == 0.0 && event.values[1] == 0.0){
                percentual_o2 = 0.0f;
            }
            else {
                percentual_o2 = Math.max(event.values[0], event.values[1]);
            }

            if (current_tracking.getO2inBlood() == null && samplings_o2InBlood.size()< number_of_samplings_start_session) {
                samplings_o2InBlood.add(percentual_o2);
            }
            if(samplings_o2InBlood.size()== number_of_samplings_start_session){
                Float totalO2 = 0.0f;
                for(Float f : samplings_o2InBlood){
                    totalO2 += f;
                }
                Float averageO2 = totalO2/samplings_o2InBlood.size();
                current_tracking.setO2inBlood(averageO2);
                mSensorManager.unregisterListener(this, sensorBloodOxygen);

                binding.textViewO2InBlood.setText(percentual_o2.toString());
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
        binding.textViewBPM.setText(s);
        binding.textViewLinearAccelerationX.setText(s);
        binding.textViewLinearAccelerationY.setText(s);
        binding.textViewLinearAccelerationZ.setText(s);
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

        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        dataItemTask
                .addOnSuccessListener(new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d(TAG, "Sending message failed: " + dataItem + ", message: " + message);
                        if(jsonArrayListFromFile.contains(message)){
                            jsonArrayListFromFile.remove(message);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Sending message failed: " + e);
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
                    registerSensorsOutBody();
                    registerSensorsInBody();
                    Log.d(TAG, "Registrati i sensori");
                    try {
                        if (current_tracking.hasValues(thereIsO2Sensor)) {
                            try {
                                setupManageTracking();

                                Gson gson = new Gson();
                                String json_current_tracking = gson.toJson(current_tracking);
                                FileUtility.writeTracking(getApplicationContext(), json_current_tracking);
                                Log.d(TAG, "Scritto in memoria: "+json_current_tracking);

                                if (manageTrackingNodeId != null) {
                                    jsonArrayListFromFile = FileUtility.readTrackings(getApplicationContext());
                                    if(jsonArrayListFromFile !=null){
                                        for(String str : jsonArrayListFromFile){
                                            sendData(str);
                                            Log.d(TAG, "Invio dato in file: "+str);
                                        }
                                    }
                                }
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (jsonArrayListFromFile.size() > 0) {
                                for(String s : jsonArrayListFromFile){
                                    FileUtility.writeTracking(getApplicationContext(), s);
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
}
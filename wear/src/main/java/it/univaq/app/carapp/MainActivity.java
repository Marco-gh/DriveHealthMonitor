package it.univaq.app.carapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import it.univaq.app.carapp.Model.Session;
import it.univaq.app.carapp.Utility.FileUtility;
import it.univaq.app.carapp.databinding.ActivityMainBinding;

public class MainActivity extends Activity implements SensorEventListener {

    private ActivityMainBinding binding;
    private static final int TYPE_lifeq_lel_spo2 = 65561;
    private boolean isRunning = false;
    private static final int ACCELERATION_SENSOR = Sensor.TYPE_ACCELEROMETER;

    private boolean offBody = false;

    private Sensor SensorBPM;
    private Sensor SensorAccelerometer;
    private Sensor SensorOffBody;
    private SensorManager mSensorManager;
    private int accuracyHeartRate;
    private Session current_session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setAllSensorTextViews("-");

        binding.imageViewCar.setVisibility(View.INVISIBLE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        binding.buttonStop.setEnabled(false);
        binding.textViewOutOfBody.setVisibility(View.INVISIBLE);

        binding.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.imageViewCar.setVisibility(View.VISIBLE);

                isRunning = true;
                binding.buttonStart.setEnabled(false);
                binding.buttonStop.setEnabled(true);
                if(mSensorManager!=null){
                    requestPermissions(new String[]{Manifest.permission.BODY_SENSORS},1);
                }
                setAllSensorTextViews("0");
                current_session = new Session();
            }
        });
        binding.buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isRunning = false;
                binding.buttonStart.setEnabled(true);
                binding.buttonStop.setEnabled(false);
                binding.imageViewCar.setVisibility(View.INVISIBLE);
                mSensorManager.unregisterListener(MainActivity.this);
                setAllSensorTextViews("-");

                //if there isn't connection save data inside internal storage
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            setupManageSession();
                            if(manageSessionNodeId==null){
                                Gson gson = new Gson();
                                String json = gson.toJson(current_session);
                                FileUtility.writeSessionFile(getApplicationContext(),current_session.getStringDate(),json.toString().getBytes());
                            }
                            else{
                                //startservice()->per non passare attraverso l'activity aperta sul dispositivo mobile
                                //per ogni file in memoria
                                Gson gson = new Gson();
                                String json = gson.toJson(current_session);
                                sendData(json);
                                //cancellare quel file in memoria
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensorOutBody();
        registerSensorInBody();
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            registerSensorInBody();
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
        }
*/
        if(isRunning && !offBody){
            if(event.sensor.getType()==Sensor.TYPE_HEART_RATE && accuracyHeartRate!=SensorManager.SENSOR_STATUS_UNRELIABLE &&
                    accuracyHeartRate!=SensorManager.SENSOR_STATUS_NO_CONTACT){
                binding.textViewBPM.setText(String.valueOf(event.values[0]));

                current_session.addBpm((int) event.values[0]);
            }
            else if(event.sensor.getType()==ACCELERATION_SENSOR){
                String x = String.valueOf(event.values[0]);
                if(x.length()>3){
                    x = x.substring(0,x.lastIndexOf(".")+3);
                }
                String y = String.valueOf(event.values[1]);
                if(y.length()>3){
                    y = y.substring(0,y.lastIndexOf(".")+3);
                }
                String z = String.valueOf(event.values[2]);
                if(z.length()>3){
                    z = z.substring(0,z.lastIndexOf(".")+3);
                }

                binding.textViewLinearAccelerationX.setText(x);
                binding.textViewLinearAccelerationY.setText(y);
                binding.textViewLinearAccelerationZ.setText(z);

                current_session.addAcceleration(x, y, z);
                //Log.d("accell",String.format("X:%s Y:%s Z:%s", event.values[0], event.values[1], event.values[2]));
            }
            else if(mSensorManager.getDefaultSensor(TYPE_lifeq_lel_spo2)!=null && event.sensor.getType() == TYPE_lifeq_lel_spo2){
                Log.d("MY_APP","Blood O2: "+ Arrays.toString(event.values));

                Float percentual_o2 = null;
                if (event.values[0] == 0.0 && event.values[1] != 0.0) {
                    percentual_o2 = event.values[1];
                }
                else if (event.values[1] == 0.0 && event.values[0] != 0.0) {
                    percentual_o2 = event.values[1];
                }
                else{
                    percentual_o2 = Math.max(event.values[0],event.values[1]);
                }

                current_session.addO2inBlood(percentual_o2);
            }
        }
        else if(isRunning && offBody){
            binding.textViewOutOfBody.setVisibility(View.VISIBLE);
            setAllSensorTextViews("-");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if(sensor.getType()==Sensor.TYPE_HEART_RATE){
            this.accuracyHeartRate = accuracy;
            Log.d("MY_APP","Accuracy BPM: "+accuracy);
        }
    }

    public void setAllSensorTextViews(String s){
        binding.textViewBPM.setText(s);
        binding.textViewLinearAccelerationX.setText(s);
        binding.textViewLinearAccelerationY.setText(s);
        binding.textViewLinearAccelerationZ.setText(s);
    }

    public void registerSensorOutBody(){
        SensorAccelerometer = mSensorManager.getDefaultSensor(ACCELERATION_SENSOR);
        SensorOffBody = mSensorManager.getDefaultSensor(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT);
        mSensorManager.registerListener(this, SensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, SensorOffBody, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void registerSensorInBody(){
        SensorBPM = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mSensorManager.registerListener(this, SensorBPM, SensorManager.SENSOR_DELAY_NORMAL);
        if(mSensorManager.getDefaultSensor(TYPE_lifeq_lel_spo2)!=null){
            Sensor sensorBloodOxygen = mSensorManager.getDefaultSensor(TYPE_lifeq_lel_spo2);
            mSensorManager.registerListener(this, sensorBloodOxygen, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    //////////////////////////////TROVARE I NODI VICINI/////////////////////////////////////////////
    private static final String MANAGE_SESSION_CAPABILITY_NAME = "manage_session";
    private String manageSessionNodeId = null;

    private void setupManageSession() throws ExecutionException, InterruptedException {
        CapabilityInfo capabilityInfo = Tasks.await(
                Wearable.getCapabilityClient(getApplicationContext()).getCapability(
                        MANAGE_SESSION_CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE));
        updateTranscriptionCapability(capabilityInfo);
    }

    private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();
        manageSessionNodeId = pickBestNodeId(connectedNodes);
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
    String datapath = "/data_path";

    private void sendData(String message) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(datapath);
        dataMap.getDataMap().putString("message", message);
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();

        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        dataItemTask
                .addOnSuccessListener(new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        System.out.println("LA SEDE FORSE: Sending message was successful: " + dataItem+", message: "+message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("LA SEDE FORSE: Sending message failed: " + e);
                    }
                })
        ;
    }
}
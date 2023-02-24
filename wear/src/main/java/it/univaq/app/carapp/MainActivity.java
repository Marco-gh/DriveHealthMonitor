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

import androidx.annotation.NonNull;

import java.util.Arrays;

import it.univaq.app.carapp.databinding.ActivityMainBinding;

public class MainActivity extends Activity implements SensorEventListener {

    private ActivityMainBinding binding;
    private static final int TYPE_lifeq_lel_spo2 = 65561;
    private boolean isRunning = false;
    private static final int ACCELERATION_SENSOR = Sensor.TYPE_ACCELEROMETER;

    //Da fare unregister dei sensori in caso ==true? Serve ancora isRunning?
    private boolean offBody;

    //Aggiungere altri sensori LifeQ?
    private Sensor SensorBPM;
    private Sensor SensorAccelerometer;
    private Sensor SensorOffBody;
    private SensorManager mSensorManager;
    private int accuracyHeartRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setAllSensorTextViews("-");

        binding.imageViewCar.setVisibility(View.INVISIBLE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        binding.buttonStop.setEnabled(false);

        binding.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRunning = true;
                binding.imageViewCar.setVisibility(View.VISIBLE);
                binding.buttonStart.setEnabled(false);
                binding.buttonStop.setEnabled(true);
                if(mSensorManager!=null){
                    requestPermissions(new String[]{Manifest.permission.BODY_SENSORS},1);
                }
                setAllSensorTextViews("0");
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
                setAllSensorTextViews("0");
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
        if(isRunning && !offBody){
            if(event.sensor.getType()==Sensor.TYPE_HEART_RATE && accuracyHeartRate!=SensorManager.SENSOR_STATUS_UNRELIABLE &&
                    accuracyHeartRate!=SensorManager.SENSOR_STATUS_NO_CONTACT){
                binding.textViewBPM.setText(String.valueOf(event.values[0]));
            }
            else if(event.sensor.getType()==ACCELERATION_SENSOR){
                binding.textViewLinearAccelerationX.setText(String.valueOf(event.values[0]));
                binding.textViewLinearAccelerationY.setText(String.valueOf(event.values[1]));
                binding.textViewLinearAccelerationZ.setText(String.valueOf(event.values[2]));

                //Log.d("accell",String.format("X:%s Y:%s Z:%s", event.values[0], event.values[1], event.values[2]));
            }
            else if (event.sensor.getType()==Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT){
                this.offBody = event.values[0] == 0.0;
            }
            else if(event.sensor.getType() == TYPE_lifeq_lel_spo2){
                Log.d("MY_APP","Blood O2: "+ Arrays.toString(event.values));

                String percentual_o2 = String.valueOf(Math.max(event.values[0],event.values[1]));
                if(!percentual_o2.equals("")){
                    binding.textViewO2InBlood.setText(percentual_o2);
                }
            }
            else if(offBody){
                setAllSensorTextViews("-");
            }
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
}
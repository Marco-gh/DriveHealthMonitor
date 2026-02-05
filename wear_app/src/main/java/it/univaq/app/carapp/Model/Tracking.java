package it.univaq.app.carapp.Model;

import java.time.LocalDateTime;
import java.util.Arrays;

import android.content.Context;
import android.provider.Settings.Secure;

public class Tracking {
    private String deviceID;
    private String date;
    private Float bpm;
    private Float[] accelerometer;
    private Float O2inBlood;

    public Tracking(Context context) {
        this.deviceID = Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
        this.date = LocalDateTime.now().toString();
        this.bpm = null;
        this.accelerometer = null;
        this.O2inBlood = null;
    }

    @Override
    public String toString() {
        return "Tracking{" +
                "deviceID='" + deviceID + '\'' +
                ", date=" + date +
                ", bpm=" + bpm +
                ", accelerometer=" + Arrays.toString(accelerometer) +
                ", O2inBlood=" + O2inBlood +
                '}';
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Float getBpm() {
        return bpm;
    }

    public void setBpm(Float bpm) {
        this.bpm = bpm;
    }

    public Float[] getAccelerometer() {
        return accelerometer;
    }

    public void setAccelerometer(Float[] accelerometer) {
        this.accelerometer = accelerometer;
    }

    public Float getO2inBlood() {
        return O2inBlood;
    }

    public void setO2inBlood(Float o2inBlood) {
        O2inBlood = o2inBlood;
    }
}

package it.univaq.app.carapp.Model;

import android.content.Context;
import android.provider.Settings.Secure;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import java.util.Date;

@Entity
public class Tracking {
    @Ignore
    final static DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private String deviceID;
    @NonNull
    @PrimaryKey
    private String date;
    private Float bpm;
    @TypeConverters
    private Float[] accelerometer;
    private Float O2inBlood;

    public Tracking(){}

    public Tracking(Context context) {
        this.deviceID = Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
        this.date = null;
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

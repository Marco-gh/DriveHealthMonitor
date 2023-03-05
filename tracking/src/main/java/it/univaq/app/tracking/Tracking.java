package it.univaq.app.tracking;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Tracking {
    public final static DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private String deviceID;
    private LocalDateTime date;
    private Time time;
    private Float bpm;
    private Float[] accelerometer;
    private Float O2inBlood;

    public Tracking(/*Context context*/) {
        //this.deviceID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        this.date = LocalDateTime.now();
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

    public boolean hasValues(boolean thereIsO2Sensor){
        if(deviceID!=null && bpm!=null && accelerometer!=null){
            if(thereIsO2Sensor && O2inBlood!=null){
                return true;
            }
            return true;
        }
        else{
            return false;
        }
    }

    public String getStringDate() {
        return date.format(ISO_FORMATTER);
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
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

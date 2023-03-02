package it.univaq.app.carapp.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Entity (tableName = "sessions")
public class Session {

    @PrimaryKey
    @NonNull private LocalDateTime date;
    private ArrayList<Integer> bpm;
    private ArrayList<ArrayList<String>> accelerometer;
    private ArrayList<Float> O2inBlood = new ArrayList<>();

    public String getStringDate() {
        return date.toString();
    }

    public Session() {
        this.date = LocalDateTime.now();
        this.bpm = new ArrayList<>();
        this.accelerometer = new ArrayList<ArrayList<String>>();
        this.O2inBlood = new ArrayList<>();
    }

    public void addBpm(int x){
        this.bpm.add(x);
    }

    public void addAcceleration(String x, String y, String z){
        ArrayList<String> acceleration = new ArrayList<>();
        acceleration.add(x);
        acceleration.add(y);
        acceleration.add(z);
        this.accelerometer.add(acceleration);
    }

    public void addO2inBlood(Float x){
        this.O2inBlood.add(x);
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public ArrayList<Integer> getBpm() {
        return bpm;
    }

    public void setBpm(ArrayList<Integer> bpm) {
        this.bpm = bpm;
    }

    public ArrayList<ArrayList<String>> getAccelerometer() {
        return accelerometer;
    }

    public void setAccelerometer(ArrayList<ArrayList<String>> accelerometer) {
        this.accelerometer = accelerometer;
    }

    public ArrayList<Float> getO2inBlood() {
        return O2inBlood;
    }

    public void setO2inBlood(ArrayList<Float> o2inBlood) {
        O2inBlood = o2inBlood;
    }
}

package it.univaq.app.carapp.Model;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Session {
    //Togliere date, utile visto che il nome del file porta la data?
    private LocalDateTime date;
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
}

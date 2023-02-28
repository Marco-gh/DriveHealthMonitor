package it.univaq.app.carapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(getApplicationContext(), DataListenerService.class);
    }

    @Override
    public void onResume() {
        super.onResume();

        startService(intent);
    }

    @Override
    public void onPause() {
        super.onPause();

        stopService(intent);
    }

}
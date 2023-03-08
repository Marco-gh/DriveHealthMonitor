package it.univaq.app.carapp.Utility.RoomDB;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import it.univaq.app.carapp.View.MainFragmentActivity;

public class Converters {

    @TypeConverter
    public static String toStringFromFloatArray(Float[] array) {
        Gson gson = new Gson();
        String string = gson.toJson(array);
        return string;
    }

    @TypeConverter
    public static Float[] toFloatArrayFromString(String string) {
        Gson gson = new Gson();
        Float[] array = gson.fromJson(string, Float[].class);
        return array;
    }
}

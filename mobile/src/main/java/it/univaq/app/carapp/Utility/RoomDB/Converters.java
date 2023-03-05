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
    final static DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @TypeConverter
    public static LocalDateTime toLocalDateTime(String string) {
        if(string != "" && string != null){
            LocalDateTime localDateTime = LocalDateTime.parse(string, ISO_FORMATTER);
            return localDateTime;
        }
        else{
            return LocalDateTime.now();
        }
    }

    @TypeConverter
    public static String fromLocalDateTime(LocalDateTime date) {
        if(date != null){
            String formattedString = date.format(ISO_FORMATTER);
            System.out.println("Date.format "+formattedString);
            return formattedString;
        }
        else{
            return "null";
        }
    }

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

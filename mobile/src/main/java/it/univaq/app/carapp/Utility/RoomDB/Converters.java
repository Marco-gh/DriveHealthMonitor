package it.univaq.app.carapp.Utility.RoomDB;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class Converters {
    final static DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @TypeConverter
    public static LocalDateTime toLocalDateTime(String string) {
        LocalDateTime localDateTime = LocalDateTime.parse(string, ISO_FORMATTER);
        return localDateTime;
    }

    @TypeConverter
    public static String fromLocalDateTime(LocalDateTime date) {
        String formattedString = date.format(ISO_FORMATTER);
        return formattedString;
    }

    @TypeConverter
    public static ArrayList<Integer> fromStringToLisInt(String value) {
        Type listType = new TypeToken<ArrayList<Integer>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayListInt(ArrayList<Integer> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }

    @TypeConverter
    public static ArrayList<Float> fromStringToLisFlo(String value) {
        Type listType = new TypeToken<ArrayList<Float>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayListFlo(ArrayList<Float> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }

    @TypeConverter
    public static ArrayList<ArrayList<String>> fromStringToListStr(String value) {
        Type listType = new TypeToken<ArrayList<ArrayList<String>>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayListStr(ArrayList<ArrayList<String>> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}

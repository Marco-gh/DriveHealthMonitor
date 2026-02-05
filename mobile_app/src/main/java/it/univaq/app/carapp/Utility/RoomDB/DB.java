package it.univaq.app.carapp.Utility.RoomDB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import it.univaq.app.carapp.Model.Tracking;

@Database(entities = { Tracking.class }, version = 1)
@TypeConverters({Converters.class})
public abstract class DB extends RoomDatabase {

    public abstract TrackingDAO getSessionDAO();

    private static volatile DB instance = null;

    public static synchronized DB getInstance(Context context) {

        if(instance == null) {
            synchronized (DB.class) {
                if(instance == null) {
                    instance = Room.databaseBuilder(
                                    context,
                                    DB.class,
                                    "myRoomDatabase.db")
                            .build();
                }
            }
        }
        return instance;
    }
}

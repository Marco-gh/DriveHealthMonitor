package it.univaq.app.carapp.Utility.RoomDB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.univaq.app.carapp.Model.Tracking;

@Dao
public abstract class TrackingDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(Tracking... trackings);

    @Query("SELECT * FROM Tracking")
    public abstract List<Tracking> findAll();

    @Delete
    public abstract void remove(Tracking tracking);
}

package it.univaq.app.carapp.Utility.RoomDB;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.univaq.app.carapp.Model.Tracking;

@Dao
public abstract class TrackingDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(Tracking... session);

    @Update
    public abstract void update(Tracking session);

    @Query("SELECT * FROM Tracking ORDER BY deviceID DESC")
    public abstract List<Tracking> findAll();
}

package it.univaq.app.carapp.Utility.RoomDB;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverter;
import androidx.room.Update;

import java.util.List;

import it.univaq.app.carapp.Model.Session;

@Dao
public abstract class SessionDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(Session... session);

    @Update
    public abstract void update(Session session);

    @Query("SELECT * FROM sessions ORDER BY date DESC")
    public abstract List<Session> findAll();
}

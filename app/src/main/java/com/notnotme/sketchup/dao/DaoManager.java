package com.notnotme.sketchup.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface DaoManager {

    @Query("SELECT * FROM Sketch")
    List<Sketch> getAllSketch();

    @Insert
    void saveSketch(Sketch sketch);

    @Delete
    void deleteSketch(Sketch sketch);

    @Delete
    void deleteSketches(List<Sketch> sketch);

}

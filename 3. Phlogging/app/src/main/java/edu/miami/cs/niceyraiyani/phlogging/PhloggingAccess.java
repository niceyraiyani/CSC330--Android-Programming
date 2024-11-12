package edu.miami.cs.niceyraiyani.phlogging;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PhloggingAccess {
    // Retrieves all PhloggingEntity records from the database and orders them by their ID in ascending order.
    @Query("SELECT * FROM Phlogs ORDER BY id ASC")
    List<PhloggingEntity> fetchAllPhlogs();

    @Query("SELECT * FROM Phlogs WHERE phlogsDB_timestamp=:this_timestamp")
    PhloggingEntity getPhlogByTimestamp(String this_timestamp);

    // new entry
    @Insert
    void addPhlog(PhloggingEntity newPhlog);
    // remove entry

    @Delete
    void deletePhlog(PhloggingEntity oldPlog);

    //update entry

    @Update
    void updatePhlog(PhloggingEntity newPhlog);
}

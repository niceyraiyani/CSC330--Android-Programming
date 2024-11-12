package edu.miami.cs.niceyraiyani.phlogging;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {PhloggingEntity.class},version = 1, exportSchema = false)
public abstract class PhloggingDB extends RoomDatabase {
    // Room database for the phlogging database
    public abstract PhloggingAccess daoAccess();
}
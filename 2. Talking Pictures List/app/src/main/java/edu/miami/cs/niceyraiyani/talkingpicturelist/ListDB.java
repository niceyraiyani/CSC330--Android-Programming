package edu.miami.cs.niceyraiyani.talkingpicturelist;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// From the website, the basis for the Database
@Database(entities = {ListEntity.class},version = 1,exportSchema = false)
public abstract class ListDB extends RoomDatabase {
    public abstract ListAccess daoAccess();
}
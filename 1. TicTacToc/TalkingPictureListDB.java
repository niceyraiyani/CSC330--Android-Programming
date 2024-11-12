package edu.miami.cs.geoff.talkingpicturelist;

import androidx.room.Database;
import androidx.room.RoomDatabase;

//=================================================================================================
@Database(entities = {TalkingPictureListEntity.class},version = 1,exportSchema = false)
public abstract class TalkingPictureListDB extends RoomDatabase {
//-------------------------------------------------------------------------------------------------
    public abstract TalkingPictureListAccess daoAccess();
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================
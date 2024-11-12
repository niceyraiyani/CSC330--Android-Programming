package edu.miami.cs.geoff.talkingpicturelist;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

//=================================================================================================
@Dao
public interface TalkingPictureListAccess {
//-------------------------------------------------------------------------------------------------
    @Query("SELECT * FROM TalkingPictures")
    List<TalkingPictureListEntity> fetchAllTalkingPictures();

    @Query("SELECT * FROM TalkingPictures where id LIKE :id")
    TalkingPictureListEntity getTalkingPictureById(int id);

    @Query("SELECT * FROM TalkingPictures where image_id LIKE :id")
    TalkingPictureListEntity getTalkingPictureByImageId(long id);

    @Insert
    void addTalkingPicture(TalkingPictureListEntity newTalkingPicture);

    @Delete
    void deleteTalkingPicture(TalkingPictureListEntity oldTalkingPicture);

    @Update
    void updateTalkingPicture(TalkingPictureListEntity newTalkingPicture);
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================
package edu.miami.cs.niceyraiyani.talkingpicturelist;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
// Interface for accessing the list which is required for the room database
@Dao
public interface ListAccess {
    @Query("SELECT * FROM TalkingPictures")
    List<ListEntity> fetchAllTalkingPictures();

    @Query("SELECT * FROM TalkingPictures where id LIKE :id")
    ListEntity getTalkingPictureById(int id);

    @Query("SELECT * FROM TalkingPictures where image_id LIKE :id")
    ListEntity getTalkingPictureByImageId(long id);

    @Insert
    void addTalkingPicture(ListEntity newTalkingPicture);

    @Delete
    void deleteTalkingPicture(ListEntity oldTalkingPicture);

    @Update
    void updateTalkingPicture(ListEntity newTalkingPicture);
}
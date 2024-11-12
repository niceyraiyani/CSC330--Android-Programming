package edu.miami.cs.geoff.talkingpicturelist;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//=================================================================================================
@Entity(tableName = "TalkingPictures")
public class TalkingPictureListEntity {
//-------------------------------------------------------------------------------------------------
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "image_id")
    private long imageId;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "recording")
    private byte[] recording;
//-------------------------------------------------------------------------------------------------
    public TalkingPictureListEntity() {
    }
//-------------------------------------------------------------------------------------------------
    public int getId() {
        return(id);
    }
//-------------------------------------------------------------------------------------------------
    public long getImageId() {
        return(imageId);
    }
//-------------------------------------------------------------------------------------------------
    public String getDescription() {
        return(description);
    }
//-------------------------------------------------------------------------------------------------
    public byte[] getRecording() {
        return(recording);
    }
//-------------------------------------------------------------------------------------------------
    public void setId(int newId) {
        id = newId;
    }
//-------------------------------------------------------------------------------------------------
    public void setImageId(long newId) {
        imageId = newId;
    }
//-------------------------------------------------------------------------------------------------
    public void setDescription(String newDescription) {
        description = newDescription;
    }
//-------------------------------------------------------------------------------------------------
    public void setRecording(byte[] newRecording) {
        recording = newRecording;
    }
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================
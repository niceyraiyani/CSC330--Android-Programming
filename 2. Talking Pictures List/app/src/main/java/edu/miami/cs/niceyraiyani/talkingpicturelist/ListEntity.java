package edu.miami.cs.niceyraiyani.talkingpicturelist;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
// Storing the talking picture data in a Room database
@Entity(tableName = "TalkingPictures")
public class ListEntity {
    // talking picture id
    @PrimaryKey(autoGenerate = true)
    private int id;
    // image id
    @ColumnInfo(name = "image_id")
    private long imageId;
    // description associated with each picture
    @ColumnInfo(name = "description")
    private String description;
    //recording if any
    @ColumnInfo(name = "recording")
    private byte[] recording;
    public ListEntity() {
    }
    public int getId() {
        return(id);
    }
    public long getImageId() {
        return(imageId);
    }
    public String getDescription() {
        return(description);
    }
    public byte[] getRecording() {
        return(recording);
    }
    public void setId(int newId) {
        id = newId;
    }
    public void setImageId(long newId) {
        imageId = newId;
    }
    public void setDescription(String newDescription) {
        description = newDescription;
    }
    public void setRecording(byte[] newRecording) {
        recording = newRecording;
    }
}
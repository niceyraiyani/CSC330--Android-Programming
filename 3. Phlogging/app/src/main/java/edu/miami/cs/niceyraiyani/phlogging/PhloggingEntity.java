package edu.miami.cs.niceyraiyani.phlogging;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Phlogs",indices = {@Index(value = {"phlogsDB_timestamp"},unique = true)})
public class PhloggingEntity {

    // stating all the variables for each descriptions in the phlogs
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "phlogsDB_title")
    private String title;

    @ColumnInfo(name = "phlogsDB_text")
    private String text;

    @ColumnInfo(name = "phlogsDB_uri")
    private String uri;

    @ColumnInfo(name = "phlogsDB_timestamp")
    private String timestamp;

    @ColumnInfo(name = "phlogsDB_latitude")
    private float latitude;

    @ColumnInfo(name = "phlogsDB_longitude")
    private float longitude;

    @ColumnInfo(name = "phlogsDB_text_location")
    private String textLocation;

    // All the getters and setter methods for adding to the phlogging databse
    public PhloggingEntity() {
    }
    public void setId(int newID) {
        id = newID;
    }

    public int getId() {
        return(id);
    }

    public String getTitle() {
        return(title);
    }

    public void setTitle(String newname) {
        title = newname;
    }


    public String getUri() {
        return(uri);
    }

    public void setUri(String newURI) {
        uri = newURI;
    }
    public String getText() {
        return(text);
    }

    public void setText(String newText) {
        text = newText;
    }

    public String getTimestamp() {
        return(timestamp);
    }

    public void setTimestamp(String newTimestamp) {
        timestamp = newTimestamp;
    }
    public String getTextLocation() {
        return(textLocation);
    }

    public void setTextLocation(String newTextLocation) {
        textLocation = newTextLocation;
    }

    public float getLatitude() {
        return(latitude);
    }

    public void setLatitude(float newlat) {
        latitude = newlat;
    }

    public float getLongitude() {
        return(longitude);
    }

    public void setLongitude(float newLong) {
        longitude = newLong;
    }



}
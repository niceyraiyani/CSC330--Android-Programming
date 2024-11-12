package edu.miami.cs.niceyraiyani.phlogging;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class DisplayEdit extends AppCompatActivity {
    // Variables to store photo URI, latitude, longitude, and timestamp
    private Uri savedUri = null;
    private float lat, longi;
    private String time;
    private PhloggingEntity entryWithTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_edit); // Setting up the layout

        // Retrieve timestamp from intent and display
        time = getIntent().getStringExtra("edu.miami.cs.niceyraiyani.phlogging.timestamp");
        ((TextView) findViewById(R.id.edit_timestamp)).setText(time);

        // Fetching entry using the timestamp
        entryWithTime = MainActivity.phloggingDB.daoAccess().getPhlogByTimestamp(time);
        populateUIWithData();

        handleLocationData();
    }

    private void populateUIWithData() {
        if (entryWithTime != null) {
            // Populate UI with existing entry data
            ((TextView) findViewById(R.id.edit_title)).setText(entryWithTime.getTitle());
            ((TextView) findViewById(R.id.edit_text)).setText(entryWithTime.getText());
            savedUri = Uri.parse(entryWithTime.getUri());
            ((ImageView) findViewById(R.id.edit_image)).setImageURI(savedUri);
            lat = entryWithTime.getLatitude();
            longi = entryWithTime.getLongitude();
            ((TextView) findViewById(R.id.edit_latlon_location)).setText(
                    String.format("Lat: %5.1f Lon: %5.1f", lat, longi));
            ((TextView) findViewById(R.id.edit_text_location)).setText(
                    entryWithTime.getTextLocation());
        } else {
            setHintsForNewEntry();
        }
    }

    private void setHintsForNewEntry() {
        // Setting hints for new entry fields
        ((TextView) findViewById(R.id.edit_title)).setHint(getResources().getString(R.string.no_title));
        ((TextView) findViewById(R.id.edit_text)).setHint(getResources().getString(R.string.no_text));
    }

    private void handleLocationData() {
        if (MainActivity.currentLocation != null) {
            lat = (float) MainActivity.currentLocation.getLatitude();
            longi = (float) MainActivity.currentLocation.getLongitude();
            ((TextView) findViewById(R.id.edit_latlon_location)).setText(
                    String.format("Lat: %5.2f Lon: %5.2f", lat, longi));
            new SensorLocatorDecode(getApplicationContext(), this).execute(
                    MainActivity.currentLocation);
        } else {
            displayNoLocation();
        }
    }

    private void displayNoLocation() {
        // Display 'No Location' if no current location data is available
        ((TextView) findViewById(R.id.edit_latlon_location)).setText(
                getResources().getString(R.string.no_location));
        ((TextView) findViewById(R.id.edit_text_location)).setText(
                getResources().getString(R.string.no_location));
    }

    public void editClickHandler(View view) {
        Intent returnIntent;
        switch (view.getId()) {
            case R.id.edit_camera:
                handleCameraClick();
                break;
            case R.id.edit_save:
                handleSaveClick();
                break;
            case R.id.edit_delete:
                handleDeleteClick();
                break;
            default:
                break;
        }
    }

    private void handleCameraClick() {
        // Camera button click logic
        savedUri = FileProvider.getUriForFile(
                this, getApplicationContext().getPackageName() + ".provider",
                new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES) + "/" +
                        getResources().getString(R.string.camera).replaceAll("XXX", "" + System.currentTimeMillis())));
        openCamera.launch(savedUri);
    }

    private void handleSaveClick() {
        // Save button click logic
        PhloggingEntity newEntry = createNewPhloggingEntity();
        updateOrAddEntryInDB(newEntry);
        closeActivity();
    }

    private void handleDeleteClick() {
        if (entryWithTime != null) {
            MainActivity.phloggingDB.daoAccess().deletePhlog(entryWithTime);
        } else {
            Toast.makeText(this, "No Phlog there to delete", Toast.LENGTH_SHORT).show();
        }
        closeActivity();
    }

    private PhloggingEntity createNewPhloggingEntity() {
        PhloggingEntity newEntry = new PhloggingEntity();
        newEntry.setTitle(((TextView) findViewById(R.id.edit_title)).getText().toString());
        newEntry.setText(((TextView) findViewById(R.id.edit_text)).getText().toString());
        newEntry.setUri(savedUri != null ? savedUri.toString() : "");
        newEntry.setTimestamp(((TextView) findViewById(R.id.edit_timestamp)).getText().toString());
        newEntry.setLatitude(lat);
        newEntry.setLongitude(longi);
        newEntry.setTextLocation(((TextView) findViewById(R.id.edit_text_location)).getText().toString());
        return newEntry;
    }

    private void updateOrAddEntryInDB(PhloggingEntity newEntry) {
        if (entryWithTime != null) {
            newEntry.setId(entryWithTime.getId());
            MainActivity.phloggingDB.daoAccess().updatePhlog(newEntry);
        } else {
            MainActivity.phloggingDB.daoAccess().addPhlog(newEntry);
        }
    }

    private void closeActivity() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private ActivityResultLauncher<Uri> openCamera = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), result -> {
                if (result) {
                    ImageView editImage = findViewById(R.id.edit_image);
                    editImage.setImageURI(null);
                    editImage.setImageURI(savedUri);
                }
            });
}

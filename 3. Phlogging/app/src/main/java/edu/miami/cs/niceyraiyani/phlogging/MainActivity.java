package edu.miami.cs.niceyraiyani.phlogging;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        SimpleAdapter.ViewBinder {

    private static final String DATABASE_NAME = "PhloggingRoom.db";
    public static PhloggingDB phloggingDB;

    private FusedLocationProviderClient fusedLocationClient;
    public static Location currentLocation = null;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
    }

    private void requestPermissions() {
        // Requesting necessary permissions
        getPermissions.launch(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION});
    }

    private void goOnCreating(boolean havePermission) {
        if (havePermission) {
            setupViewAndDB();
            setupLocationUpdates();
        } else {
            // Handle case where permissions are not granted
            Toast.makeText(this,"Need permission",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupViewAndDB() {
        setContentView(R.layout.activity_main);
        phloggingDB = Room.databaseBuilder(getApplicationContext(), PhloggingDB.class, DATABASE_NAME)
                .allowMainThreadQueries().build();
        fillList();
    }

    private void setupLocationUpdates() {
        // Setting up location updates
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(getResources().getInteger(R.integer.time_between_location_updates_ms));
        locationRequest.setFastestInterval(getResources().getInteger(R.integer.time_between_location_updates_ms));
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, myLocationCallback, Looper.myLooper());
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission denied for location", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fillList() {
        // Populating the list view with data from the database
        ListView theList = findViewById(R.id.phlog_list);
        List<PhloggingEntity> allPhlogEntities = phloggingDB.daoAccess().fetchAllPhlogs();
        ArrayList<HashMap<String, Object>> arrayListOfPhlogHashes = new ArrayList<>();

        for (PhloggingEntity onePhlog : allPhlogEntities) {
            HashMap<String, Object> onePhlogHash = new HashMap<>();
            onePhlogHash.put("list_thumbnail", onePhlog.getUri());
            onePhlogHash.put("list_title", onePhlog.getTitle());
            onePhlogHash.put("list_timestamp", onePhlog.getTimestamp());
            arrayListOfPhlogHashes.add(onePhlogHash);
        }

        SimpleAdapter listAdapter = new SimpleAdapter(this, arrayListOfPhlogHashes, R.layout.phlog_list_entry,
                new String[]{"list_thumbnail", "list_title", "list_timestamp"},
                new int[]{R.id.list_thumbnail, R.id.list_title, R.id.list_timestamp});
        listAdapter.setViewBinder(this);
        theList.setAdapter(listAdapter);
        theList.setOnItemClickListener(this);
    }

    @Override
    public boolean setViewValue(View view, Object data, String asText) {
        // Custom binder to handle list item views
        switch (view.getId()) {
            case R.id.list_thumbnail:
                if (!asText.isEmpty()) {
                    ((ImageView) view).setImageURI(Uri.parse((String) data));
                }
                break;
            case R.id.list_title:
                ((TextView) view).setText(data != null && !asText.isEmpty() ? asText : getResources().getString(R.string.no_title));
                break;
            case R.id.list_timestamp:
                ((TextView) view).setText((String) data);
                break;
        }
        return true;
    }

    public void myClickHandler(View view) {
        // Handling click events
        if (view.getId() == R.id.make_phlog) {
            Intent displayEditIntent = new Intent(this, DisplayEdit.class);
            displayEditIntent.putExtra("edu.miami.cs.niceyraiyani.phlogging.timestamp", timeToString(System.currentTimeMillis()));
            startEditDisplay.launch(displayEditIntent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
        // Handling item clicks in the list view
        Intent displayEditIntent = new Intent(this, DisplayEdit.class);
        displayEditIntent.putExtra("edu.miami.cs.niceyraiyani.phlogging.timestamp", ((TextView) (view.findViewById(R.id.list_timestamp))).getText());
        startEditDisplay.launch(displayEditIntent);
    }

    private ActivityResultLauncher<Intent> startEditDisplay = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    fillList();
                }
            });

    private LocationCallback myLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            currentLocation = locationResult.getLastLocation();
        }
    };

    private static String timeToString(long time) {
        // Converting time in milliseconds to formatted string
        SimpleDateFormat formattedDate = new SimpleDateFormat("HH:mm:ss EEEE, MMMM dd, yyyy z");
        GregorianCalendar lastRunTime = new GregorianCalendar();
        lastRunTime.setTimeInMillis(time);
        return formattedDate.format(lastRunTime.getTime());
    }

    @Override
    public void onDestroy() {
        // Cleanup on destroy
        super.onDestroy();
        phloggingDB.close();
        fusedLocationClient.removeLocationUpdates(myLocationCallback);
    }

    private ActivityResultLauncher<String[]> getPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), results -> {
                for (Boolean granted : results.values()) {
                    if (!granted) {
                        goOnCreating(false);
                        return;
                    }
                }
                goOnCreating(true);
            });
}
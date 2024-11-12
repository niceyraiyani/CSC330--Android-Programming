package edu.miami.cs.niceyraiyani.phlogging;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.TextView;

import java.util.List;
public class SensorLocatorDecode extends AsyncTask<Location, Void, String> {
    private Context theContext;
    private Activity theActivity;

    // Constructor to initialize context and activity
    public SensorLocatorDecode(Context context, Activity activity) {
        theContext = context;
        theActivity = activity;
    }

    // The background process to decode the location
    @Override
    protected String doInBackground(Location... locations) {
        return locations.length > 0 ? androidGeodecode(locations[0]) : "ERROR: No Location Provided";
    }

    // Executed after the completion of doInBackground, updates the UI
    @Override
    protected void onPostExecute(String result) {
        ((TextView) theActivity.findViewById(R.id.edit_text_location)).setText(result);
    }

    // Converts a Location object to a readable address using Geocoder
    private String androidGeodecode(Location location) {
        if (Geocoder.isPresent()) {
            Geocoder androidGeocoder = new Geocoder(theContext);
            try {
                List<Address> addresses = androidGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses.isEmpty()) {
                    return "ERROR: Unknown location";
                } else {
                    return formatAddress(addresses.get(0));
                }
            } catch (Exception e) {
                return "ERROR: " + e.getMessage();
            }
        } else {
            return "ERROR: No Geocoder available";
        }
    }

    // Helper method to format the address from the Address object
    private String formatAddress(Address address) {
        StringBuilder locationName = new StringBuilder();
        int index = 0;
        String addressLine;
        while ((addressLine = address.getAddressLine(index)) != null) {
            locationName.append(addressLine).append(", ");
            index++;
        }
        return locationName.toString();
    }
}
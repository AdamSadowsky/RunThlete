package com.company.runthlete;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class GoogleLocationTracker {
    //Location service objects
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    private LocationUpdateListener updateListener;
    private Location lastKnownLocation = null;
    private Location firstKnownLocation = null;
    private final Context context;
    private boolean isRunning = false; // Track if the timer is running
    private static HandlerThread locationHandlerThread;
    private static Handler locationHandler;


    public GoogleLocationTracker(Context context) {
        this.context = context;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        //Requests location updates with a high accuracy
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000) // Request updates every 1000ms
                .setMinUpdateIntervalMillis(800)  // Allow updates every 800ms
                .setWaitForAccurateLocation(true)  // Ensures GPS accuracy before providing an update
                .setMaxUpdateDelayMillis(1500)  // Prevents delays longer than 1.5 second
                .build();

        //Initializes LocationCallback to handle location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (!isRunning) return;
                //Iterates through each location and adds to list
                for (Location location : locationResult.getLocations()) {
                    if(location == null) {
                        Log.d("Location", "Location is null");
                        return;
                    }

                    if (firstKnownLocation == null ) {
                        firstKnownLocation = location;//Sets first location
                    }
                        lastKnownLocation = location; //Constantly updates last known location to current location
                    // Notify the listener so the UI can update
                    if (updateListener != null) {
                        updateListener.updateUi(location);//Notify the listener so the UI can update
                    }
                }
            }
        };
        //Reinitialize handler thread if it’s null or not alive
        if (locationHandlerThread == null || !locationHandlerThread.isAlive()) {
            locationHandlerThread = new HandlerThread("LocationUpdates");
            locationHandlerThread.start();
            locationHandler = new Handler(locationHandlerThread.getLooper());
        }
    }

    public void setLocationUpdateListener(LocationUpdateListener listener){
        this.updateListener = listener;
    }


    @SuppressLint("MissingPermission")
    public void startTracking() {
        //Checks for location permissions
        if (!initLocationPermission()) {
            return;
        }
        if (firstKnownLocation == null) {
            //Retrieves the users current location
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                if (location != null) {
                    firstKnownLocation = location;
                    lastKnownLocation = location;
                    isRunning = true;
                    if (locationHandler != null) {
                        //Invokes location updates at a specific interval until run is stopped
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, locationHandler.getLooper());
                    } else {
                        Log.w("Location", "Location handler is null; cannot request location updates");
                    }
                    } else {
                        Log.d("Location", "Location null");
                        Toast.makeText(context, "Waiting for GPS... Please wait before starting.", Toast.LENGTH_SHORT).show();
                    }
                    });
        }
        else { // If we already have a location, start tracking immediately
            isRunning = true;
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, locationHandler.getLooper());
        }
    }

    public void stopTracking() {
        //If location is being tracked it stops the updates
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        if (locationHandlerThread != null) {
            locationHandler.removeCallbacksAndMessages(null);
            locationHandlerThread.quitSafely();
            locationHandlerThread = null;
            locationHandler = null;
        }

        isRunning = false;
        }

        //Checks if program has necessary location permissions
    private boolean initLocationPermission(){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return false;
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context, "GPS is turned off. Enable it before starting!", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    // Public getter methods for the UI
    public Location getFirstKnownLocation() {
        return firstKnownLocation;
    }

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }
}







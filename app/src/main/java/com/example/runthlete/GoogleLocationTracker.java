package com.example.runthlete;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
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
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class GoogleLocationTracker {
    //Location service objects
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    private LocationUpdateListener updateListener;
    private Location lastKnownLocation = null;
    private Location firstKnownLocation = null;
    private Context context;
    private boolean isRunning = false; // Track if the timer is running
    private static HandlerThread locationHandlerThread;
    private static Handler locationHandler;
    private List<LatLng> runPath = new ArrayList<>();
    private Handler logHandler = new Handler();
    private Runnable logRunnable;


    public GoogleLocationTracker(Context context) {
        this.context = context;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        //Requests location updates with a high accuracy
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000) // Request updates every 500ms
                .setMinUpdateIntervalMillis(800)  // Allow updates every 800ms (for quick responsiveness)
                .setWaitForAccurateLocation(true)  // Ensures GPS accuracy before providing an update
                .setMaxUpdateDelayMillis(1500)  // Prevents delays longer than 1 second
                .build();

        // Initialize LocationCallback to handle location updates
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
                        firstKnownLocation = location;//Set first location
                        Log.d("Debug", "First known location initiated");
                    }
                    lastKnownLocation = location; // Update last known location to current location
                        Log.d("Location", "Last known location: " + location.getLatitude() + ", " + location.getLongitude());//Continuously updates location
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
        logRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    String lastLocation = "null";

                    if (lastKnownLocation != null) {
                        lastLocation = lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude();
                    }

                    Log.d("TrackerLog", "Last Location: " + lastLocation);

                    logHandler.postDelayed(this, 2000); // Re-run in 2 seconds
                }
            }
        };
    }

    public void setLocationUpdateListener(LocationUpdateListener listener){
        this.updateListener = listener;
    }


    @SuppressLint("MissingPermission")
    public void startTracking() {
        if (!initLocationPermission()) {
            return;
        } // Reinitialize handler thread if it’s null or not alive
        if (locationHandlerThread == null || !locationHandlerThread.isAlive()) {
            locationHandlerThread = new HandlerThread("LocationUpdates");
            locationHandlerThread.start();
            locationHandler = new Handler(locationHandlerThread.getLooper());
        } if (firstKnownLocation == null) {
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                if (location != null) {
                    Log.d("Location", "Tracking initiated");
                    firstKnownLocation = location;
                    lastKnownLocation = location;
                    isRunning = true;
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, locationHandler.getLooper());
                    Log.d("Location", "Location: " + firstKnownLocation); } else { Log.d("Location", "Location null");
                    Toast.makeText(context, "Waiting for GPS... Please wait before starting.", Toast.LENGTH_SHORT).show();
                }
                    });
        }
        else { // If we already have a location, start tracking immediately
            isRunning = true;
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, locationHandler.getLooper());
        }
        Log.d("DEBUG", "startTracking() finished execution");
        logHandler.post(logRunnable);
    }

    public void stopTracking() {
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        if (locationHandlerThread != null) {
            locationHandlerThread.quitSafely();
            locationHandlerThread = null;
            locationHandler = null;
        }

        isRunning = false;
        logHandler.removeCallbacks(logRunnable);
        }

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







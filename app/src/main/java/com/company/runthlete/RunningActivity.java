package com.company.runthlete;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.maps.model.LatLng;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RunningActivity extends AppCompatActivity implements LocationUpdateListener{
    private TextView distanceTracker, paceTracker, avgPaceTracker, caloriesTracker, timeTracker, stepsTracker;
    private ImageButton pauseButton, stopButton, startButton;
    private Calendar calendar;
    private String date;
    private static final int PERMISSIONS_FINE_LOCATION = 1;
    private GoogleLocationTracker locationTracker;
    private RunUIMetrics uiMetrics;
    private boolean isRunning = false; // Track if the timer is running
    private final List<LatLng> runPath = new ArrayList<>();
    private long lastUpdateTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_running);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        // Retrieve the weight passed from hubActivity
        float userWeightKg = getIntent().getIntExtra("userWeight", 0) / 2.20462f;

        //Initialize location tracker
        locationTracker = new GoogleLocationTracker(this);
        locationTracker.setLocationUpdateListener(this);

        //Initialize UI metrics
        uiMetrics = new RunUIMetrics(this, distanceTracker, paceTracker, avgPaceTracker, caloriesTracker, timeTracker, stepsTracker, userWeightKg, locationTracker);

        startButton.setOnClickListener(v -> {
            if(isRunning){
                Log.d("Debug", "Program is already running");
                return;
            }
            Log.d("Button", "Start Button Pressed");
            //Checks if permission is granted and starts tracking metrics
            if (ActivityCompat.checkSelfPermission(RunningActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Log.d("Debug", "Location accessible");

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(RunningActivity.this, "GPS is turned off. Enable it before starting!", Toast.LENGTH_LONG).show();
                    Log.d("Debug", "GPS not available");
                    return;
                }

                locationTracker.startTracking();//Starts tracking in GoogleLocationTracker
                uiMetrics.startTimer();//Starts tracking in RunUIMetrics
                startButton.setVisibility(View.INVISIBLE);//Hides start button
                stopButton.setVisibility(View.VISIBLE);//Shows stop button
                pauseButton.setVisibility(View.VISIBLE);//Shows pause button
                isRunning = true;
            } else {
                Log.d("Debug", "Missing permissions to start activity");
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);  // Request permission if not granted
            }
        });

        stopButton.setOnClickListener(v -> {
            Location firstKnownLocation = locationTracker.getFirstKnownLocation();
            Location lastKnownLocation = locationTracker.getLastKnownLocation();
            Log.d("Button", "Stop Button Pressed");

            //Checks if permission is granted and stops tracking metrics
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("Debug", "Location permissions have been granted");

                //Checks if last known location is null or invalid
                if (firstKnownLocation == null ||
                        firstKnownLocation.getLatitude() == 0 || firstKnownLocation.getLongitude() == 0 ||
                        lastKnownLocation.getLatitude() == 0 || lastKnownLocation.getLongitude() == 0) {
                    Log.d("Debug", "Could not retrieve last known location");
                    return;
                }

                //Checks if last known location is outdated
                if (SystemClock.elapsedRealtime() - lastKnownLocation.getTime() > 10000) {
                    Toast.makeText(this, "Location data is outdated. Please wait...", Toast.LENGTH_SHORT).show();
                    Log.d("Debug", "Last known location is outdated");
                    return;
                }

                Log.d("Debug", "Tracking has stopped");
                locationTracker.stopTracking();
                uiMetrics.stopTimer();
                stopButton.setEnabled(false);  // Disable stop to prevent repeated clicks
                pauseButton.setEnabled(false); // Disable pause since tracking is permanently stopped
                isRunning = false;
                calendar = Calendar.getInstance();
                DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
                date = df.format(calendar.getTime());

                //Navigates user to hubActivity which then navigates to post run fragment with their run analytics and path
                //Passes all data to hub activity in order for the bottom and side nav views to be visible on fragment
                    Intent i = new Intent(RunningActivity.this, hubActivity.class);
                    i.putParcelableArrayListExtra("runPath", new ArrayList<>(runPath));
                    i.putExtra("isRunning", isRunning);
                    i.putExtra("startLat", firstKnownLocation.getLatitude());
                    i.putExtra("startLng", firstKnownLocation.getLongitude());
                    i.putExtra("lastLat", lastKnownLocation.getLatitude());
                    i.putExtra("lastLng", lastKnownLocation.getLongitude());
                    i.putExtra("calories", uiMetrics.getCaloriesBurned());
                    i.putExtra("avgPace", uiMetrics.getAvgPace());
                    i.putExtra("steps", uiMetrics.getSteps());
                    i.putExtra("totalDistance", uiMetrics.getTotalDistance());
                    i.putExtra("hours", uiMetrics.getHours());
                    i.putExtra("minutes", uiMetrics.getMinutes());
                    i.putExtra("seconds", uiMetrics.getSeconds());
                    i.putExtra("date", date);
                    startActivity(i);
                    finish();
                } else {
                Log.d("Debug", "Missing permissions cant stop activity");
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);  // Request permission if not granted
            }
        });

        pauseButton.setOnClickListener(v -> {
            Log.d("Button", "Pause Button Pressed");
            //Checks if permission is granted and pauses tracking metrics
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Pause tracking
                if (isRunning) {
                    Log.d("Debug", "Tracking paused");
                    locationTracker.stopTracking();
                    uiMetrics.pauseTimer();
                    pauseButton.setImageResource(R.drawable.baseline_play_circle_24);
                    Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show();
                    isRunning = false;
                } else {
                    //Resume tracking
                    Log.d("Button", "Resume Button Pressed");
                    Log.d("Debug", "Tracking resumed");
                    locationTracker.startTracking();
                    uiMetrics.resumeTimer();
                    pauseButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
                    isRunning = true;
                    Toast.makeText(this, "Resumed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    //Updates through google location tracker listener
    public void updateUi (Location location) {
        //Only update UI if the user is running
        if (!isRunning) {
            return;
        }

        runPath.add(new LatLng(location.getLatitude(), location.getLongitude()));

        long currentTime = System.currentTimeMillis();
        //Prevents UI from updating too frequently
        if (currentTime - lastUpdateTime < 1000) {
            return;
        }

        lastUpdateTime = currentTime;

        runOnUiThread(() -> {
            uiMetrics.updateUIData(location);//Updates UI metrics on main thread
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Handles the result of the permission request
        if (requestCode == PERMISSIONS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted: you may start tracking if needed (or simply wait for user action)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                Log.d("Permissions", "Permissions granted");
                locationTracker.startTracking();
            } else {
                Log.d("Permissions", "Permissions denied");
                Toast.makeText(this, "This app requires location permission to function", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //Initializes all views to their layout ids
    private void initViews() {
        distanceTracker = findViewById(R.id.distanceTracker);
        paceTracker = findViewById(R.id.paceTracker);
        avgPaceTracker = findViewById(R.id.avgPaceTracker);
        caloriesTracker = findViewById(R.id.calorieTracker);
        timeTracker = findViewById(R.id.timeTracker);
        stepsTracker = findViewById(R.id.stepsTracker);

        avgPaceTracker.setText("Average Pace: \n0.00 mph");
        caloriesTracker.setText("Calories: \n0");
        distanceTracker.setText("Distance: \n0.00 mph");
        paceTracker.setText("Pace: \n0.00 mph");
        timeTracker.setText("Time: \n00:00:00");
        stepsTracker.setText("Steps: \n0");

        pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setTag("running");
        pauseButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
        pauseButton.setVisibility(View.INVISIBLE);
        stopButton = findViewById(R.id.stopButton);
        stopButton.setVisibility(View.INVISIBLE);
        stopButton.setImageResource(R.drawable.baseline_stop_circle_24);
        startButton = findViewById(R.id.startButton);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uiMetrics != null) {
            uiMetrics.shutdownExecutor(); //Shut down the executor
        }
    }

        @Override
        protected void onPause() {
            super.onPause();
            if(locationTracker != null){
                locationTracker.stopTracking();
            }
            if (uiMetrics != null) {
                uiMetrics.shutdownExecutor(); //Shut down the executor
            }
        }

        @Override
    protected void onResume() {
        super.onResume();

        }

}

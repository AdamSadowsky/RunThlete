package com.example.runthlete;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RunningActivity extends AppCompatActivity {

    //UI elements
    private static final int PERMISSIONS_FINE_LOCATION = 1;
    private TextView distanceTracker, paceTracker, avgPaceTracker, caloriesTracker, timeTracker, stepsTracker;
    private ImageButton pauseButton, stopButton, startButton;

    //Location service objects
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    //A list that stores the users route coordinates
    private List<LatLng> runPath = new ArrayList<>();

    //Initializes variables
    private float totalDistance = 0f; // Total distance in miles
    private long startTime = 0L; // Start time for the run
    private long pauseTime = 0L; // Store time when paused
    private boolean isRunning = false; // Track if the timer is running

    //Handler to update time
    private Handler timeHandler = new Handler();

    private float userWeightKg;

    private float initialStepCount = -1; // Use -1 as a flag that we haven't recorded it yet.

    //Checks if user is running and updates UI accordingly
    private Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                long elapsedMillis = SystemClock.elapsedRealtime() - startTime;
                updateTimeUI(elapsedMillis);
                timeHandler.postDelayed(this, 1000); // Update every second
            }
        }
    };

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

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        initViews();


        // Retrieve the weight passed from hubActivity
        userWeightKg = getIntent().getFloatExtra("userWeight", 0f) / 2.20462f;


        //Initializes location service objects which is used to request locations
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Requests location updates with a high accuracy
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)  // Specifies a minimum update interval
                .setWaitForAccurateLocation(true)  // Waits for high accuracy before updating
                .build();

        // Initialize LocationCallback to handle location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                //Iterates through each location and adds to list
                for (Location location : locationResult.getLocations()) {
                    LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
                    //Checks if user is running and calculates the distance traveled between two points
                    if (!runPath.isEmpty()) {
                        totalDistance += calculateDistance(runPath.get(runPath.size() - 1), newPoint) / 1609.34f;
                    }
                    //Adds new point to list
                    runPath.add(newPoint);
                    updateUIData(location);  // Update the UI with the new location
                }
            }
        };

        HandlerThread locationHandlerThread = new HandlerThread("LocationUpdates");
        locationHandlerThread.start();
        Handler locationHandler = new Handler(locationHandlerThread.getLooper());

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Checks if permission is granted and starts tracking metrics
                if (fusedLocationProviderClient != null &&
                        ActivityCompat.checkSelfPermission(RunningActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    isRunning = true;
                    startTime = SystemClock.elapsedRealtime();
                    timeHandler.post(timeRunnable);
                    //Disables start button and enables pause and stop buttons
                    startButton.setVisibility(View.INVISIBLE);
                    stopButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);
                    Toast.makeText(RunningActivity.this, "Start button pressed", Toast.LENGTH_SHORT).show();
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, locationHandler.getLooper());
                }
                //Requests permission if not granted
                else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);  // Request permission if not granted
                }
            }
        });
        stopButton.setOnClickListener(v -> {
            //Checks if permission is granted and stops tracking metrics
            if (fusedLocationProviderClient != null &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                isRunning = false;
                timeHandler.removeCallbacks(timeRunnable);
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                locationCallback = null; // Optional: Clear the callback to prevent further updates
                Toast.makeText(this, "Tracking Stopped", Toast.LENGTH_SHORT).show();
                stopButton.setEnabled(false);  // Disable stop to prevent repeated clicks
                pauseButton.setEnabled(false); // Disable pause since tracking is permanently stopped
                //Initiates new page
                //Navigates user to post run page with their run analytics and path
                Intent i = new Intent(RunningActivity.this, MapsActivity.class);
                i.putParcelableArrayListExtra("runPath", new ArrayList<>(runPath));
                i.putExtra("totalDistance", totalDistance);
                i.putExtra("elapsedTime", SystemClock.elapsedRealtime() - startTime);
                startActivity(i);
                finish();
            }
            //Requests permission if not granted
            else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);  // Request permission if not granted
            }
        });

        pauseButton.setOnClickListener(v -> {
            //Checks if permission is granted and pauses tracking metrics
            if (fusedLocationProviderClient != null &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Checks tag to determine whether program is currently running or paused
                if (pauseButton.getTag().equals("running")) {
                    // Pause tracking
                    if (sensorManager != null) {
                        sensorManager.unregisterListener(stepCounterListener);
                    }
                    isRunning = false;
                    pauseTime = SystemClock.elapsedRealtime();
                    timeHandler.removeCallbacks(timeRunnable);
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback); //Stops location updates
                    pauseButton.setTag("paused"); //Changes tag to paused for else condition to run on next click
                    pauseButton.setImageResource(R.drawable.baseline_play_circle_24);
                    Toast.makeText(this, "Tracking Paused", Toast.LENGTH_SHORT).show();
                } else {
                    // Resume tracking
                    if (sensorManager != null && stepCounterSensor != null) {
                        sensorManager.registerListener(stepCounterListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    }
                    isRunning = true;
                    startTime += (SystemClock.elapsedRealtime() - pauseTime);
                    timeHandler.post(timeRunnable);
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, locationHandler.getLooper()); //Starts location updates
                    pauseButton.setTag("running"); //Changes tag to running for if condition to run on next click
                    pauseButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
                    Toast.makeText(this, "Tracking Resumed", Toast.LENGTH_SHORT).show();
                }
            }
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
            } else {
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

    private SensorEventListener stepCounterListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (initialStepCount < 0) {
                // First update: record the initial total steps
                initialStepCount = event.values[0];
            }
            // Calculate the steps taken since the run started
            int stepsSinceStart = (int) (event.values[0] - initialStepCount);
            // Update your UI with the steps counted
            String stepCount = String.format(Locale.getDefault(), getString(R.string.steps), stepsSinceStart);
            stepsTracker.setText(stepCount);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

        //Calculates distance between two points in meters
        private float calculateDistance(LatLng start, LatLng end) {
            float[] result = new float[1];
            Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, result);
            return result[0];
        }

        //Updates and sets the time text
        private void updateTimeUI(long elapsedMillis) {
            long seconds = (elapsedMillis / 1000) % 60;
            long minutes = (elapsedMillis / (1000 * 60)) % 60;
            long hours = (elapsedMillis / (1000 * 60 * 60));
            String timeText = String.format(Locale.getDefault(), getString(R.string.time), hours, minutes, seconds);
            timeTracker.setText(timeText);

        }


        //Sets location data metrics to the to each specific UI element
        private void updateUIData(Location location) {
            //Sets distance text
            String distanceText = String.format(Locale.getDefault(), getString(R.string.distance), totalDistance);
            distanceTracker.setText(distanceText);

            //Sets pace text if user is running
            long elapsedMillis = SystemClock.elapsedRealtime() - startTime;
            if (location.hasSpeed() && elapsedMillis > 2000) {
                float speedMps = location.getSpeed();  // Get speed in m/s
                float speedMph = speedMps * 2.23694f;      // Convert to mph
                String paceText = String.format(Locale.getDefault(), getString(R.string.paceMPH), speedMph);
                paceTracker.setText(paceText);
            }

            //Sets average pace text if user is running
            if (elapsedMillis > 2000) {
                float avgSpeedMph = totalDistance / (elapsedMillis / 3600000f);
                String avgPaceText = String.format(Locale.getDefault(), getString(R.string.avg_pace), avgSpeedMph);
                avgPaceTracker.setText(avgPaceText);

                float metValue = determineMET(avgSpeedMph); // A method that returns the appropriate MET value
                float durationHours = (SystemClock.elapsedRealtime() - startTime) / 3600000f;
                int caloriesBurned = (int) calculateCaloriesBurned(metValue, userWeightKg, durationHours);
                String caloriesBurnedText = String.format(Locale.getDefault(), getString(R.string.calories), caloriesBurned);
                caloriesTracker.setText(caloriesBurnedText);
            }

        }

        private float determineMET(float avgSpeedMph) {
            // If nearly stationary, assume resting metabolic rate
            if (avgSpeedMph < 0.5f) {
                return 1.0f;
            } else if (avgSpeedMph < 6) {
                return 8.3f;
            } else if (avgSpeedMph < 7) {
                return 9.8f;
            } else if (avgSpeedMph < 8) {
                return 11.5f;
            } else {
                return 13.5f;
            }
        }

        private float calculateCaloriesBurned(float met, float weightKg, float durationHours) {
            return met * weightKg * durationHours;
        }
    }

package com.company.runthlete;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class RunUIMetrics {

    private final Context context;
    private final TextView distanceView, paceView, avgPaceView, caloriesView, timeView, stepsView;
    private final SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private final GoogleLocationTracker locationTracker;
    private float instantaneousPace = 0f; // mph (immediate reading)
    private float avgPace = 0f;
    private float totalCaloriesSoFar = 0f;  // Accumulated calories (as a float)
    private int caloriesBurned = 0;
    private final Handler timeHandler = new Handler();// Handler for updating the time
    private long hours = 0L;
    private long minutes = 0L;
    private long seconds = 0L;
    private long startTime = 0L;
    private long pauseTime = 0L;
    private long previousUpdateTime = 0L;//Used to calculate time difference between updates
    private Location previousLocation = null;//Last location used to compute a delta
    private float totalDistanceMeters = 0f;
    private float totalDistanceMiles = 0f;
    private int steps = 0;
    private int initialStepCount = -1; //Use -1 as a flag that we haven't recorded it yet.
    private final float userWeightKg;
    private boolean isRunning = false; //Track if the activity is running
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    public RunUIMetrics(Context context, TextView distanceView, TextView paceView,
                               TextView avgPaceView, TextView caloriesView, TextView timeView,
                               TextView stepsView, float userWeightKg, GoogleLocationTracker tracker) {
        this.context = context;
        this.distanceView = distanceView;
        this.paceView = paceView;
        this.avgPaceView = avgPaceView;
        this.caloriesView = caloriesView;
        this.timeView = timeView;
        this.stepsView = stepsView;
        this.userWeightKg = userWeightKg;
        this.locationTracker = tracker;

        //Initializes the step counter
        sensorManager =(SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager !=null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
    }

    private final SensorEventListener stepCounterListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            //Initialize the initial step count if it hasn't been recorded yet
            if (initialStepCount < 0) {
                initialStepCount = (int) event.values[0];
                Log.d("Info", "First step recorded");
            }
            //Calculate the steps taken since the run started
            steps = (int) (event.values[0] - initialStepCount);
            String stepCount = String.format(Locale.getDefault(), context.getString(R.string.steps), steps);//Stores stepCount in a string
            stepsView.setText(stepCount);//Sets stepCount string to layout
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private final Runnable timeRunnable = new Runnable() {
        public void run() {
            //Starts and updates time if program is running every second
            if (isRunning) {
                long elapsedMillis = SystemClock.elapsedRealtime() - startTime;
                updateTimeUI(elapsedMillis);
                timeHandler.postDelayed(this, 1000); // Update every second
            }
        }
    };

    //Initializes variables to default values and begins recording time and step count
    public void startTimer() {
        isRunning = true;
        startTime = SystemClock.elapsedRealtime();
        timeHandler.post(timeRunnable);
        totalDistanceMeters = 0f;
        totalCaloriesSoFar = 0f;
        initialStepCount = -1; // so the step sensor re-initializes
        if (sensorManager != null && stepCounterSensor != null) {
            sensorManager.registerListener(stepCounterListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d("Debug", "Step counter started");
        }
    }

    //Stops tracking of time and step count
    public void stopTimer() {
        isRunning = false;
        timeHandler.removeCallbacks(timeRunnable);
        if (sensorManager != null) {
            sensorManager.unregisterListener(stepCounterListener);
            Log.d("Debug", "Step counter ended");
        }
    }

    //Pauses updates to time and step count
    public void pauseTimer() {
        isRunning = false;
        pauseTime = SystemClock.elapsedRealtime();//Records time paused
        timeHandler.removeCallbacks(timeRunnable);
        if (sensorManager != null) {
            sensorManager.unregisterListener(stepCounterListener);
            Log.d("Debug", "Step counter paused");
        }
    }

    //Resumes updates to time and step count
    public void resumeTimer() {
        isRunning = true;
        //Subtracts pause time from total time
        startTime += (SystemClock.elapsedRealtime() - pauseTime);
        timeHandler.post(timeRunnable);
        if (sensorManager != null && stepCounterSensor != null) {
            sensorManager.registerListener(stepCounterListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d("Debug", "Step counter resumed");
        }
        previousLocation = null;//Prevents large distance jumps from pause to current location
    }


    //Updates and sets the time text to layout
    private void updateTimeUI(long time) {
        seconds = (time / 1000) % 60;
        minutes = (time / (1000 * 60)) % 60;
        hours = (time / (1000 * 60 * 60));
        String timeText = String.format(Locale.getDefault(), context.getString(R.string.time), hours, minutes, seconds);
        timeView.setText(timeText);
    }


    //Sets location data metrics to each specific UI element
    public void updateUIData(Location location) {
        if (!isRunning || location == null) return;

        if (backgroundExecutor.isShutdown()) return;

        try {
            backgroundExecutor.execute(() -> {
        if (locationTracker.getFirstKnownLocation() == null) {
            Log.d("RunUIMetrics", "Initial GPS fix not acquired yet. Skipping UI update.");
            new Handler(context.getMainLooper()).postDelayed(() -> updateUIData(location), 1000);
            return;
        }

        //Records time
        long elapsedMillis = SystemClock.elapsedRealtime() - startTime;

        if (elapsedMillis >= 2000) {
            //Calculate distance in meters if previous location is available
            if (previousLocation != null) {
                float delta = previousLocation.distanceTo(location); // in meters
                totalDistanceMeters += delta;
            }
            previousLocation = location;// Update the previous location for the next iteration

            float speedMps = location.getSpeed();  // speed in m/s
            instantaneousPace = speedMps * 2.23694f; // convert to mph

            updateTimeUI(elapsedMillis);
            updatePace(location);

            //Heavy calculations in a background thread
            backgroundExecutor.execute(() -> {
                long now = SystemClock.elapsedRealtime();
                float deltaTimeHours = 0f;
                //Calculate the time difference between updates if it's not the first update
                if (previousUpdateTime != 0) {
                    deltaTimeHours = (now - previousUpdateTime) / 3600000f;  // Convert ms to hours
                }
                previousUpdateTime = now;//Update the previous time for the next iteration
                float met = determineMET(instantaneousPace);//MET value based on current speed
                float partialCalories = met * userWeightKg * deltaTimeHours;  //Calories for this interval
                totalCaloriesSoFar += partialCalories;
                totalDistanceMiles = totalDistanceMeters / 1609.34f;
                //Compute average pace for the entire run (miles per hour)
                float totalHours = elapsedMillis / 3600000f;
                if (totalHours > 0) {
                    float tempAvgPace = totalDistanceMiles / totalHours;
                    if(tempAvgPace < 1 || tempAvgPace > 20) {
                        avgPace = 0f;
                    } else {
                        avgPace = tempAvgPace;
                    }
                } else {
                    avgPace = 0f;
                }
                //Convert total calories to an integer for display
                caloriesBurned = (int) totalCaloriesSoFar;

                //UI updates go back on main thread
                new Handler(context.getMainLooper()).post(() -> {

                    distanceView.setText(String.format(Locale.getDefault(), context.getString(R.string.distance), totalDistanceMiles));
                    avgPaceView.setText(String.format(Locale.getDefault(), context.getString(R.string.avgPace), avgPace));
                    caloriesView.setText(String.format(Locale.getDefault(), context.getString(R.string.calories), caloriesBurned));
                });
            });
        }
            });
        } catch (RejectedExecutionException e) {
            // The executor is shut down – skip scheduling this task.
            Log.w("RunUIMetrics", "Executor is shut down; skipping task update");
        }
    }

    private void updatePace(Location location) {
        //Needs to ensure 2 seconds have passed before metrics begin recording
        long elapsedMillis = SystemClock.elapsedRealtime() - startTime;

        //Log raw location speed for debugging
        float speedMps = location.getSpeed();  // Speed in meters per second
        float pace = speedMps * 2.23694f;  // Convert to mph

        //Only update pace if program has ran longer than 2 seconds
        if (isRunning && elapsedMillis > 2000) {
            // Ignore inaccurate speeds at the start
            if (pace > 0.1f && pace < 20f) {  // Filter out bad readings
                String paceText = String.format(Locale.getDefault(), context.getString(R.string.paceMPH), pace);
                paceView.setText(paceText);
            } else {
                Log.d("Debug", "Ignored bad speed reading: " + pace);
                paceView.setText("Pace: \n0.00 mph");
            }
        }
    }

    //Determines calories burned based off users pace
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
//Getters and setter for run stats
    public float getAvgPace() {
        return avgPace;
    }

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public float getTotalDistance() {
        return totalDistanceMiles;
    }

    public int getSteps() {
        return steps;
    }

    public long getHours() {
        return hours;
    }

    public long getMinutes() {
        return minutes;
    }

    public long getSeconds() {
        return seconds;
    }

    public void shutdownExecutor() {
        backgroundExecutor.shutdownNow();
    }


}



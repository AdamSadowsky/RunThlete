package com.company.runthlete;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.company.runthlete.databinding.FragmentPostrunBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PostRunFragment extends Fragment implements OnMapReadyCallback {
    private ArrayList<LatLng> runPath;
    private GoogleMap mMap;
    private LatLng firstKnownLocation;
    private LatLng lastKnownLocation;
    private FragmentPostrunBinding binding;
    private FirebaseAuth fAuth;
    private String userID;
    private long hours, minutes, seconds, runSeconds;
    private float avgPace, totalDistance;
    private int calories, steps;
    private String date;
    private FirebaseFirestore db;
    private CollectionReference runsCol;
    private DocumentReference totalRunsDoc;
    private String name;
    private String defaultName;
    Button saveBtn;
    TextView timeTracker, avgPaceTracker, distanceTracker, calorieTracker, stepsTracker;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPostrunBinding.inflate(inflater, container, false);
        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        initViews();

        //If user chooses to save their run this will push all their run data into firestore for them so view later
        saveBtn.setOnClickListener(e -> {
                    fAuth = FirebaseAuth.getInstance();
                    db = FirebaseFirestore.getInstance();
                    userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();

                    //Goes to users collection, then their userid document, and lastly their runs collection
                    runsCol = db
                            .collection("users")
                            .document(userID)
                            .collection("runs");
                    //Stores all of the user saved run total stats in one document
                    totalRunsDoc = db
                            .collection("users")
                            .document(userID)
                            .collection("userInfo")
                            .document("totalRunStats");

                    //Counts all of the users total runs ands sets it to a default name
                    //if user saved run without entering a name else uses the name inputted
                    runsCol.count()
                            .get(AggregateSource.SERVER)
                            .addOnSuccessListener(aggregateQuerySnapshot -> {
                                long totalRuns = aggregateQuerySnapshot.getCount();
                                name = Objects.requireNonNull(binding.runName.getText()).toString().trim();
                                if (name.isEmpty()) {
                                    defaultName = "Run #" + (totalRuns + 1);
                                    writeToFireStore(defaultName);
                                } else {
                                    writeToFireStore(name);
                                }
                            })
                            .addOnFailureListener(x -> {
                                defaultName = "Run #1";
                                Log.e("Error", "Count failed");
                                writeToFireStore(defaultName);
                            });
        });
    }

    //Layout views
    private void initViews() {
        timeTracker = binding.timeTracker;
        avgPaceTracker = binding.avgPaceTracker;
        distanceTracker = binding.distanceTracker;
        calorieTracker = binding.calorieTracker;
        stepsTracker = binding.stepsTracker;
        saveBtn = binding.save;

        //Uses bundle passed to hub activity and sets the data
        Bundle a = getArguments();
        if(a != null) {
            hours         = a.getLong("hours");
            minutes       = a.getLong("minutes");
            seconds       = a.getLong("seconds");
            timeTracker.setText(getString(R.string.time, hours, minutes, seconds));
            avgPace       = a.getFloat("avgPace");
            avgPaceTracker.setText(getString(R.string.avgPace, avgPace));
            totalDistance = a.getFloat("totalDistance");
            distanceTracker.setText(getString(R.string.distance, totalDistance));
            calories      = a.getInt("calories");
            calorieTracker.setText(getString(R.string.calories, calories));
            steps         = a.getInt("steps");
            stepsTracker.setText(getString(R.string.steps, steps));
            runSeconds = (hours * 3600) + (minutes * 60) + seconds;
            date = a.getString("date");
            double startLat = a.getDouble("startLat");
            double startLng = a.getDouble("startLng");
            double lastLat  = a.getDouble("lastLat");
            double lastLng  = a.getDouble("lastLng");
            firstKnownLocation = new LatLng(startLat, startLng);
            lastKnownLocation  = new LatLng(lastLat, lastLng);
            runPath = a.getParcelableArrayList("runPath");
            if(runPath == null) {
                runPath = new ArrayList<>();
            }
        }

        Log.d("MapsActivity", "First Known Location: " + firstKnownLocation);
        Log.d("MapsActivity", "Last Known Location: " + lastKnownLocation);


    }

    //Takes the data retrieved from bundle and puts it into firestore
    private void writeToFireStore(String run_name) {
        //Stores all locations in an array to put in firestore and later generate the run path
        List<GeoPoint> geoPath = new ArrayList<>(runPath.size());
        for(LatLng loc: runPath) {
            geoPath.add(new GeoPoint(loc.latitude, loc.longitude));
        }
        //All run data types stored for a particular run in firestore
        Map<String, Object> runStats = new HashMap<>();
        runStats.put("calories", calories);
        runStats.put("steps", steps);
        runStats.put("time", getString(R.string.timePush, hours, minutes, seconds));
        runStats.put("distance", getString(R.string.distancePush, totalDistance));
        runStats.put("avgPace", getString(R.string.avgPacePush, avgPace));
        runStats.put("date", date);
        runStats.put("name", run_name);
        runStats.put("runPath", geoPath);
        mMap.setOnMapLoadedCallback(() -> mMap.snapshot(snapshot -> {
            if(snapshot == null){
                Log.e("Error", "snapshot is null");
                return;
            }
            File file = new File(requireContext().getFilesDir(), "runMap" + System.currentTimeMillis() + ".png");
            try {
                FileOutputStream out = new FileOutputStream(file);
                boolean compressed = snapshot.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();

                if (!compressed || !file.exists() || file.length() == 0) {
                    Log.e("UploadError", "Failed to save snapshot properly. File missing or empty.");
                    return;
                }
                out.close();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference("map_snapshots/" + file.getName());
                Uri fileUri = Uri.fromFile(file);

                storageReference.putFile(fileUri)
                        .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String downloadUrl = uri.toString();

                                    runStats.put("mapImageUrl", downloadUrl);


                            //Increments all the values in total run stats with new saved run data
                            totalRunsDoc.get().addOnSuccessListener(snapshotDoc -> {
                                WriteBatch batch = db.batch();
                                DocumentReference runsDoc = runsCol.document();
                                batch.set(runsDoc, runStats);//Puts run stats in run doc
                                if (!snapshotDoc.exists()) {
                                    Map<String, Object> totalRunStats = new HashMap<>();
                                    totalRunStats.put("totalCalories", calories);
                                    totalRunStats.put("totalSteps", steps);
                                    totalRunStats.put("totalTime", runSeconds);
                                    totalRunStats.put("totalDistance", totalDistance);
                                    batch.set(totalRunsDoc, totalRunStats);
                                } else {
                                    Map<String, Object> totalRunStats = new HashMap<>();
                                    totalRunStats.put("totalCalories", FieldValue.increment(calories));
                                    totalRunStats.put("totalSteps", FieldValue.increment(steps));
                                    totalRunStats.put("totalTime", FieldValue.increment(runSeconds));
                                    totalRunStats.put("totalDistance", FieldValue.increment(totalDistance));
                                    batch.set(totalRunsDoc, totalRunStats, SetOptions.merge());//Increments total runs doc with total run stats
                                }

                                batch.commit()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("RunFrag", "Run data submitted");
                                            requireActivity()
                                                    .getSupportFragmentManager()
                                                    .beginTransaction()
                                                    .replace(R.id.fragment_container, new HomeFragment())
                                                    .commit();
                                        })
                                        .addOnFailureListener(aVoid -> Log.d("RunFrag", "Failed run data submission"));
                            });
                        })).addOnFailureListener(e -> Log.e("Error", "Failed to upload image", e));
            } catch (IOException e) {
                Log.e("Error", "Failed to save map image", e);
            }
        }));
    }



    //Sets the start and end points on the map for user to see with a polyline
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (lastKnownLocation.latitude != 0 && lastKnownLocation.longitude != 0) {
            //Sets map view to this location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));

            if (runPath != null && !runPath.isEmpty()) {
                polyline(runPath);
            }

            //Sets image at start location
            mMap.addMarker(new MarkerOptions()
                    .position(firstKnownLocation)
                    .title("Start")
                    .icon(BitmapDescriptorFactory.fromBitmap(Objects.requireNonNull(getBitmapFromVectorDrawable(requireContext(), R.drawable.baseline_run_circle_24)))));
            //Sets image at end location
            mMap.addMarker(new MarkerOptions()
                    .position(lastKnownLocation)
                    .title("End")
                    .icon(BitmapDescriptorFactory.fromBitmap(Objects.requireNonNull(getBitmapFromVectorDrawable(requireContext(), R.drawable.baseline_outlined_flag_24)))));
        }
    }

    //Generates the polyline with accurate precision along run path
    private void polyline(final List<LatLng> originalPoints) {
        new Thread(() -> {
            //Sets a low tolerance in order to prevent inaccurate path
            final List<LatLng> processedPoints = simplifyPath(originalPoints, 0.0001);

            //Post the drawing of the polyline back to the main thread.
            requireActivity().runOnUiThread(() -> {
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(processedPoints)
                        .width(15)
                        .color(Color.RED)
                        .geodesic(true);
                mMap.addPolyline(polylineOptions);
                Log.d("Debug", "Simplified polyline drawn with " + processedPoints.size() + " points.");
            });
        }).start();
    }

    //Returns a simplified version of the provided list of LatLng points.
    private List<LatLng> simplifyPath(List<LatLng> points, double tolerance) {
        if (points == null || points.size() < 3) {
            return points;
        }

        //Finds the point with the maximum perpendicular distance from the line
        //connecting the first and last points.
        int index = 0;
        double maxDistance = 0;
        LatLng firstPoint = points.get(0);
        LatLng lastPoint = points.get(points.size() - 1);

        for (int i = 1; i < points.size() - 1; i++) {
            double distance = perpendicularDistance(points.get(i), firstPoint, lastPoint);
            if (distance > maxDistance) {
                index = i;
                maxDistance = distance;
            }
        }

        //If the maximum distance is greater than the tolerance, recursively simplify.
        if (maxDistance > tolerance) {
            List<LatLng> recResults1 = simplifyPath(points.subList(0, index + 1), tolerance);
            List<LatLng> recResults2 = simplifyPath(points.subList(index, points.size()), tolerance);
            //Combine the two results; remove the duplicate point at the split.
            List<LatLng> result = new ArrayList<>(recResults1);
            result.remove(result.size() - 1);
            result.addAll(recResults2);
            return result;
        } else {
            //No point is farther than the tolerance from the line,
            //so we can just return the endpoints.
            List<LatLng> result = new ArrayList<>();
            result.add(firstPoint);
            result.add(lastPoint);
            return result;
        }
    }

    //Computes the perpendicular distance from 'point' to the line segment from 'lineStart' to 'lineEnd'.
    private double perpendicularDistance(LatLng point, LatLng lineStart, LatLng lineEnd) {
        if (lineStart.equals(lineEnd)) {
            return distanceBetween(point, lineStart);
        }
        double dx = lineEnd.longitude - lineStart.longitude;
        double dy = lineEnd.latitude - lineStart.latitude;
        //Compute the perpendicular distance (the area of the triangle times 2 divided by the base length)
        double numerator = Math.abs(dy * point.longitude - dx * point.latitude + lineEnd.longitude * lineStart.latitude - lineEnd.latitude * lineStart.longitude);
        double denominator = Math.sqrt(dx * dx + dy * dy);
        return numerator / denominator;
    }

    //Computes the distance (in meters) between two LatLng points using Android's Location.distanceBetween().
    private double distanceBetween(LatLng p1, LatLng p2) {
        float[] results = new float[1];
        Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results);
        return results[0];
    }

    //Creates bitmap from vector image
    private Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable != null) {
            //Set the drawable's bounds to the desired dimensions.
            drawable.setBounds(0, 0, 80, 80);
            //Create a bitmap with the specified width and height.
            Bitmap bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.mapView.onDestroy();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause(){
        binding.mapView.onPause();
        super.onPause();
    }
}
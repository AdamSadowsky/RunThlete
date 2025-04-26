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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.company.runthlete.databinding.FragmentPostrunBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PostRunFragment extends Fragment implements OnMapReadyCallback {
    public static List<LatLng> runPath;
    private GoogleMap mMap;
    private LatLng firstKnownLocation;
    private LatLng lastKnownLocation;
    private FragmentPostrunBinding binding;
    FirebaseAuth fAuth;
    String userID;


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


        double startLat = requireActivity().getIntent().getDoubleExtra("startLat", 0);
        double startLng = requireActivity().getIntent().getDoubleExtra("startLng", 0);
        double lastLat = requireActivity().getIntent().getDoubleExtra("lastLat", 0);
        double lastLng = requireActivity().getIntent().getDoubleExtra("lastLng", 0);

        firstKnownLocation = new LatLng(startLat, startLng);
        lastKnownLocation = new LatLng(lastLat, lastLng);

        Log.d("MapsActivity", "First Known Location: " + firstKnownLocation);
        Log.d("MapsActivity", "Last Known Location: " + lastKnownLocation);
    }

    private void initViews() {
        TextView timeTracker = requireActivity().findViewById(R.id.timeTracker);
        TextView avgPaceTracker = requireActivity().findViewById(R.id.avgPaceTracker);
        TextView distanceTracker = requireActivity().findViewById(R.id.distanceTracker);
        TextView calorieTracker = requireActivity().findViewById(R.id.calorieTracker);
        TextView stepsTracker = requireActivity().findViewById(R.id.stepsTracker);

        long hours = requireActivity().getIntent().getLongExtra("hours", 0L);
        long minutes = requireActivity().getIntent().getLongExtra("minutes", 0L);
        long seconds = requireActivity().getIntent().getLongExtra("seconds", 0L);
        timeTracker.setText(getString(R.string.time, hours, minutes, seconds));
        float avgPace = requireActivity().getIntent().getFloatExtra("avgPace", 0f);
        avgPaceTracker.setText(getString(R.string.avg_pace, avgPace));
        float totalDistance = requireActivity().getIntent().getFloatExtra("totalDistance", 0f);
        distanceTracker.setText(getString(R.string.distance, totalDistance));
        int calories = requireActivity().getIntent().getIntExtra("calories", 0);
        calorieTracker.setText(getString(R.string.calories, calories));
        int steps = requireActivity().getIntent().getIntExtra("steps", 0);
        stepsTracker.setText(getString(R.string.steps, steps));


        fAuth = FirebaseAuth.getInstance();


        Objects.requireNonNull(fAuth.getCurrentUser()).reload().addOnCompleteListener(item -> {
            userID = fAuth.getCurrentUser().getUid();
            Map<String, Object> userRunMap = new HashMap<>();
            userRunMap.put("calories", calories);
            userRunMap.put("steps", steps);
            userRunMap.put("time", getString(R.string.time, hours, minutes, seconds));
            userRunMap.put("distance", totalDistance);
            userRunMap.put("avgPace", avgPace);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userID).collection("Runs")
                    .add(userRunMap)
                    .addOnSuccessListener(aVoid -> Log.d("RunFrag", "Run data submitted"))
                    .addOnFailureListener(aVoid -> Log.d("RunFrag", "Failed run data submission"));
        });

        Objects.requireNonNull(fAuth.getCurrentUser()).reload().addOnCompleteListener(item -> {
            userID = fAuth.getCurrentUser().getUid();
            Map<String, Object> userTotalRunStats = new HashMap<>();
            userTotalRunStats.put("Calories: ", calories);
            userTotalRunStats.put("Steps: ", steps);
            userTotalRunStats.put("Time: ", getString(R.string.time, hours, minutes, seconds));
            userTotalRunStats.put("Distance: ", totalDistance);
            userTotalRunStats.put("Average Pace: ", avgPace);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userID).collection("Run Stats")
                    .add(userTotalRunStats)
                    .addOnSuccessListener(aVoid -> Log.d("RunFrag", "Run data submitted"))
                    .addOnFailureListener(aVoid -> Log.d("RunFrag", "Failed run data submission"));
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (lastKnownLocation.latitude != 0 && lastKnownLocation.longitude != 0) {
            // ✅ Move camera to the user's last known location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));

            if (runPath != null && !runPath.isEmpty()) {
                polyline(runPath);
            }

            mMap.addMarker(new MarkerOptions()
                    .position(firstKnownLocation)
                    .title("Start")
                    .icon(BitmapDescriptorFactory.fromBitmap(Objects.requireNonNull(getBitmapFromVectorDrawable(requireContext(), R.drawable.baseline_run_circle_24, 80, 80)))));
            mMap.addMarker(new MarkerOptions()
                    .position(lastKnownLocation)
                    .title("End")
                    .icon(BitmapDescriptorFactory.fromBitmap(Objects.requireNonNull(getBitmapFromVectorDrawable(requireContext(), R.drawable.baseline_outlined_flag_24, 80, 80)))));
        }
    }

    private void polyline(final List<LatLng> originalPoints) {
        new Thread(() -> {
            // OPTIONAL: If you have a huge list, you can simplify it.
            // For demonstration, we'll call a stub method; replace it with your algorithm.
            final List<LatLng> processedPoints = simplifyPath(originalPoints, 0.0001); // 10-meter tolerance

            // Post the drawing of the polyline back to the main thread.
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .addAll(processedPoints)
                            .width(15)
                            .color(Color.RED)
                            .geodesic(true);
                    mMap.addPolyline(polylineOptions);
                    Log.d("Debug", "Simplified polyline drawn with " + processedPoints.size() + " points.");
                }
            });
        }).start();
    }

    // Returns a simplified version of the provided list of LatLng points.
// 'tolerance' is in meters—points with a perpendicular distance below this tolerance will be removed.
    private List<LatLng> simplifyPath(List<LatLng> points, double tolerance) {
        if (points == null || points.size() < 3) {
            return points;
        }

        // Find the point with the maximum perpendicular distance from the line
        // connecting the first and last points.
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

        // If the maximum distance is greater than the tolerance, recursively simplify.
        if (maxDistance > tolerance) {
            List<LatLng> recResults1 = simplifyPath(points.subList(0, index + 1), tolerance);
            List<LatLng> recResults2 = simplifyPath(points.subList(index, points.size()), tolerance);
            // Combine the two results; remove the duplicate point at the split.
            List<LatLng> result = new ArrayList<>(recResults1);
            result.remove(result.size() - 1);
            result.addAll(recResults2);
            return result;
        } else {
            // No point is farther than the tolerance from the line,
            // so we can just return the endpoints.
            List<LatLng> result = new ArrayList<>();
            result.add(firstPoint);
            result.add(lastPoint);
            return result;
        }
    }

    // Helper method: computes the perpendicular distance from 'point' to the line segment from 'lineStart' to 'lineEnd'.
// This method uses a simple geometric formula. For small areas, treating LatLng as Cartesian coordinates is acceptable.
    private double perpendicularDistance(LatLng point, LatLng lineStart, LatLng lineEnd) {
        if (lineStart.equals(lineEnd)) {
            return distanceBetween(point, lineStart);
        }
        double dx = lineEnd.longitude - lineStart.longitude;
        double dy = lineEnd.latitude - lineStart.latitude;
        // Compute the perpendicular distance (the area of the triangle times 2 divided by the base length)
        double numerator = Math.abs(dy * point.longitude - dx * point.latitude + lineEnd.longitude * lineStart.latitude - lineEnd.latitude * lineStart.longitude);
        double denominator = Math.sqrt(dx * dx + dy * dy);
        return numerator / denominator;
    }

    // Helper method: computes the distance (in meters) between two LatLng points using Android's Location.distanceBetween().
    private double distanceBetween(LatLng p1, LatLng p2) {
        float[] results = new float[1];
        Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results);
        return results[0];
    }

    private Bitmap getBitmapFromVectorDrawable(Context context, int drawableId, int width, int height) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable != null) {
            // Set the drawable's bounds to the desired dimensions.
            drawable.setBounds(0, 0, width, height);
            // Create a bitmap with the specified width and height.
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }
}
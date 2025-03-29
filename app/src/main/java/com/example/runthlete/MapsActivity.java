package com.example.runthlete;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.runthlete.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mapView;
    private List<LatLng> runPath; // Store the received path
    private LatLng firstKnownLocation;
    private LatLng lastKnownLocation;
    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        double firstLat = getIntent().getDoubleExtra("firstLat", 0);
        double firstLng = getIntent().getDoubleExtra("firstLng", 0);
        double lastLat = getIntent().getDoubleExtra("lastLat", 0);
        double lastLng = getIntent().getDoubleExtra("lastLng", 0);

        firstKnownLocation = new LatLng(firstLat, firstLng);
        lastKnownLocation = new LatLng(lastLat, lastLng);

        runPath = getIntent().getParcelableArrayListExtra("runPath");
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (lastKnownLocation.latitude != 0 && lastKnownLocation.longitude != 0) {
            // ✅ Move camera to the user's last known location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 15));

            // ✅ Draw the polyline from the run
            if (runPath != null && !runPath.isEmpty()) {
                mMap.addPolyline(new PolylineOptions().addAll(runPath).width(10).color(0xFFFF0000));
            }
        } else {
            Toast.makeText(this, "No location data available", Toast.LENGTH_SHORT).show();
        }

        if (runPath != null && !runPath.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(runPath) // ✅ Add all points from runPath
                    .width(8) // ✅ Set polyline width
                    .color(0xFFFF0000) // ✅ Set color (Red)
                    .geodesic(true); // ✅ Make it follow Earth's curvature

            mMap.addPolyline(polylineOptions);
        }


        mMap.addMarker(new MarkerOptions().position(firstKnownLocation).title("Start"));
        mMap.addMarker(new MarkerOptions().position(lastKnownLocation).title("End"));
    }
}
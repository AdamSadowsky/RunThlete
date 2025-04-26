package com.company.runthlete;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.company.runthlete.databinding.FragmentHomeBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Source;

import java.util.Objects;
import java.util.concurrent.Executor;


public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private EditText editTextWeight;
    private TextView editTextGreeting;
    FirebaseAuth auth;
    FirebaseUser user;
    private GoogleLocationTracker locationTracker;
    private GoogleMap mMap;
    private FragmentHomeBinding binding;
    private Location lastKnownLocation;
    private FirebaseAuth fAuth;
    private FirebaseFirestore db;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editTextGreeting = binding.greeting;
                String firstName = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
                if(firstName != null) {
                    editTextGreeting.setVisibility(View.VISIBLE);
                    editTextGreeting.setText(getString(R.string.userGreeting, firstName));
                }


        if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationTracking();
        } else {
            Toast.makeText(getActivity(), "Missing Permissions", Toast.LENGTH_SHORT).show();
            Log.d("Debug", "Missing permissions to start activity");
            locationPermissionLauncher.launch(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
        }

            binding.runningButton.setOnClickListener(v -> {
                    if(validateWeight()) {
                        // Get the weight from the EditText and convert to float
                        String weightStr = Objects.requireNonNull(binding.weightInput.getText()).toString().trim();

                        int weight = 0;
                        try {
                            weight = Integer.parseInt(weightStr);
                        } catch (NumberFormatException n) {
                            Log.d("Debug", "A non integer value was entered as weight");
                            return;
                        }
                        Log.d("RegisterActivity", "run button clicked");
                        Intent i = new Intent(getActivity(), RunningActivity.class);
                        i.putExtra("userWeight", weight); // Pass the weight with key "userWeight"
                        startActivity(i);
                    } else {
                        Toast.makeText(getActivity(), "Weight can not be left empty", Toast.LENGTH_SHORT).show();
                    }
            });

            binding.logoutButton.setOnClickListener(v -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
            });
        }

        private boolean validateWeight() {
            String weight = Objects.requireNonNull(binding.weightInput.getText()).toString().trim();
            editTextWeight = binding.weightInput;
            editTextWeight.setError(null);

            if(weight.isEmpty()){
                editTextWeight.setError("Weight can not be left empty");
                return false;
            }

            return true;
        }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        try {
            locationTracker.startTracking();
        } catch(Exception e) {
            Log.d("Debug", "Start tracking error", e);
        }
        lastKnownLocation = locationTracker.getLastKnownLocation();
        if(lastKnownLocation != null) {
            LatLng lastLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 15));
        } else {
            Log.d("Debug", "Location is null");
        }
    }

    private ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (Boolean.TRUE.equals(fineLocationGranted) || Boolean.TRUE.equals(coarseLocationGranted)) {
                    // Permissions granted: proceed with location tracking.
                    startLocationTracking();
                    Log.d("Debug", "Location granted");
                } else {
                    // Permissions denied: show a message or handle accordingly.
                    Toast.makeText(getActivity(), "This app requires location permission to function", Toast.LENGTH_SHORT).show();
                    Log.d("Debug", "Location not granted");
                    requireActivity().finish();
                }
            });

    @SuppressLint("MissingPermission")
    private void startLocationTracking() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        locationTracker = new GoogleLocationTracker(requireContext());
        locationTracker.setLocationUpdateListener(location -> {
            if (mMap != null && location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                            mMap.setMyLocationEnabled(true);
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    });

                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
        if(locationTracker != null){
            locationTracker.stopTracking();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.mapView.onDestroy();
        binding = null;
    }
}

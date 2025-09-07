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
import java.util.Objects;

public class HomeFragment extends Fragment{

    private FragmentHomeBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView editTextGreeting = binding.greeting;
                String firstName = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
                if(firstName != null) {
                    editTextGreeting.setVisibility(View.VISIBLE);
                    editTextGreeting.setText(getString(R.string.userGreeting, firstName));
                }


            binding.runningButton.setOnClickListener(v -> {
                if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Missing Permissions", Toast.LENGTH_SHORT).show();
                    Log.d("Debug", "Missing permissions to start activity");
                    locationPermissionLauncher.launch(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    });
                    return;
                }
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
        }

        private boolean validateWeight() {
            String weight = Objects.requireNonNull(binding.weightInput.getText()).toString().trim();
            EditText editTextWeight = binding.weightInput;
            editTextWeight.setError(null);

            if(weight.isEmpty()){
                editTextWeight.setError("Weight can not be left empty");
                return false;
            }

            return true;
        }

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (Boolean.FALSE.equals(fineLocationGranted)) {
                    // Permissions denied: show a message or handle accordingly.
                    Toast.makeText(getActivity(), "This app requires location permission to function", Toast.LENGTH_SHORT).show();
                    Log.d("Debug", "Location not granted");
                    requireActivity().finish();
                }
            });


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.company.runthlete;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class RunDetailsFragment extends Fragment {
    TextView timeTracker, avgPaceTracker, distanceTracker, calorieTracker, stepsTracker, nameView, dateView;
    private ImageView mapView;
    private Button backButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_run_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.mapView);
        timeTracker = view.findViewById(R.id.timeTracker);
        avgPaceTracker = view.findViewById(R.id.avgPaceTracker);
        distanceTracker = view.findViewById(R.id.distanceTracker);
        calorieTracker = view.findViewById(R.id.calorieTracker);
        stepsTracker = view.findViewById(R.id.stepsTracker);
        nameView = view.findViewById(R.id.name);
        dateView = view.findViewById(R.id.date);
        backButton = view.findViewById(R.id.backButton);
        String runID = getArguments() != null ? getArguments().getString("runID") : null;
        if (runID != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .collection("runs")
                    .document(runID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            RunData runData = documentSnapshot.toObject(RunData.class);
                            if (runData != null) {
                                //Converts time which is stores as seconds into hours, minutes, and seconds
                                timeTracker.setText(getString(R.string.time, runData.getTime()/3600, runData.getTime()%3600/60, runData.getTime()%3600%60));
                                avgPaceTracker.setText(getString(R.string.avgPace, runData.getAvgPace()));
                                distanceTracker.setText(getString(R.string.distance, runData.getDistance()));
                                calorieTracker.setText(getString(R.string.calories, runData.getCalories()));
                                stepsTracker.setText(getString(R.string.steps, runData.getSteps()));
                                dateView.setText(runData.getDate());
                                nameView.setText(runData.getName());

                                Glide.with(requireContext())
                                        .load(runData.getMapImageUrl())
                                        .into(mapView);
                            }
                        }
                    });
                } else {
            Log.e("RunDetailsFragment", "No runId passed to fragment");
        }

        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SavedRunsFragment())
                    .commit();
        });
    }
}
package com.company.runthlete;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LeaderboardsDailyFragment extends Fragment {
    private ArrayList<LeaderboardsData> leaderboardsList;
    private LeaderboardsAdapter leaderboardsAdapter;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private String userId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboards, container,  false);
        // Inflate the layout for this fragment
        RecyclerView recyclerView = view.findViewById(R.id.leaderboard);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        leaderboardsList = new ArrayList<>();
        leaderboardsAdapter = new LeaderboardsAdapter(requireContext(), leaderboardsList);

        recyclerView.setAdapter(leaderboardsAdapter);

        userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        EventChangeListener();



        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void EventChangeListener() {
        db
                .collection("leaderboards")
                .document("daily")
                .addSnapshotListener((value, error) -> {
                    if(error != null){
                        Log.e("Error", Objects.requireNonNull(error.getMessage()));
                        return;
                    }
                    if(value != null && value.contains("entries")){
                        List<Object> rawEntries = (List<Object>) value.get("entries");
                        leaderboardsList.clear();

                        for(Object obj: rawEntries){
                            if(obj instanceof Map){
                                Map<String, Object> map = (Map<String, Object>) obj;
                                String name = (String) map.get("name");
                                double distance = map.get("totalDistance") instanceof Number ? ((Number) map.get("totalDistance")).doubleValue() : 0;
                                leaderboardsList.add(new LeaderboardsData(distance, name));
                            }
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    leaderboardsAdapter.notifyDataSetChanged();
                });
    }
}
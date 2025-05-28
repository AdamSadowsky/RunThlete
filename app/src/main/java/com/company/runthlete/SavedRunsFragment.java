package com.company.runthlete;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SavedRunsFragment extends Fragment {
    private ArrayList<RunData> runList;
    private SavedRunsAdapter savedRunsAdapter;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private String userId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_runs, container,  false);
        // Inflate the layout for this fragment
        RecyclerView recyclerView = view.findViewById(R.id.pastRuns);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        runList = new ArrayList<>();

        savedRunsAdapter = new SavedRunsAdapter(requireContext(), runList);
        recyclerView.setAdapter(savedRunsAdapter);

        userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        EventChangeListener();

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void EventChangeListener() {
        db.collection("users")
                .document(userId)
                .collection("runs")
                .addSnapshotListener((value, error) -> {
                    if(error != null){
                        Log.e("Error", Objects.requireNonNull(error.getMessage()));
                        return;
                    }
                    runList.clear();
                    List<DocumentChange> docs = Objects.requireNonNull(value).getDocumentChanges();
                    for(DocumentChange doc: docs) {
                        runList.add(0, doc.getDocument().toObject(RunData.class));
                    }
                    progressBar.setVisibility(View.GONE);
                    savedRunsAdapter.notifyDataSetChanged();
                });
    }
}
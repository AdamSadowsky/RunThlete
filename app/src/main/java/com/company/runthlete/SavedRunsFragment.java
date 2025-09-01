package com.company.runthlete;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

        //Assigns a saveRunsAdapter object with these parameters which will be used to inflate
        //the run id of the run card clicked into a a new fragment with additional run info
        savedRunsAdapter = new SavedRunsAdapter(requireContext(), runList, runID -> {
            RunDetailsFragment runDetailsFragment = new RunDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("runID", runID);//Stores runID in bundle
            runDetailsFragment.setArguments(bundle);//Allows runDetailFrag to retrieve runID to inflate run details
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, runDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(savedRunsAdapter);

        userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        EventChangeListener();



        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void EventChangeListener() {
        db
                .collection("users")
                .document(userId)
                .collection("runs")
                .addSnapshotListener((value, error) -> {
                    if(error != null){
                        Log.e("Error", Objects.requireNonNull(error.getMessage()));
                        return;
                    }
                    runList.clear();
                    List<DocumentChange> docs = Objects.requireNonNull(value).getDocumentChanges();
                    //Loops through each run document and then sets its ID to it for it to be accessed later
                    for(DocumentChange doc: docs) {
                        RunData runData = doc.getDocument().toObject(RunData.class);
                        runData.setID(doc.getDocument().getId());
                        runList.add(0, runData);
                    }
                    progressBar.setVisibility(View.GONE);
                    savedRunsAdapter.notifyDataSetChanged();
                });
    }
}
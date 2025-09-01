package com.company.runthlete;

import static android.provider.Settings.System.getString;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class SavedRunsAdapter extends RecyclerView.Adapter<SavedRunsAdapter.RunViewHolder> {

    private final ArrayList<RunData> runList;
    private final OnRunClickListener listener;
    private final Context context;

    //Saves the fragment context for inflating layouts, run card list, and on click listeners for each run id
    public SavedRunsAdapter(Context context, ArrayList<RunData> runList, OnRunClickListener listener) {
        this.context = context;
        this.runList = runList;
        this.listener = listener;
    }

    //Inflates a new runCard view
    //Parent provides the correct layout
    @NonNull
    @Override
    public SavedRunsAdapter.RunViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.run_card, parent, false);
        return new RunViewHolder(itemView);//Wraps the inflated view into RunViewHolder and returns it
    }

    //Assigns each value to its proper field
    @Override
    public void onBindViewHolder(@NonNull SavedRunsAdapter.RunViewHolder holder, int position) {
        RunData runData = runList.get(position);//Gets the runData at this position
        Context context = holder.itemView.getContext();//Gets a valid context from the card view for glide
        holder.runName.setText(runData.getName());
        holder.runDate.setText(runData.getDate());
        holder.time.setText(context.getString(R.string.time, runData.getTime()/3600, runData.getTime()%3600/60, runData.getTime()%3600%60));
        holder.avgPace.setText(context.getString(R.string.avgPace, runData.getAvgPace()));
        holder.distance.setText(context.getString(R.string.distance, runData.getDistance()));

        //Loads the snapshot into the image view of the card
        Glide.with(context)
                .load(runData.getMapImageUrl())
                .into(holder.mapImage);

        //On click checks that run id and listener isn't null
        //Passes runID in onRunClick which calls lambda in run details frag
        //which initiates the fragment switch with the runs details displayed
        holder.itemView.setOnClickListener(v -> {
            String runID = runData.getID();
            if(listener != null && runID != null){
                listener.onRunClick(runID);
            }
        });
    }

    //Returns the the number of runs that will be displayed
    @Override
    public int getItemCount() {
        return runList.size();
    }


    public static class RunViewHolder extends RecyclerView.ViewHolder{
        private final TextView runName, runDate, time, distance, avgPace;
        private final ImageView mapImage;

        //Sets each field to its proper view
        public RunViewHolder(@NonNull View itemView) {
            super(itemView);//Passes the root view(runCard) to the recycler view(parent)
            runName = itemView.findViewById(R.id.runName);
            runDate = itemView.findViewById(R.id.runDate);
            time = itemView.findViewById(R.id.time);
            distance = itemView.findViewById(R.id.distance);
            avgPace = itemView.findViewById(R.id.avgPace);
            mapImage = itemView.findViewById(R.id.mapView);
        }
    }
}

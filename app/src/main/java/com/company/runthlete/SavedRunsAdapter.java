package com.company.runthlete;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

//For self: item view is the root view of the runCard
//parent is the recycler view that holds all the cards
//context refers to the fragment or activity that hosts the recycler view
//holder is an instance of RunViewHolder which accesses all the fields in run card
//super(itemView) passes the full card layout to the RecyclerView.ViewHolder superclass, which stores it as itemView
//recycler view is now able to manage run card through itemView and reuse them


public class SavedRunsAdapter extends RecyclerView.Adapter<SavedRunsAdapter.RunViewHolder> {

    private final ArrayList<RunData> runList;
    private final Context context;

    //Saves the fragment context for inflating layouts and runList
    public SavedRunsAdapter(Context context, ArrayList<RunData> runList) {
        this.context = context;
        this.runList = runList;
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
        holder.time.setText(runData.getTime());
        holder.avgPace.setText(runData.getAvgPace());
        holder.distance.setText(runData.getDistance());

        //Loads the snapshot into the image view of the card
        Glide.with(context)
                .load(runData.getMapImageUrl())
                .into(holder.mapImage);

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

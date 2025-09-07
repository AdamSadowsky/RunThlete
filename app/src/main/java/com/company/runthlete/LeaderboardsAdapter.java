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

public class LeaderboardsAdapter extends RecyclerView.Adapter<LeaderboardsAdapter.RunViewHolder>{
    private final ArrayList<LeaderboardsData> leaderboardsList;
    private final Context context;

    //Saves the fragment context for inflating layouts, run card list, and on click listeners for each run id
    public LeaderboardsAdapter(Context context, ArrayList<LeaderboardsData> leaderboardsList) {
        this.context = context;
        this.leaderboardsList = leaderboardsList;
    }

    //Inflates a new runCard view
    //Parent provides the correct layout
    @NonNull
    @Override
    public LeaderboardsAdapter.RunViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboards_card, parent, false);
        return new LeaderboardsAdapter.RunViewHolder(itemView);//Wraps the inflated view into RunViewHolder and returns it
    }

    //Assigns each value to its proper field
    @Override
    public void onBindViewHolder(@NonNull LeaderboardsAdapter.RunViewHolder holder, int position) {
        LeaderboardsData leaderboardsData = leaderboardsList.get(position);//Gets the runData at this position
        Context context = holder.itemView.getContext();//Gets a valid context from the card view for glide
        holder.userName.setText(leaderboardsData.getUserName());
        holder.distance.setText(context.getString(R.string.distance, leaderboardsData.getDistance()));

        //Loads the snapshot into the image view of the card
        Glide.with(context)
                .load(leaderboardsData.getProfilePicture())
                .placeholder(R.drawable.baseline_account_circle_24)
                .error(R.drawable.baseline_account_circle_24)
                .into(holder.profilePicture);
    }

    //Returns the the number of runs that will be displayed
    @Override
    public int getItemCount() {
        return leaderboardsList.size();
    }

    public static class RunViewHolder extends RecyclerView.ViewHolder{
        private final TextView userName, distance;
        private final ImageView profilePicture;

        //Sets each field to its proper view
        public RunViewHolder(@NonNull View itemView) {
            super(itemView);//Passes the root view(runCard) to the recycler view(parent)
            userName = itemView.findViewById(R.id.userName);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            distance = itemView.findViewById(R.id.distance);
        }
    }
}

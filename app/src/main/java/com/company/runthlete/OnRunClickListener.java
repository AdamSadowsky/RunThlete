package com.company.runthlete;

//For self: item view is the root view of the runCard
//parent is the recycler view that holds all the cards
//context refers to the fragment or activity that hosts the recycler view
//holder is an instance of RunViewHolder which accesses all the fields in run card
//super(itemView) passes the full card layout to the RecyclerView.ViewHolder superclass, which stores it as itemView
//recycler view is now able to manage run card through itemView and reuse them
public interface OnRunClickListener {
    void onRunClick(String runID);
}

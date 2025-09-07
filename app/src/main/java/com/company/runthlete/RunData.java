package com.company.runthlete;

//Documents are downloaded from firestore by the SavedRunsFragment and assigned their ID
//Setters set the values behind the scenes from the firestore run document
//Getters are called to retrieve set values and return them to be assigned or used for logic
public class RunData {
    private String name, date, id, mapImageUrl;
    private double avgPace, distance;
    private long time, calories, steps;

    //Fields value are firsts set then called with get to retrieve it
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setRunDate(String runDate) {
        this.date = runDate;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
    }

    public long getCalories() {
        return calories;
    }

    public void setCalories(long calories) {
        this.calories = calories;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getMapImageUrl() {
        return mapImageUrl;
    }

    public void setMapImageUrl(String mapImageUrl) {
        this.mapImageUrl = mapImageUrl;
    }

    public double getAvgPace() {
        return avgPace;
    }

    public void setAvgPace(double avgPace) {
        this.avgPace = avgPace;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public RunData() {
    }
}

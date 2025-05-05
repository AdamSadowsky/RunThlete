package com.company.runthlete;

import java.lang.reflect.Array;

public class runData {
    String time;
    long calories, steps;
    double avgPace, distance;
    Array runPath;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getCalories() {
        return calories;
    }

    public void setCalories(long calories) {
        this.calories = calories;
    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
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

    public Array getRunPath() {
        return runPath;
    }

    public void setRunPath(Array runPath) {
        this.runPath = runPath;
    }

    public runData(String time, long calories, long steps, double avgPace, double distance, Array runPath) {
        this.time = time;
        this.calories = calories;
        this.steps = steps;
        this.avgPace = avgPace;
        this.distance = distance;
        this.runPath = runPath;
    }
}

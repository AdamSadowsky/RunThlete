package com.company.runthlete;

public class RunData {
    private String name, date, time, mapImageUrl, avgPace, distance;

    //Fields value are firsts set then called with get to retrieve it
    public String getName() {
        return name;
    }

    public void setRunName(String runName) {
        this.name = runName;
    }

    public String getDate() {
        return date;
    }

    public void setRunDate(String runDate) {
        this.date = runDate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMapImageUrl() {
        return mapImageUrl;
    }

    public void setMapImageUrl(String mapImageUrl) {
        this.mapImageUrl = mapImageUrl;
    }

    public String getAvgPace() {
        return avgPace;
    }

    public void setAvgPace(String avgPace) {
        this.avgPace = avgPace;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }


    public RunData(String name, String date, String time, String mapImageUrl, String avgPace, String distance) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.mapImageUrl = mapImageUrl;
        this.avgPace = avgPace;
        this.distance = distance;
    }

    public RunData() {
    }
}

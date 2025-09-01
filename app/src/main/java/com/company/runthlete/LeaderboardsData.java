package com.company.runthlete;

public class LeaderboardsData {
    private String id, userName, profilePicture;
    private double distance;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public LeaderboardsData(){
    }

    public LeaderboardsData(double distance, String userName) {
        this.distance = distance;
        this.userName = userName;
    }
}

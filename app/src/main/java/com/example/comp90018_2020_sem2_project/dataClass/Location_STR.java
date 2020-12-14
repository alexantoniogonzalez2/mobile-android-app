package com.example.comp90018_2020_sem2_project.dataClass;

/*
A class to encapsulate the information relating to the location as string
 */
public class Location_STR {
    private Double Latitude;
    private Double Longitude;

    public Location_STR(){}

    public Location_STR(android.location.Location l){
        this.Latitude = l.getLatitude();
        this.Longitude = l.getLongitude();

    }

    public Double getLatitude() {
        return Latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }
}

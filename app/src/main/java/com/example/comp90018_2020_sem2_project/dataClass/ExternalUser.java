package com.example.comp90018_2020_sem2_project.dataClass;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import java.util.Map;

/*
A class to encapsulate the information relating to another user
 */
public class ExternalUser {

    private String userName;
    private String email;
    private String avatarString;
    private LatLng currentLoc;
    private MarkerOptions mOptions;
    private Marker marker;

    public ExternalUser(DataSnapshot d) {
        try {
            Map<String, String> valueMap = (Map<String, String>) d.getValue();
            Map<String, Double> locMap = (Map<String, Double>) d.child("CurrentLocation").getValue();
            this.userName = valueMap.get("name");
            this.email = valueMap.get("email");
            this.avatarString = valueMap.get("avatar");
            this.currentLoc = new LatLng(locMap.get("latitude"), locMap.get("longitude"));
            this.mOptions = new MarkerOptions().position(currentLoc).title(userName);

        } catch (NullPointerException e) {
        }
    }

    /*
    Sets the users marker
     */
    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    /*
    Returns the latlng of the user
     */
    public LatLng getCurrentLoc() {
        return this.currentLoc;
    }

    /*
    Returns the username
     */
    public String getUserName() {
        return this.userName;
    }

    /*
    Returns the email
     */
    public String getEmail() {
        return this.email;
    }

    /*
    Returns the users marker options
     */
    public MarkerOptions getmOptions() {
        return this.mOptions;
    }

    /*
    Returns the users marker
     */
    public Marker getMarker() { return this.marker; }

    /*
    Returns the avatar string
     */
    public String getAvatarString() {
        return avatarString;
    }

}

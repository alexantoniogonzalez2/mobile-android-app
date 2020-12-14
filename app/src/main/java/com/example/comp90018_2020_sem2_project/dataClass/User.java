package com.example.comp90018_2020_sem2_project.dataClass;

import com.example.comp90018_2020_sem2_project.R;
import com.example.comp90018_2020_sem2_project.dataClass.Location_STR;
import java.io.Serializable;

/*
A class to encapsulate the information relating to a simple user
 */
public class User implements Serializable {
    private String name;
    private String email;
    private Location_STR currentLocation;
    private String avatar;
    private String linkedInUsername;

    public User(){
    }

    public User (String name, String email, String avatar, String linkedInUsername){
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.linkedInUsername = linkedInUsername;
    }

    public String getEmail() {
        return this.email;
    }

    public String getAvatar () {
        return this.avatar;
    }

    public String getName () {
        return this.name;
    }

    public String getLinkedInUsername () {
        return this.linkedInUsername;
    }

    public int getDrawable () {
        int drawableAvatar = R.drawable.boy_idle__2_;
        if (this.avatar.equals("Avatar1")) {
            drawableAvatar = R.drawable.boy_idle__2_;
        } else {
            drawableAvatar = R.drawable.idle__2_;
        }
        return drawableAvatar;

    }

    public void setEmail (String email) {
        this.email = email;
    }

    public void setAvatar (String avatar) {
        this.avatar = avatar;
    }

    public void setName (String name) {
        this.name = name;
    }

    public void setLinkedInUsername (String linkedInUsername) {
        this.linkedInUsername = linkedInUsername;
    }

    public void setCurrentLocation (Location_STR currentLocation) {
        this.currentLocation = currentLocation;
    }

}
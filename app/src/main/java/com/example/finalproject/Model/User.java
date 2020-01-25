/*
File based on tutorial:
KOD DEV (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.

User class represents a User model in the application
*/


package com.example.finalproject.Model;

public class User {

    private String id, username, fullname, imageURL, bio;

    public User(String id, String username, String fullname, String imageURL, String bio) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.imageURL = imageURL;
        this.bio = bio;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}

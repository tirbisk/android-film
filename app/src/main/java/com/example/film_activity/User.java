package com.example.film_activity;

import android.media.Image;

import java.util.ArrayList;

public class User {
    private String username;
    private String password;
    private String bornDate;
    private Integer session_timer;
    private ArrayList<Movie> watchlist;

    private Image profilePictures;


    public User(String username, String password, String bornDate, ArrayList<Movie> watchlist, Integer session_timer) {
        this.username = username;
        this.password = password;
        this.bornDate = bornDate;
        this.watchlist = watchlist;
        this.session_timer = session_timer;
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }



    public Integer getSessionTimer() {
        return session_timer;
    }


    public void setProfilePictures(Image profilePictures) {
        this.profilePictures = profilePictures;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSessionTimer(Integer session_timer) {
        this.session_timer = session_timer;
    }

    public void addToWatchList(Movie movie) {
        this.watchlist.add(movie);
    }

    public void removeFromWatchList(int index) {
        this.watchlist.remove(index);
    }

    public ArrayList<Movie> getWatchlist() {
        return this.watchlist;
    }
}
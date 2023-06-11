package com.example.film_activity;


import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;


public class UserManagement extends AppCompatActivity {
    private final SharedPreferences sharedPreferences;
    private Handler sessionTimeoutHandler;
    private Runnable sessionTimeoutRunnable;
    private static final String SESSION_KEY = "SessionKey";

    public UserManagement(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public User createUser(String name, String password, String date) {
        ArrayList<String> watchlist = new ArrayList<>();
        User user = new User(name, password, date, watchlist, 600000);
        saveUser(user);

        return user;
    }

    private void saveUser(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(user.getUsername(), json);
        editor.apply();
    }

    public User getUserExistence(String username, String password) {
        Gson gson = new Gson();

        String json = sharedPreferences.getString(username, null);
        if (json != null) {
            User user = gson.fromJson(json, User.class);

            if (user != null && user.getPassword().equals(password)) {
                return user;
            }
        }

        return null;
    }

    private void saveSession(String username, Integer session_timer) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SESSION_KEY, username);
        editor.putLong(SESSION_KEY + "_EXPIRATION", System.currentTimeMillis() + session_timer);
        editor.apply();
    }

    private String getSession() {
        long sessionExpiration = sharedPreferences.getLong(SESSION_KEY + "_EXPIRATION", 0);
        if (sessionExpiration > System.currentTimeMillis()) {
            return sharedPreferences.getString(SESSION_KEY, null);
        } else {
            clearSession();
            return null;
        }
    }

    private void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SESSION_KEY);
        editor.remove(SESSION_KEY + "_EXPIRATION");
        editor.apply();
    }

    boolean isUserLoggedIn() {
        return getSession() != null;
    }

    private void startSessionTimer() {
        stopSessionTimer();

        if (isUserLoggedIn()) {
            long sessionExpiration = sharedPreferences.getLong(SESSION_KEY + "_EXPIRATION", 0);
            long remainingTime = sessionExpiration - System.currentTimeMillis();
            if (remainingTime > 0) {
                sessionTimeoutHandler.postDelayed(sessionTimeoutRunnable, remainingTime);
            } else {
                clearSession();
            }
        }
    }

    private void stopSessionTimer() {
        sessionTimeoutHandler.removeCallbacks(sessionTimeoutRunnable);
    }

    public User login(String username, String password) {
        User user = getUserExistence(username, password);

        if (user != null) {
            saveSession(user.getUsername(), user.getSessionTimer());
            startSessionTimer();
            return user;
        } else {
            return null;
        }
    }

    public User getUserLogged() {
        String username = getSession();
        if (username != null) {
            String json = sharedPreferences.getString(username, null);
            if (json != null) {
                Gson gson = new Gson();
                return gson.fromJson(json, User.class);
            }
        }
        return null;
    }

    public User editUser(String username, String newPassword,  Integer newSessionTimer, Image newProfilePictures) {
        String json = sharedPreferences.getString(username, null);
        if (json != null) {
            Gson gson = new Gson();
            User user = gson.fromJson(json, User.class);
            if (user != null) {
                user.setSessionTimer(newSessionTimer);
                user.setProfilePictures(newProfilePictures);
                user.setPassword(newPassword);
                saveUser(user);
                return user;
            }
        }
        return null;
    }
}

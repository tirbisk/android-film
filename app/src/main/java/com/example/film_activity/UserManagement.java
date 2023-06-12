package com.example.film_activity;


import android.content.SharedPreferences;
import android.media.Image;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class UserManagement extends AppCompatActivity {
    private final SharedPreferences sharedPreferences;
    private final Handler sessionTimeoutHandler;
    private final Runnable sessionTimeoutRunnable;
    private static final String SESSION_KEY = "SessionKey";

    public UserManagement(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        sessionTimeoutHandler = new Handler();
        this.sessionTimeoutRunnable = () -> {

        };
    }

    public User createUser(String name, String password, String date) {
        ArrayList<Movie> watchlist = new ArrayList<>();
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
        if (sessionTimeoutHandler != null) {
            sessionTimeoutHandler.removeCallbacks(sessionTimeoutRunnable);
        }
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

    public void addMovieToWatchList(String username, Movie movie) {
        String json = sharedPreferences.getString(username, null);
        if (json != null) {
            Gson gson = new Gson();
            User user = gson.fromJson(json, User.class);
            if (user != null) {
                user.addToWatchList(movie);
                saveUser(user);
            }
        }
    }

    public void removeMovieFromWatchList(String username, int index) {
        String json = sharedPreferences.getString(username, null);
        if (json != null) {
            Gson gson = new Gson();
            User user = gson.fromJson(json, User.class);
            if (user != null) {
                user.removeFromWatchList(index);
                saveUser(user);
            }
        }
    }

    public boolean checkIfUserIs18(String birthDateString) throws ParseException {
        Calendar currentDate = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        birthDate.setTime(sdf.parse(birthDateString));

        int age = currentDate.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
        if (currentDate.get(Calendar.MONTH) < birthDate.get(Calendar.MONTH)
                || (currentDate.get(Calendar.MONTH) == birthDate.get(Calendar.MONTH)
                && currentDate.get(Calendar.DAY_OF_MONTH) < birthDate.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        return age > 18;
    }
}

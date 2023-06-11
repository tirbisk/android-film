package com.example.film_activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    boolean hasConnection = false;
    static ConnectivityManager connectivityManager = null;
    static UserManagement userManagement = null;
    User loggedUser = null;
    private Bitmap capturedPhoto;
    private ActivityResultLauncher<Intent> takePictureLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences UserSharedPreferences = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        userManagement = new UserManagement(UserSharedPreferences);

        if (userManagement.isUserLoggedIn()) {
            this.loggedUser = userManagement.getUserLogged();
            setContentView(R.layout.film_display_list);
        } else {
            setContentView(R.layout.login_page);
        }
        connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
    }
    public void handleClickLoginButton(View view) {
        EditText username = findViewById(R.id.login_page_username_text_field);
        EditText password = findViewById(R.id.login_page_password_text_field);

        String stringUsername = username.getText().toString();
        String stringPassword = password.getText().toString();

        User user = userManagement.login(stringUsername, stringPassword);
        if (user != null) {
            loggedUser = user;
            setContentView(R.layout.film_display_list);
        }
    }

    public void handleClickCreateUserButton(View view) {
        EditText username = findViewById(R.id.create_user_page_username_text_field);
        EditText password = findViewById(R.id.create_user_page_password_text_field);
        EditText born_date = findViewById(R.id.create_user_select_date);

        String stringUsername = username.getText().toString();
        String stringPassword = password.getText().toString();
        String stringBornDate = born_date.getText().toString();

        User newUser = userManagement.createUser(stringUsername, stringPassword, stringBornDate);

        if (newUser != null) {
            userManagement.login(stringUsername, stringPassword);
            setContentView(R.layout.film_display_list);
        }
    }

    public void showDatePickerDialog(View view) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                MainActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String selectedDate = sdf.format(calendar.getTime());

                        EditText select_date_field = findViewById(R.id.create_user_select_date);
                        select_date_field.setText(selectedDate, TextView.BufferType.EDITABLE);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }
    public void handleClickRegisterButton(View view) {
        setContentView(R.layout.register_page);
    }
    public void handleClickFilmListButton(View view) {
        setContentView(R.layout.film_display_list);
    }

    public void handleClickProfilButton(View view) {
        setContentView(R.layout.user_parameters);
    }

    public void handleClickWatchListButton(View view) {
        setContentView(R.layout.watchlist);
    }

    //TODO : not working
    @SuppressLint("QueryPermissionsNeeded")
    public void handleClickTakePictureButton(View view) {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        Bundle extras = data.getExtras();
                        capturedPhoto = (Bitmap) extras.get("data");

                        ImageView imageView = findViewById(R.id.imageView);
                        imageView.setImageBitmap(capturedPhoto);
                    }
                }
        );

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePictureLauncher.launch(takePictureIntent);
        }
    }

}

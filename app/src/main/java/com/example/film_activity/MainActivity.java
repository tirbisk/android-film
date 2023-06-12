package com.example.film_activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    boolean hasConnection = false;
    static ConnectivityManager connectivityManager = null;
    static UserManagement userManagement = null;
    User loggedUser = null;


    private static final String API_KEY = "db36b773ea0954fcc7a30c3e6eae7014";
    private RecyclerView recyclerView;

    private WatchlistAdapter watchlistAdapter;
    private MoviesAdapter movieAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences UserSharedPreferences = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        userManagement = new UserManagement(UserSharedPreferences);

        if (userManagement.isUserLoggedIn()) {
            this.loggedUser = userManagement.getUserLogged();
            setContentView(R.layout.movie_display_list);
            openDisplayMovieListPage();
        } else {
            setContentView(R.layout.login_page);
        }
        connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

    }

    private void performSearch(String query) {
        TMDBApiService apiService = TMDBApiClient.getClient().create(TMDBApiService.class);
        Call<MovieResponse> call = apiService.searchMovies(API_KEY, query);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful()) {
                    MovieResponse movieResponse = response.body();
                    if (movieResponse != null && movieResponse.getResults() != null && !movieResponse.getResults().isEmpty()) {
                        List<Movie> movies = movieResponse.getResults();
                        movieAdapter.setMovies(movies);
                    } else {
                        Snackbar.make(recyclerView, "Aucun résultat trouvé", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("API Error", "Response Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("API Error", "API Call Failed: " + t.getMessage());
            }
        });
    }

    public void handleClickLoginButton(View view) {
        EditText username = findViewById(R.id.login_page_username_text_field);
        EditText password = findViewById(R.id.login_page_password_text_field);

        String stringUsername = username.getText().toString();
        String stringPassword = password.getText().toString();

        User user = userManagement.login(stringUsername, stringPassword);
        if (user != null) {
            loggedUser = user;
            setContentView(R.layout.movie_display_list);
        }
    }

    public void handleClickCreateUserButton(View view) throws ParseException {
        EditText username = findViewById(R.id.create_user_page_username_text_field);
        EditText password = findViewById(R.id.create_user_page_password_text_field);
        EditText born_date = findViewById(R.id.create_user_select_date);

        String stringUsername = username.getText().toString();
        String stringPassword = password.getText().toString();
        String stringBornDate = born_date.getText().toString();

        if (userManagement.checkIfUserIs18(stringBornDate)) {
            User newUser = userManagement.createUser(stringUsername, stringPassword, stringBornDate);

            if (newUser != null) {
                userManagement.login(stringUsername, stringPassword);
                loggedUser = newUser;
                setContentView(R.layout.movie_display_list);
            }
        } else {
            showToast("User not 18");
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
    public void handleClickMovieListButton(View view) {
        setContentView(R.layout.movie_display_list);
        openDisplayMovieListPage();
    }

    public void handleClickProfilButton(View view) {
        setContentView(R.layout.user_parameters);
        onOpenProfilePage();
    }

    public void handleClickWatchListButton(View view) {
        setContentView(R.layout.watchlist);
        onOpenWatchList();
    }

    //TODO : not working
    @SuppressLint("QueryPermissionsNeeded")
    public void handleClickTakePictureButton(View view) {

    }
    //TODO : image management
    public void handleClickSelectPictureButton(View view) {

    }

    private void openDisplayMovieListPage() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        TMDBApiService apiService = TMDBApiClient.getClient().create(TMDBApiService.class);

        Call<MovieResponse> call = apiService.getPopularMovies(API_KEY);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful()) {
                    MovieResponse movieResponse = response.body();
                    if (movieResponse != null && movieResponse.getResults() != null && !movieResponse.getResults().isEmpty()) {
                        List<Movie> movies = movieResponse.getResults();

                        for (int i = 0; i < movies.size(); i++) {
                            Movie movie = movies.get(i);
                            movie.setId(i);
                        }

                        movieAdapter = new MoviesAdapter(movies);
                        recyclerView.setAdapter(movieAdapter);
                    }
                } else {
                    Log.e("API Error", "Response Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("API Error", "API Call Failed: " + t.getMessage());
            }
        });

        EditText searchEditText = findViewById(R.id.search_edit_text);
        Button searchButton = findViewById(R.id.search_button);

        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString();
            performSearch(query);
        });
    }

    @SuppressLint("SetTextI18n")
    private void onOpenProfilePage() {
        EditText sessionTimerEditText = findViewById(R.id.session_timer_edit);
        EditText username = findViewById(R.id.username_edit_user_page);
        EditText password = findViewById(R.id.edit_password_text_page);

        int sessionTimerInMin = loggedUser.getSessionTimer() / 60 / 1000;
        sessionTimerEditText.setText(Integer.toString(sessionTimerInMin));
        username.setText(loggedUser.getUsername());
        password.setText(loggedUser.getPassword());
        username.setEnabled(false);
    }

    public void handleClickSubmitEditUser(View view) {
        EditText sessionTimerEditText = findViewById(R.id.session_timer_edit);
        EditText username = findViewById(R.id.username_edit_user_page);
        EditText password = findViewById(R.id.edit_password_text_page);

        String stringUsername = username.getText().toString();
        String stringPassword = password.getText().toString();
        Integer stringSessionTimer = Integer.parseInt(sessionTimerEditText.getText().toString()) * 60 * 1000;

        loggedUser = userManagement.editUser(stringUsername, stringPassword, stringSessionTimer, null);
    }

    public void handleClickMovie(View view) {
        setContentView(R.layout.one_film_detail_display);
        Movie movie = movieAdapter.movies.get(view.getId());

        ImageView posterMovie = findViewById(R.id.posterDisplayOneMovie);
        EditText movieTitle = findViewById(R.id.displayOneMovieTitle);
        EditText releaseDateTextView = findViewById(R.id.displayOneReleaseDateTextView);
        EditText detailsTextView = findViewById(R.id.displayOneDetailsTextView);
        movieTitle.setEnabled(false);
        releaseDateTextView.setEnabled(false);
        detailsTextView.setEnabled(false);

        String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
        Picasso.get().load(posterUrl).into(posterMovie);

        movieTitle.setText(movie.getTitle());
        releaseDateTextView.setText(movie.getReleaseDate().toString());
        detailsTextView.setText(movie.getOverview());
    }

    public void handleClickAddToWatchlist(View view) {
        int watchlist_length = loggedUser.getWatchlist().size();

        EditText movieTitle = findViewById(R.id.displayOneMovieTitle);

        for (Movie movie: movieAdapter.movies) {
            if (Objects.equals(movie.getTitle(), movieTitle.getText().toString())) {
                Movie movieCopy = new Movie(
                        movie.getTitle(),
                        watchlist_length,
                        movie.getOverview(),
                        movie.getReleaseDate(),
                        movie.getPosterPath()
                );

                userManagement.addMovieToWatchList(loggedUser.getUsername(), movieCopy);
                loggedUser.addToWatchList(movieCopy);
                showToast("Added to watchlist: " + movieTitle.getText().toString());
                break;
            }
        }

    }

    private void onOpenWatchList() {
        recyclerView = findViewById(R.id.watchlistRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Movie> watchlist = loggedUser.getWatchlist();

        for (int i = 0; i < watchlist.size(); i++) {
            Movie movie = watchlist.get(i);
            movie.setId(i);
        }

        watchlistAdapter = new WatchlistAdapter(watchlist);
        recyclerView.setAdapter(watchlistAdapter);
    }
     @SuppressLint("NotifyDataSetChanged")
     public void handleClickRemoveFromWatchlist(View view){
        int index = (Integer) view.getTag();
        userManagement.removeMovieFromWatchList(loggedUser.getUsername(), index);
        loggedUser.removeFromWatchList(index);
        showToast("Removed from watchlist");
        onOpenWatchList();
     }

     private void onRegisterPageOpen(){
         EditText birthDateEditText = findViewById(R.id.create_user_select_date);
         birthDateEditText.setEnabled(false);
     }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

package com.example.film_activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "db36b773ea0954fcc7a30c3e6eae7014";
    private RecyclerView recyclerView;
    private MoviesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                        adapter = new MoviesAdapter(movies);
                        recyclerView.setAdapter(adapter);
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
                        adapter.setMovies(movies);
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
}

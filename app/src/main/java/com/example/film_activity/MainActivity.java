package com.example.film_activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.widget.TextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "db36b773ea0954fcc7a30c3e6eae7014";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TMDBApiService apiService = TMDBApiClient.getClient().create(TMDBApiService.class);

        Call<MovieResponse> call = apiService.getPopularMovies(API_KEY);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful()) {
                    MovieResponse movieResponse = response.body();
                    if (movieResponse != null && movieResponse.getResults() != null && !movieResponse.getResults().isEmpty()) {
                        Movie movie = movieResponse.getResults().get(0); // Récupérez le premier film de la liste

                        TextView titleTextView = findViewById(R.id.titleTextView);
                        TextView overviewTextView = findViewById(R.id.overviewTextView);

                        titleTextView.setText(movie.getTitle());
                        overviewTextView.setText(movie.getOverview());
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

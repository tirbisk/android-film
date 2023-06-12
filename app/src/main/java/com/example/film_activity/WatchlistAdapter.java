package com.example.film_activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.MovieViewHolder> {

    public List<Movie> watchlist;

    public WatchlistAdapter(List<Movie> watchlist) {
        this.watchlist = watchlist;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watchlist, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = watchlist.get(position);
        holder.titleTextView.setText(movie.getTitle());
        holder.releaseDateTextView.setText(movie.getReleaseDate());

        String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
        Picasso.get().load(posterUrl).into(holder.posterImageView);

        holder.removeFromWatchlistButton.setTag(movie.getId());
    }

    @Override
    public int getItemCount() {
        return watchlist.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {

        private final ImageView posterImageView;
        private final TextView titleTextView;
        private final TextView releaseDateTextView;
        private final Button removeFromWatchlistButton;
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            removeFromWatchlistButton = itemView.findViewById(R.id.remove_from_watchlist);
            posterImageView = itemView.findViewById(R.id.posterImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            releaseDateTextView = itemView.findViewById(R.id.releaseDateTextView);
        }
    }
}
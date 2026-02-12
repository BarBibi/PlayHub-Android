package com.example.playhub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<Game> fullGameList; // Full list of games
    private List<Game> displayedList; // List of games to be displayed
    private Context context;

    private OnFavoriteClickListener favoriteListener;
    private List<Integer> userFavoritesIds = new ArrayList<>();

    private OnItemClickListener itemClickListener;

    // Interface for favorite click listener
    public interface OnFavoriteClickListener {
        void onFavoriteClick(int gameId, boolean isFavorite);
    }

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(Game game);
    }

    public GameAdapter(Context context, List<Game> gameList, OnFavoriteClickListener listener) {
        this.context = context;
        this.fullGameList = new ArrayList<>(gameList);
        this.displayedList = gameList;
        this.favoriteListener = listener;
    }

    public void setGames(List<Game> games) {
        this.fullGameList = new ArrayList<>(games);
        this.displayedList = new ArrayList<>(games);
        notifyDataSetChanged();
    }

    public void filter(String query, String genre, String platform) {
        List<Game> filteredList = new ArrayList<>();

        for (Game game : fullGameList) {
            // check name
            boolean matchesName = game.getTitle().toLowerCase().contains(query.toLowerCase());

            // check genre
            boolean matchesGenre = genre.equals("All Genres") || game.getGenre().equalsIgnoreCase(genre);

            // check platform
            boolean matchesPlatform = platform.equals("All Platforms") || game.getPlatform().equalsIgnoreCase(platform);

            // if all conditions are met, add the game to the filtered list
            if (matchesName && matchesGenre && matchesPlatform) {
                filteredList.add(game);
            }
        }

        this.displayedList = filteredList;
        notifyDataSetChanged(); // refresh the list
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = displayedList.get(position);

        // Bind text data
        holder.tvTitle.setText(game.getTitle());
        holder.tvGenre.setText(game.getGenre() + " | " + game.getPlatform());
        holder.tvDescription.setText(game.getShortDescription());

        // Load image using Glide
        Glide.with(context)
                .load(game.getThumbnail())
                .placeholder(android.R.drawable.ic_menu_gallery) // Image while loading
                .into(holder.imgThumbnail);

        // Set the state of the checkbox based on the list
        holder.cbFavorite.setOnCheckedChangeListener(null);

        // Check if the game id is in the list of the user favorite
        boolean isFav = userFavoritesIds.contains(game.getId());
        holder.cbFavorite.setChecked(isFav);

        // Set favorite click listener
        holder.cbFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (favoriteListener != null) {
                favoriteListener.onFavoriteClick(game.getId(), isChecked);
            }
        });

        // Set item click listener
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(game);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayedList.size();
    }

    // Inner class for ViewHolder
    public static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView tvTitle, tvGenre, tvDescription;
        CheckBox cbFavorite;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgGameThumbnail);
            tvTitle = itemView.findViewById(R.id.tvGameTitle);
            tvGenre = itemView.findViewById(R.id.tvGameGenre);
            tvDescription = itemView.findViewById(R.id.tvGameDescription);
            cbFavorite = itemView.findViewById(R.id.cbFavorite);
        }
    }

    // Set favorite status of a game
    public void setFavorites(List<Integer> favorites) {
        this.userFavoritesIds = favorites;
        notifyDataSetChanged();
    }

    // Set item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }
}
package com.example.playhub;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface GameApiService {
    // Defines the endpoint to fetch the list of games
    @GET("api/games")
    Call<List<Game>> getGames();
}
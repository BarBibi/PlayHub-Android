package com.example.playhub;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PlayHubApiService {

    // Create user
    @POST("/api/users")
    Call<ResponseBody> createUser(@Body User user);

    // Get user data
    @GET("/api/users/{uid}")
    Call<User> getUser(@Path("uid") String uid);

    // Update user data
    @PUT("/api/users/{uid}")
    Call<ResponseBody> updateUser(@Path("uid") String uid, @Body User user);

    // Add favorite game
    @POST("/api/users/{uid}/favorites")
    Call<ResponseBody> addFavorite(@Path("uid") String uid, @Body FavoriteRequest request);

    // Remove favorite game
    @HTTP(method = "DELETE", path = "/api/users/{uid}/favorites", hasBody = true)
    Call<ResponseBody> removeFavorite(@Path("uid") String uid, @Body FavoriteRequest request);

    // Get comments for a game
    @GET("/api/comments/{gameId}")
    Call<List<Comment>> getComments(@Path("gameId") int gameId);

    // Add comment
    @POST("/api/comments")
    Call<ResponseBody> addComment(@Body Comment comment);
}

// Helper class for Retrofit calls
class ResponseBody {
    private String message;

    public String getMessage() {
        return message;
    }
}

// Helper class for favorite request
class FavoriteRequest {
    private int gameId;

    public FavoriteRequest(int gameId) {
        this.gameId = gameId;
    }
}
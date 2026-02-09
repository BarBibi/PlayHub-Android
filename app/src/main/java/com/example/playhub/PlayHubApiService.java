package com.example.playhub;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
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
}

class ResponseBody {
    private String message;

    public String getMessage() {
        return message;
    }
}
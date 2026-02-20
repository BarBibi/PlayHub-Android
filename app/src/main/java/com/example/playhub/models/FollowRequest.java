package com.example.playhub.models;

import com.google.gson.annotations.SerializedName;

// Helper class for follow request
public class FollowRequest {
    @SerializedName("currentUserId")
    private String currentUserId;

    @SerializedName("targetUserId")
    private String targetUserId;

    public FollowRequest(String currentUserId, String targetUserId) {
        this.currentUserId = currentUserId;
        this.targetUserId = targetUserId;
    }
}

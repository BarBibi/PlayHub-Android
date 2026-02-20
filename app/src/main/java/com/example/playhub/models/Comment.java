package com.example.playhub.models;

public class Comment {
    private String id;      // MongoDB ID
    private int gameId;
    private String userId;
    private String nickname;
    private String email;
    private String content;
    private String timestamp;

    public Comment(int gameId, String userId, String nickname, String email, String content) {
        this.gameId = gameId;
        this.userId = userId;
        this.nickname = nickname;
        this.email = email;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public int getGameId() {
        return gameId;
    }

    public String getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
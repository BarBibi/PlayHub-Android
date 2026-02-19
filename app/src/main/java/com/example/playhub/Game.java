package com.example.playhub;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Game implements Serializable{
    private int id;
    private String title;
    private String thumbnail;

    @SerializedName("short_description")
    private String shortDescription;

    private String genre;
    private String platform;
    private String publisher;
    private String developer;

    @SerializedName("release_date")
    private String releaseDate;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getThumbnail() { return thumbnail; }
    public String getShortDescription() { return shortDescription; }
    public String getGenre() { return genre; }
    public String getPlatform() { return platform; }
    public String getPublisher() { return publisher; }
    public String getDeveloper() { return developer; }
    public String getReleaseDate() { return releaseDate; }
}

package com.example.playhub.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class User {
    @SerializedName("_id")
    private String uid;
    private String email, password, birthDate, nickname, phone, gender, profileImage;
    private List<Object> favorites;
    private List<String> following = new ArrayList<>();
    private List<String> followers = new ArrayList<>();

    public User() {}

    public User(String uid, String email, String password, String birthDate, String nickname, String phone, String gender) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.nickname = nickname;
        this.phone = phone;
        this.gender = gender;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public List<Object> getFavorites() {
        return favorites;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public List<String> getFollowing() {
        return following;
    }

    public void setFollowing(List<String> following) {
        this.following = following;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }
}

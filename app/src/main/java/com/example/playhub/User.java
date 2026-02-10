package com.example.playhub;

import java.util.List;

public class User {
    private String uid, email, password, birthDate, nickname, phone, gender;
    private List<Object> favorites;

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
}

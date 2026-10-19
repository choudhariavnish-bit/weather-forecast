package com.weather.model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String fullName;
    private String mobile;
    private String email;
    private String passwordHash;
    private String passwordSalt;
    private Integer cityId;
    private boolean active;
    private LocalDateTime createdAt;

    public User() {}

    public User(int userId, String fullName, String mobile, String email, String passwordHash, Integer cityId) {
        this.userId = userId;
        this.fullName = fullName;
        this.mobile = mobile;
        this.email = email;
        this.passwordHash = passwordHash;
        this.cityId = cityId;
        this.active = true;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

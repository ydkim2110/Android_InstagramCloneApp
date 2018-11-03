package com.example.anti2110.instagramcloneapp.Model;

public class UserSettings {

    private User mUser;
    private UserAccountSettings mSettings;

    public UserSettings() {
    }

    public UserSettings(User user, UserAccountSettings settings) {
        mUser = user;
        mSettings = settings;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public UserAccountSettings getSettings() {
        return mSettings;
    }

    public void setSettings(UserAccountSettings settings) {
        mSettings = settings;
    }
}

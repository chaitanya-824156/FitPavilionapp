package com.example.fitpavillion.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.fitpavillion.constants.CONSTANTS;
import com.example.fitpavillion.models.User;
import com.google.gson.Gson;

public class SharedPref {
    private static SharedPref mInstance;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private SharedPref(Context context) {
        sharedPreferences = context.getSharedPreferences(CONSTANTS.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public static SharedPref getInstance(Context context) {
        if (mInstance == null) mInstance = new SharedPref(context);
        return mInstance;
    }

//    public String getUser() {
//        return sharedPreferences.getString(CONSTANTS.USER, null);
//    }
//
//    public void setUser(String user) {
//        editor.putString(CONSTANTS.USER, user);
//        editor.apply();
//    }

    public boolean getProfileComplete() {
        return sharedPreferences.getBoolean(CONSTANTS.USER_PROFILE, false);
    }

    public void setProfileComplete(boolean isLoggedIn) {
        editor.putBoolean(CONSTANTS.USER_PROFILE, isLoggedIn);
        editor.apply();
    }

    public boolean getLogin() {
        return sharedPreferences.getBoolean(CONSTANTS.USER_LOGIN, false);
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(CONSTANTS.USER_LOGIN, isLoggedIn);
        editor.apply();
    }

    public User getUser() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(CONSTANTS.USER, null);
        return gson.fromJson(json, User.class);
    }

    public void setUser(User user) {
        Gson gson = new Gson();
        String json = gson.toJson(user); // myObject - instance of MyObject
        editor.putString(CONSTANTS.USER, json);
        editor.apply();
    }

    public void clearData() {
        editor.clear();
        editor.apply();
    }
}

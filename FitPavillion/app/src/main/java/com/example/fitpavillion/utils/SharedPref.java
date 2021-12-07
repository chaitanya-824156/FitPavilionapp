package com.example.fitpavillion.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fitpavillion.constants.CONSTANTS;
import com.example.fitpavillion.models.Conversation;
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

    public boolean getProfileComplete() {
        return sharedPreferences.getBoolean(CONSTANTS.PROFILE_COMPLETE, false);
    }

    public void setProfileComplete(boolean isLoggedIn) {
        editor.putBoolean(CONSTANTS.PROFILE_COMPLETE, isLoggedIn);
        editor.apply();
    }

    public String getProfileType() {
        return sharedPreferences.getString(CONSTANTS.PROFILE_TYPE, null);
    }

    public void setProfileType(String type) {
        if (type == null) editor.remove(CONSTANTS.PROFILE_TYPE);
        else editor.putString(CONSTANTS.PROFILE_TYPE, type);
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

    public void setConv(Conversation conv) {
        if (conv != null) {
            Gson gson = new Gson();
            String json = gson.toJson(conv); // myObject - instance of MyObject
            editor.putString(CONSTANTS.OPEN_CONVERSATION, json);
        } else editor.remove(CONSTANTS.OPEN_CONVERSATION);
        editor.apply();
    }

    public Conversation getConvo() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(CONSTANTS.OPEN_CONVERSATION, null);
        return gson.fromJson(json, Conversation.class);
    }

    public void clearData() {
        editor.clear();
        editor.apply();
    }
}

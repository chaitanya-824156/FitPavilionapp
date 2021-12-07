package com.example.fitpavillion.models;


import android.text.format.DateFormat;

import java.util.Date;

public class Conversation {
    private String id;
    private String chat_id;
    private String USER_NAME;
    private String TRAINER_NAME;
    private long updated;
    private String TRAINER;
    private String USER;
    private String USER_TOKEN;
    private String TRAINER_TOKEN;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getUSER_NAME() {
        return USER_NAME;
    }

    public void setUSER_NAME(String USER_NAME) {
        this.USER_NAME = USER_NAME;
    }

    public String getTRAINER_NAME() {
        return TRAINER_NAME;
    }

    public void setTRAINER_NAME(String TRAINER_NAME) {
        this.TRAINER_NAME = TRAINER_NAME;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public String getUpdatedDateString() {
        return String.valueOf(DateFormat.format("yyyy-MM-dd hh:mm aaa", new Date(updated)));
    }

    public String getTRAINER() {
        return TRAINER;
    }

    public void setTRAINER(String TRAINER) {
        this.TRAINER = TRAINER;
    }

    public String getUSER() {
        return USER;
    }

    public void setUSER(String USER) {
        this.USER = USER;
    }

    public String getUSER_TOKEN() {
        return USER_TOKEN;
    }

    public void setUSER_TOKEN(String USER_TOKEN) {
        this.USER_TOKEN = USER_TOKEN;
    }

    public String getTRAINER_TOKEN() {
        return TRAINER_TOKEN;
    }

    public void setTRAINER_TOKEN(String TRAINER_TOKEN) {
        this.TRAINER_TOKEN = TRAINER_TOKEN;
    }
}

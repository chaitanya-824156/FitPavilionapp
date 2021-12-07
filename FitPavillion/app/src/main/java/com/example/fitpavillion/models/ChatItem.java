package com.example.fitpavillion.models;


import android.text.format.DateFormat;

import java.util.Date;

public class ChatItem {
    private String id;
    private String from;
    private String to;
    private String message;
    private long date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getDateString() {
        return String.valueOf(DateFormat.format("yyyy-MM-dd hh:mm aaa", new Date(date)));
    }
}

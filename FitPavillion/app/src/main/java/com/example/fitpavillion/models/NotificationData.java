package com.example.fitpavillion.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotificationData {
    @Expose
    @SerializedName("title")
    private String title;

    @Expose
    @SerializedName("body")
    private String body;

    @Expose
    @SerializedName("chat")
    private String chat;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }
}

package com.example.fitpavillion.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FcmNotification {
    @Expose
    @SerializedName("to")
    private String to;
    @Expose
    @SerializedName("priority")
    private String priority;
    @Expose
    @SerializedName("notification")
    private NotificationData notification;
    @Expose
    @SerializedName("data")
    private NotificationData data;


    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public NotificationData getNotification() {
        return notification;
    }

    public void setNotification(NotificationData notification) {
        this.notification = notification;
    }

    public NotificationData getData() {
        return data;
    }

    public void setData(NotificationData data) {
        this.data = data;
    }
}

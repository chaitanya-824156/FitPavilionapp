package com.example.fitpavillion.models;

import android.text.format.DateFormat;

import java.util.Date;

public class FoodConsumption {
    private String id;
    private String name;
    private double calorie;
    private double quantity;
    private double totalQuantity;
    private long date;

    public FoodConsumption() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCalorie() {
        return calorie;
    }

    public void setCalorie(double calorie) {
        this.calorie = calorie;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(double totalQuantity) {
        this.totalQuantity = totalQuantity;
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

package com.example.fitpavillion.models;

public class FoodItem {
    private String id;
    private String name;
    private double carbohydrates;
    private double protien;
    private double fat;
    private double calorie;

    public FoodItem() {
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

    public double getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public double getProtien() {
        return protien;
    }

    public void setProtien(double protien) {
        this.protien = protien;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getCalorie() {
        return calorie;
    }

    public void setCalorie(double calorie) {
        this.calorie = calorie;
    }

    @Override
    public String toString() {
        return "FoodItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", carbohydrates=" + carbohydrates +
                ", protien=" + protien +
                ", fat=" + fat +
                ", calorie=" + calorie +
                '}';
    }
}

package com.example.fitpavillion.models;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.util.List;

public class WorkOutPlan implements Parcelable {
    public static final Creator<WorkOutPlan> CREATOR = new Creator<WorkOutPlan>() {
        @Override
        public WorkOutPlan createFromParcel(Parcel in) {
            return new WorkOutPlan(in);
        }

        @Override
        public WorkOutPlan[] newArray(int size) {
            return new WorkOutPlan[size];
        }
    };
    private String id;
    private String name;
    private int reps;
    private int count;
    private int durationInMins;
    private String imageUrl;
    private List<String> days;
    private boolean active;


    public WorkOutPlan() {

    }

    protected WorkOutPlan(Parcel in) {
        id = in.readString();
        name = in.readString();
        reps = in.readInt();
        count = in.readInt();
        durationInMins = in.readInt();
        imageUrl = in.readString();
        days = in.createStringArrayList();
        active = in.readByte() != 0;
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

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getDurationInMins() {
        return durationInMins;
    }

    public void setDurationInMins(int durationInMins) {
        this.durationInMins = durationInMins;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(count);
        dest.writeInt(reps);
        dest.writeInt(durationInMins);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeList(days);
    }
}

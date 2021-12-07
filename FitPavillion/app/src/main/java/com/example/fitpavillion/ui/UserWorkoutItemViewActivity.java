package com.example.fitpavillion.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.example.fitpavillion.R;
import com.example.fitpavillion.models.WorkOutPlan;
import com.google.gson.Gson;

public class UserWorkoutItemViewActivity extends AppCompatActivity {

    private WorkOutPlan workOutPlan;
    private AppCompatTextView name, reps, count, duration, days_tv;
    private ImageView imageView;

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Workout Plans");
        }

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            WorkOutPlan plan = getWorkoutPlan(intent.getStringExtra("item"));
            if (plan != null) updateFields(plan);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public WorkOutPlan getWorkoutPlan(String json) {
        Gson gson = new Gson();
        workOutPlan = gson.fromJson(json, WorkOutPlan.class);
        return workOutPlan;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_workout_item_view);

        imageView = findViewById(R.id.wo_u_image_preview);
        name = findViewById(R.id.wo_u_name);
        reps = findViewById(R.id.wo_u_reps);
        count = findViewById(R.id.wo_u_count);
        days_tv = findViewById(R.id.wo_u_days);
        duration = findViewById(R.id.wo_u_duration);
    }

    private void updateFields(WorkOutPlan plan) {
        if (plan == null) return;
        name.setText(plan.getName());
        reps.setText(new StringBuilder().append("Reps : ").append(plan.getReps()));
        count.setText(new StringBuilder().append("Count : ").append(plan.getCount()).toString());
        duration.setText(new StringBuilder().append("Duration : ").append(plan.getDurationInMins()).append(" mins").toString());
        if (plan.getImageUrl() != null && !plan.getImageUrl().equals("")) {
            Glide.with(this)
                    .load(plan.getImageUrl())
                    .into(imageView)
                    .onLoadFailed(getApplicationContext().getResources().getDrawable(R.drawable.app_icon));
            imageView.setVisibility(View.VISIBLE);
        } else imageView.setVisibility(View.GONE);

        if (plan.getDays() != null && plan.getDays().size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < plan.getDays().size(); j++) {
                stringBuilder.append(plan.getDays().get(j));
                if (j != plan.getDays().size() - 1) stringBuilder.append(", ");
            }
            days_tv.setText("Days : " + stringBuilder.toString());
        }
    }
}
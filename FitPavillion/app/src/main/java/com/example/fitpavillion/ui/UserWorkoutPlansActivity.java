package com.example.fitpavillion.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.R;
import com.example.fitpavillion.adapters.WorkOutAdapter;
import com.example.fitpavillion.models.WorkOutPlan;
import com.example.fitpavillion.utils.Callback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserWorkoutPlansActivity extends AppCompatActivity {
    private static final String TAG = "UserWorkoutPlansActivit";
    private RecyclerView workout_rv;
    private WorkOutAdapter workOutAdapter;
    private ArrayList<WorkOutPlan> workOutPlanList;
    private Map<String, WorkOutPlan> workOutPlanMap;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private Query ref = db.getReference("workoutPlans").orderByChild("active").equalTo(true);

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Workout Plans");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        ref.removeEventListener(WO_listener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWorkoutPlans();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_workout_plans);

        workout_rv = findViewById(R.id.user_workouts_recycler);

        workOutPlanList = new ArrayList<>();
        workOutPlanMap = new HashMap<>();

        workOutAdapter = new WorkOutAdapter(workOutPlanList, this);
        workOutAdapter.setClickListener(workOutPlanCallback());
        workout_rv.setAdapter(workOutAdapter);
        getWorkoutPlans();
    }

    private void getWorkoutPlans() {
        ref.addValueEventListener(WO_listener());
    }

    private ValueEventListener WO_listener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    WorkOutPlan plan = dataSnapshot.getValue(WorkOutPlan.class);
                    workOutPlanMap.put(plan.getId(), plan);
                }
                workOutPlanList.clear();
                for (WorkOutPlan p : workOutPlanMap.values()) workOutPlanList.add(p);
                workOutAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        };
    }

    private Callback<WorkOutPlan> workOutPlanCallback() {
        return new Callback<WorkOutPlan>() {
            @Override
            public void result(WorkOutPlan result) {
                Intent intent = new Intent(UserWorkoutPlansActivity.this, UserWorkoutItemViewActivity.class);
                Gson gson = new Gson();
                String json = gson.toJson(result);
                intent.putExtra("item", json);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
    }
}
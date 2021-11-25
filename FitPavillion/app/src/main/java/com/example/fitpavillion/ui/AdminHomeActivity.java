package com.example.fitpavillion.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.R;
import com.example.fitpavillion.adapters.WorkOutAdapter;
import com.example.fitpavillion.models.FoodItem;
import com.example.fitpavillion.models.WorkOutPlan;
import com.example.fitpavillion.utils.Callback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class AdminHomeActivity extends AppCompatActivity implements Callback<WorkOutPlan> {

    public static final int PICK_IMAGE = 1;
    private static final String TAG = "AdminHomeActivity";
    private boolean workout = true;
    private WorkOutAdapter workOutAdapter;
    private RecyclerView workout_rv;
    private ArrayList<WorkOutPlan> workOutPlanList;
    private ArrayList<FoodItem> foodItemsList;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private Query query = mDatabase.child("workoutPlans");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        workout_rv = findViewById(R.id.admin_workouts_recycler);
        workOutPlanList = new ArrayList<>();
        foodItemsList = new ArrayList<>();
        workOutAdapter = new WorkOutAdapter(workOutPlanList, this);
        workOutAdapter.setClickListener(workOutPlanCallback());
        workout_rv.setAdapter(workOutAdapter);
        getWorkoutPlans();
    }

    private void getWorkoutPlans() {
        query.addValueEventListener(WO_listener());
    }

    @Override
    protected void onPause() {
        super.onPause();
        query.removeEventListener(WO_listener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        query.addValueEventListener(WO_listener());
    }

    private ValueEventListener WO_listener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
//                ArrayList<WorkOutPlan> p = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    WorkOutPlan plan = s.getValue(WorkOutPlan.class);
                    for (DataSnapshot sq : s.getChildren()) {
                        Log.d(TAG, "onDataChange: " + sq.getKey() + ":    " + sq.getValue());
                    }
                    workOutPlanList.add(plan);
                }
//                workOutPlanList = new ArrayList<>(p);
                workOutAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.menu_workout).setVisible(workout);
        menu.findItem(R.id.menu_food).setVisible(!workout);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_food:
                workout = !workout;
                invalidateOptionsMenu();
                break;
            case R.id.menu_workout:
                workout = !workout;
                invalidateOptionsMenu();
                break;
            case R.id.add_new:
                if (workout) {
                    Intent intent = new Intent(AdminHomeActivity.this, WorkoutFormActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                }
                break;
            default:
                break;
        }

        return true;
    }


    @Override
    public void result(WorkOutPlan result) {

    }

    private Callback<FoodItem> foodItemCallback() {
        return new Callback<FoodItem>() {
            @Override
            public void result(FoodItem result) {

            }
        };
    }

    private Callback<WorkOutPlan> workOutPlanCallback() {
        return new Callback<WorkOutPlan>() {
            @Override
            public void result(WorkOutPlan result) {
                Intent intent = new Intent(AdminHomeActivity.this, WorkoutFormActivity.class);
                Gson gson = new Gson();
                String json = gson.toJson(result);
                intent.putExtra("item", json);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
    }
}
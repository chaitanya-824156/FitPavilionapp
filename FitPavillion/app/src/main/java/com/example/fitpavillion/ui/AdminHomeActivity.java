package com.example.fitpavillion.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.MainActivity;
import com.example.fitpavillion.R;
import com.example.fitpavillion.adapters.FoodItemAdapter;
import com.example.fitpavillion.adapters.WorkOutAdapter;
import com.example.fitpavillion.models.FoodItem;
import com.example.fitpavillion.models.WorkOutPlan;
import com.example.fitpavillion.utils.Callback;
import com.example.fitpavillion.utils.LoginAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminHomeActivity extends AppCompatActivity implements Callback<WorkOutPlan> {

    public static final int PICK_IMAGE = 1;
    private static final String TAG = "AdminHomeActivity";
    private boolean workout = true;
    private WorkOutAdapter workOutAdapter;
    private FoodItemAdapter foodItemAdapter;
    private RecyclerView workout_rv, food_rv;
    private FrameLayout wo_frame, fd_frame;
    private ArrayList<WorkOutPlan> workOutPlanList;
    private Map<String, WorkOutPlan> workOutPlanMap;
    private ArrayList<FoodItem> foodItemsList;
    private Map<String, FoodItem> foodItemMap;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private Query woQuery = mDatabase.child("workoutPlans");
    private Query fQuery = mDatabase.child("foodDetails");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        workout_rv = findViewById(R.id.admin_workouts_recycler);
        food_rv = findViewById(R.id.admin_food_recycler);
        fd_frame = findViewById(R.id.layout_food);
        wo_frame = findViewById(R.id.layout_workout);
        workOutPlanList = new ArrayList<>();
        workOutPlanMap = new HashMap<>();
        foodItemsList = new ArrayList<>();
        foodItemMap = new HashMap<>();
        workOutAdapter = new WorkOutAdapter(workOutPlanList, this);
        foodItemAdapter = new FoodItemAdapter(foodItemsList, this);
        workOutAdapter.setClickListener(workOutPlanCallback());
        foodItemAdapter.setClickListener(foodItemCallback());
        workout_rv.setAdapter(workOutAdapter);
        food_rv.setAdapter(foodItemAdapter);
        getWorkoutPlans();
        getFoodDetails();
    }

    private void getWorkoutPlans() {
        woQuery.addValueEventListener(WO_listener());
    }

    private void getFoodDetails() {
        fQuery.addValueEventListener(FD_listener());
    }

    @Override
    protected void onPause() {
        super.onPause();
        woQuery.removeEventListener(WO_listener());
        fQuery.removeEventListener(WO_listener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWorkoutPlans();
        getFoodDetails();
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

    private ValueEventListener FD_listener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FoodItem plan = dataSnapshot.getValue(FoodItem.class);
                    foodItemMap.put(plan.getId(), plan);
                }
                foodItemsList.clear();
                for (FoodItem p : foodItemMap.values()) foodItemsList.add(p);
                foodItemAdapter.notifyDataSetChanged();
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
        menu.findItem(R.id.menu_workout).setVisible(!workout);
        menu.findItem(R.id.menu_food).setVisible(workout);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_food:
                wo_frame.setVisibility(View.GONE);
                fd_frame.setVisibility(View.VISIBLE);
                workout = !workout;
                invalidateOptionsMenu();
                break;
            case R.id.menu_workout:
                wo_frame.setVisibility(View.VISIBLE);
                fd_frame.setVisibility(View.GONE);
                workout = !workout;
                invalidateOptionsMenu();
                break;
            case R.id.add_new:
                if (workout) {
                    Intent intent = new Intent(AdminHomeActivity.this, WorkoutFormActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(AdminHomeActivity.this, FoodFormActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            case R.id.admin_logout:
                new LoginAuth().signOut();
                Intent intent = new Intent(AdminHomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
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
                Intent intent = new Intent(AdminHomeActivity.this, FoodFormActivity.class);
                Gson gson = new Gson();
                String json = gson.toJson(result);
                intent.putExtra("item", json);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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
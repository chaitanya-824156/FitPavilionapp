package com.example.fitpavillion.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.R;
import com.example.fitpavillion.adapters.FoodItemAdapter;
import com.example.fitpavillion.models.FoodItem;
import com.example.fitpavillion.utils.Callback;
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

public class FoodSelectorActivity extends AppCompatActivity {
    private static final String TAG = "FoodSelectorActivity";
    private ArrayList<FoodItem> foodItemsList;
    private Map<String, FoodItem> foodItemMap;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private RecyclerView food_rv;
    private Query fQuery = mDatabase.child("foodDetails");
    private FoodItemAdapter foodItemAdapter;

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Select Food");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_selector);
        food_rv = findViewById(R.id.admin_food_recycler);
        foodItemsList = new ArrayList<>();
        foodItemMap = new HashMap<>();
        foodItemAdapter = new FoodItemAdapter(foodItemsList, this);
        foodItemAdapter.setClickListener(foodItemCallback());
        food_rv.setAdapter(foodItemAdapter);
        getFoodDetails();
    }

    private void getFoodDetails() {
        fQuery.addValueEventListener(FD_listener());
    }

    @Override
    protected void onPause() {
        super.onPause();
        fQuery.removeEventListener(FD_listener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFoodDetails();
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

    private Callback<FoodItem> foodItemCallback() {
        return new Callback<FoodItem>() {
            @Override
            public void result(FoodItem result) {
                Intent intent = new Intent(FoodSelectorActivity.this, FoodDairyFormActivity.class);
                Gson gson = new Gson();
                String json = gson.toJson(result);
                intent.putExtra("item", json);
                setResult(RESULT_OK, intent);
                finish();
            }
        };
    }
}
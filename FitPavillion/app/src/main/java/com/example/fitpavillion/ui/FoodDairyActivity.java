package com.example.fitpavillion.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.R;
import com.example.fitpavillion.adapters.FoodDairyAdapter;
import com.example.fitpavillion.models.FoodConsumption;
import com.example.fitpavillion.models.User;
import com.example.fitpavillion.utils.Callback;
import com.example.fitpavillion.utils.LoginAuth;
import com.example.fitpavillion.utils.SharedPref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class FoodDairyActivity extends AppCompatActivity {
    private static final String TAG = "FoodDairyActivity";
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = db.getReference("daily_consumption");
    private FirebaseUser user;
    private FirebaseAuth auth;
    private FoodDairyAdapter adapter;
    private ArrayList<FoodConsumption> dataList;
    private Map<String, FoodConsumption> dataMap;
    private User profile;

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Food Dairy");
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
        setContentView(R.layout.activity_food_dairy);
        auth = LoginAuth.getInstance().getAuth();
        user = auth.getCurrentUser();
        SharedPref sharedPref = SharedPref.getInstance(this);
        profile = sharedPref.getUser();

        RecyclerView recyclerView = findViewById(R.id.food_dairy_recycler);
        dataList = new ArrayList<>();
        dataMap = new HashMap<>();

        adapter = new FoodDairyAdapter(dataList, this);
        adapter.setClickListener(ConversationCallback());
        recyclerView.setAdapter(adapter);
        getFoodConsumptionList();
    }

    private void getFoodConsumptionList() {
        ref.orderByChild("userId").equalTo(profile.getUid()).addValueEventListener(FC_listener());
    }

    private ValueEventListener FC_listener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FoodConsumption value = dataSnapshot.getValue(FoodConsumption.class);
                    dataMap.put(value.getId(), value);
                }
                dataList.clear();
                dataList.addAll(dataMap.values());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Collections.sort(dataList, Comparator.comparingLong(FoodConsumption::getDate).reversed());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        };
    }

    private Callback<FoodConsumption> ConversationCallback() {
        return new Callback<FoodConsumption>() {
            @Override
            public void result(FoodConsumption result) {
//                Intent intent = new Intent(FoodDairyActivity.this, ChatActivity.class);
//                Gson gson = new Gson();
//                String json = gson.toJson(result);
//                intent.putExtra("conversation", json);
//                startActivity(intent);
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.food_dairy_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.food_dairy_form) {
            Intent intent = new Intent(FoodDairyActivity.this, FoodDairyFormActivity.class);
            startActivity(intent);
            finish();
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        ref.removeEventListener(FC_listener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFoodConsumptionList();
    }
}
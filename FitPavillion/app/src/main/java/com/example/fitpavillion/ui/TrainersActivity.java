package com.example.fitpavillion.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.R;
import com.example.fitpavillion.adapters.TrainersAdapter;
import com.example.fitpavillion.models.User;
import com.example.fitpavillion.utils.Callback;
import com.example.fitpavillion.utils.LoginAuth;
import com.example.fitpavillion.utils.SharedPref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TrainersActivity extends AppCompatActivity {

    private static final String TAG = "TrainersActivity";
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private Query query;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private TrainersAdapter adapter;
    private ArrayList<User> dataList;
    private Map<String, User> dataMap;
    private RecyclerView recyclerView;
    private SharedPref sharedPref;

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Trainers");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainers);

        auth = LoginAuth.getInstance().getAuth();
        user = auth.getCurrentUser();
        sharedPref = SharedPref.getInstance(this);
        User profile = sharedPref.getUser();

        query = db.getReference().child("users").orderByChild("profile/profileType").equalTo("TRAINER");
        query.keepSynced(true);

        recyclerView = findViewById(R.id.trainer_list_recycler);

        dataList = new ArrayList<>();
        dataMap = new HashMap<>();

        adapter = new TrainersAdapter(dataList, this);
        adapter.setClickListener(ConversationCallback());
        recyclerView.setAdapter(adapter);
        getTrainersList();
    }

    private void getTrainersList() {
        query.addValueEventListener(Trainers_listener());
    }

    private ValueEventListener Trainers_listener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User trainer = dataSnapshot.child("profile").getValue(User.class);
                    dataMap.put(trainer.getUid(), trainer);
                }
                dataList.clear();
                dataList.addAll(dataMap.values());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        };
    }

    private Callback<User> ConversationCallback() {
        return new Callback<User>() {
            @Override
            public void result(User result) {
                Intent intent = new Intent(TrainersActivity.this, ChatActivity.class);
                Gson gson = new Gson();
                String json = gson.toJson(result);
                intent.putExtra("trainer", json);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
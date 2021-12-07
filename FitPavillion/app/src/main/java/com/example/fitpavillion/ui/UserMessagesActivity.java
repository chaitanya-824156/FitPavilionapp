package com.example.fitpavillion.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.R;
import com.example.fitpavillion.adapters.ConversationAdapter;
import com.example.fitpavillion.models.Conversation;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class UserMessagesActivity extends AppCompatActivity {

    private static final String TAG = "UserMessagesActivity";
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference ref = db.getReference("conversations");
    private FirebaseUser user;
    private FirebaseAuth auth;
    private ConversationAdapter adapter;
    private ArrayList<Conversation> dataList;
    private Map<String, Conversation> dataMap;
    private RecyclerView recyclerView;

    private Query query;
    private User profile;

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Messages");
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
        setContentView(R.layout.activity_user_messages);

        auth = LoginAuth.getInstance().getAuth();
        user = auth.getCurrentUser();
        SharedPref sharedPref = SharedPref.getInstance(this);
        profile = sharedPref.getUser();
        query = ref.orderByChild("USER").equalTo(profile.getUid());

        recyclerView = findViewById(R.id.trainer_conv_recycler);

        dataList = new ArrayList<>();
        dataMap = new HashMap<>();

        adapter = new ConversationAdapter(dataList, this);
        adapter.setClickListener(ConversationCallback());
        recyclerView.setAdapter(adapter);
        getConversationList();
    }

    private void getConversationList() {
        ref.orderByChild("user").equalTo(profile.getUid()).addValueEventListener(Conv_listener());
    }

    private ValueEventListener Conv_listener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Conversation value = dataSnapshot.getValue(Conversation.class);
                    dataMap.put(value.getId(), value);
                }
                dataList.clear();
                dataList.addAll(dataMap.values());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Collections.sort(dataList, Comparator.comparingLong(Conversation::getUpdated).reversed());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        };
    }

    private Callback<Conversation> ConversationCallback() {
        return new Callback<Conversation>() {
            @Override
            public void result(Conversation result) {
                Intent intent = new Intent(UserMessagesActivity.this, ChatActivity.class);
                Gson gson = new Gson();
                String json = gson.toJson(result);
                intent.putExtra("conversation", json);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        ref.removeEventListener(Conv_listener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getConversationList();
    }
}
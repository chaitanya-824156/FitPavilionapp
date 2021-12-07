package com.example.fitpavillion.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.R;
import com.example.fitpavillion.adapters.ChatsAdapter;
import com.example.fitpavillion.firebase.Api;
import com.example.fitpavillion.models.ChatItem;
import com.example.fitpavillion.models.Conversation;
import com.example.fitpavillion.models.FcmNotification;
import com.example.fitpavillion.models.NotificationData;
import com.example.fitpavillion.models.User;
import com.example.fitpavillion.utils.Callback;
import com.example.fitpavillion.utils.SharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "ChatActivity";
    private static final String KEY = "key=AAAAKZIr90U:APA91bGSMZcOtEHGDoG6Jmrh6qbzhBU9I8nnjibR3SkyXixl4ouYQAHb5QhIXIyw0zzm7yjoXOf1_tNrPVJvhTflkanMEo2grKvvIGCRXRalWsOVhTHsytI3oKPprre0N3tHtPM0Io_P";
    private User trainer;
    private Conversation conversation;
    private User profile;
    private SharedPref sharedPref;
    private String chat_id;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private Query query;
    private Query queryConv;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private ChatsAdapter adapter;
    private ArrayList<ChatItem> dataList;
    private Map<String, ChatItem> dataMap;
    private RecyclerView recyclerView;
    private AppCompatEditText input;
    private MaterialButton send;
    private DatabaseReference convUpdate = db.getReference().child("conversations");
    private DatabaseReference chatsUpdate = db.getReference().child("chats");
    private Api api;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        sharedPref = SharedPref.getInstance(this);
        profile = sharedPref.getUser();
        api = Api.getInstance();
        getExtraFromIntent();
        query = db.getReference().child("chats").child(chat_id).orderByChild("date");
        query.keepSynced(true);

        queryConv = db.getReference().child("conversations").orderByChild("chat_id").equalTo(chat_id);
        queryConv.keepSynced(true);

        recyclerView = findViewById(R.id.chats_list_recycler);
        input = findViewById(R.id.chat_input);
        send = findViewById(R.id.chat_send);
        send.setOnClickListener(this);

        dataList = new ArrayList<>();
        dataMap = new HashMap<>();

        adapter = new ChatsAdapter(dataList, this);
        adapter.setClickListener(ConversationCallback());
        recyclerView.setAdapter(adapter);
        getChatsList();
//        getConversationList();
    }

    private void getChatsList() {
        query.addValueEventListener(Chats_listener());
    }

    private ValueEventListener Chats_listener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatItem item = dataSnapshot.getValue(ChatItem.class);
                    dataMap.put(item.getId(), item);
                }
                dataList.clear();
                dataList.addAll(dataMap.values());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Collections.sort(dataList, Comparator.comparingLong(ChatItem::getDate));
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(dataList.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        };
    }

    private Callback<ChatItem> ConversationCallback() {
        return new Callback<ChatItem>() {
            @Override
            public void result(ChatItem result) {
                Log.d(TAG, "result: " + result);
            }
        };
    }

    private void getConversationList() {
        queryConv.addValueEventListener(Conv_listener());
    }

    private ValueEventListener Conv_listener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    conversation = dataSnapshot.getValue(Conversation.class);
                }
                sharedPref.setConv(conversation);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        queryConv.removeEventListener(Conv_listener());
        query.removeEventListener(Chats_listener());
        sharedPref.setConv(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getConversationList();
        getChatsList();
    }

    private void getExtraFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Conversation conversation1 = getConversation(intent.getStringExtra("conversation"));
            if (conversation1 != null) {
                chat_id = conversation1.getChat_id();
                Objects.requireNonNull(getSupportActionBar()).setTitle(profile.getProfileType().equals("USER") ? conversation.getTRAINER_NAME() : conversation.getUSER_NAME());
                updateTokens(profile.getProfileType().equals("USER") ? conversation.getTRAINER() : conversation.getUSER());
            }
            User trainer1 = getTrainer(intent.getStringExtra("trainer"));
            if (trainer1 != null) {
                Objects.requireNonNull(getSupportActionBar()).setTitle(trainer1.getName());
                String arr[] = {profile.getUid(), trainer1.getUid()};
                Arrays.sort(arr);
                StringBuilder sb = new StringBuilder("");
                for (String i : arr) {
                    sb.append(i);
                }
                chat_id = sb.toString();
                updateTokens(trainer1.getUid());
            }
        }
    }

    private void updateTokens(String id) {
        db.getReference("users").child(id).child("profile").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User otherUser = task.getResult().getValue(User.class);
                if (otherUser != null) {
                    if (conversation != null) {
                        if (otherUser.getProfileType().equals("USER"))
                            conversation.setUSER_TOKEN(otherUser.getFcmToken());
                        else conversation.setTRAINER_TOKEN(otherUser.getFcmToken());
                        db.getReference("conversations").child(conversation.getId()).setValue(conversation);
                        sharedPref.setConv(conversation);
                    } else {
                        db.getReference("conversations").orderByChild("chat_id").equalTo(chat_id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Conversation con = task.getResult().getValue(Conversation.class);
                                    if (con != null) {
                                        if (otherUser.getProfileType().equals("USER"))
                                            con.setUSER_TOKEN(otherUser.getFcmToken());
                                        else con.setTRAINER_TOKEN(otherUser.getFcmToken());
                                        db.getReference("conversations").child(con.getId()).setValue(con);
                                        sharedPref.setConv(conversation);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public User getTrainer(String json) {
        Gson gson = new Gson();
        trainer = gson.fromJson(json, User.class);
        return trainer;
    }

    public Conversation getConversation(String json) {
        Gson gson = new Gson();
        conversation = gson.fromJson(json, Conversation.class);
        return conversation;
    }

    private void sendMessage() {
        if (input.getText().toString().trim().equals("")) return;
        if (conversation == null) {
            Conversation c = new Conversation();
            String id = UUID.randomUUID().toString();
            c.setChat_id(chat_id);
            c.setId(id);
            c.setTRAINER_NAME(trainer.getName());
            c.setTRAINER(trainer.getUid());
            c.setUpdated(System.currentTimeMillis());
            c.setUSER(profile.getUid());
            c.setUSER_NAME(profile.getName());
            c.setUSER_TOKEN(profile.getFcmToken());
            c.setTRAINER_TOKEN(trainer.getFcmToken());
            convUpdate.child(id).setValue(c, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, DatabaseReference ref) {
                    if (error == null) {
                        conversation = c;
                        uploadMessage();
                    }
                }
            });
        } else {
            uploadMessage();
        }

    }

    private void uploadMessage() {
        String id = UUID.randomUUID().toString();
        ChatItem chatItem = new ChatItem();
        chatItem.setId(id);
        chatItem.setDate(System.currentTimeMillis());
        chatItem.setFrom(profile.getProfileType().equals("USER") ? conversation.getUSER() : conversation.getTRAINER());
        chatItem.setTo(!profile.getProfileType().equals("USER") ? conversation.getUSER() : conversation.getTRAINER());
        chatItem.setMessage(input.getText().toString().trim());

        chatsUpdate.child(conversation.getChat_id()).child(id).setValue(chatItem, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    convUpdate.child(conversation.getId()).child("updated").setValue(System.currentTimeMillis());
                    input.setText("");
                    input.clearFocus();
                    sendNotification(conversation, chatItem);
                    Log.e(TAG, "Message Sent Successfully " + ref.toString());
                    Toast.makeText(ChatActivity.this, "message sent Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to Send", error.toException());
                    Toast.makeText(ChatActivity.this, "Failed to send try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendNotification(Conversation conversation, ChatItem chatItem) {
        NotificationData data = new NotificationData();
        data.setBody(chatItem.getMessage());
        data.setTitle(profile.getProfileType().equals("USER") ? conversation.getUSER_NAME() : conversation.getTRAINER_NAME());
        data.setChat(getChatItemString(chatItem));

        FcmNotification fcmNotification = new FcmNotification();
        fcmNotification.setData(data);
        fcmNotification.setNotification(data);
        fcmNotification.setPriority("high");
        fcmNotification.setTo(profile.getProfileType().equals("USER") ? conversation.getTRAINER_TOKEN() : conversation.getUSER_TOKEN());

        api.sendFCMNotification(fcmNotification);
//        sendNotification(fcmNotification.getTo());
    }

    public ChatItem getChatItem(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ChatItem.class);
    }

    public String getChatItemString(ChatItem chatItem) {
        Gson gson = new Gson();
        return gson.toJson(chatItem);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void sendNotification(final String regToken) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json = new JSONObject();
                    JSONObject dataJson = new JSONObject();
                    dataJson.put("body", "Hi this is sent from device to device");
                    dataJson.put("title", "dummy title");
                    json.put("notification", dataJson);
                    json.put("to", regToken);
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization", KEY)
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                    Log.d(TAG, "doInBackground: " + finalResponse);
                } catch (Exception e) {
                    Log.d(TAG, "" + e);
                }
                return null;
            }
        }.execute();

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.chat_send) {
            sendMessage();
        } else hideKeyboard(this);
    }

}
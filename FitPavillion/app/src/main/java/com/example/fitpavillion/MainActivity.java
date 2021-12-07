package com.example.fitpavillion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitpavillion.constants.CONSTANTS;
import com.example.fitpavillion.models.User;
import com.example.fitpavillion.ui.AdminHomeActivity;
import com.example.fitpavillion.ui.HomeActivity;
import com.example.fitpavillion.ui.LoginActivity;
import com.example.fitpavillion.ui.ProfileActivity;
import com.example.fitpavillion.ui.TrainerHomeActivity;
import com.example.fitpavillion.ui.TrainerProfileActivity;
import com.example.fitpavillion.utils.LoginAuth;
import com.example.fitpavillion.utils.SharedPref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private String mCustomToken;
    private SharedPref sharedPref;
    private FirebaseAuth.AuthStateListener fireAuthListener;
    private User user;

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = new LoginAuth().getAuth();
        sharedPref = SharedPref.getInstance(this);
        fireAuthListener = authStateListener();
        createGeneralNotificationChannel();
    }

    private void createGeneralNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = CONSTANTS.DEFAULT_NOTIFICATION_CHANNEL_ID;
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    CONSTANTS.NOTIFICATION_CHANNEL_GENERAL, NotificationManager.IMPORTANCE_HIGH));
        }
    }

    private FirebaseAuth.AuthStateListener authStateListener() {
        return new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } else {
                    if (sharedPref.getProfileComplete()) {
                        user = sharedPref.getUser();
                        switch (user.getProfileType() != null ? user.getProfileType() : "") {
                            case "ADMIN":
                                startActivity(new Intent(MainActivity.this, AdminHomeActivity.class));
                                break;
                            case "USER":
                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                break;
                            case "TRAINER":
                                startActivity(new Intent(MainActivity.this, TrainerHomeActivity.class));
                                break;
                            default:
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                break;
                        }
                    } else {
                        Intent i;
                        String type = sharedPref.getProfileType();
                        if (type != null && type.equals("TRAINER")) {
                            i = new Intent(MainActivity.this, TrainerProfileActivity.class);
                        } else {
                            i = new Intent(MainActivity.this, ProfileActivity.class);
                        }
                        if (firebaseUser.getEmail() != null)
                            i.putExtra("email", firebaseUser.getEmail());
                        i.putExtra("profileType", type);

                        startActivity(i);
                    }
                }
                finish();
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(fireAuthListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (fireAuthListener != null) {
            mAuth.removeAuthStateListener(fireAuthListener);
        }
    }

    public void firebaseLogout(View view) {
        mAuth.signOut();
    }
}
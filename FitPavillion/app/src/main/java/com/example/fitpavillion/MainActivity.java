package com.example.fitpavillion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitpavillion.ui.HomeActivity;
import com.example.fitpavillion.ui.LoginActivity;
import com.example.fitpavillion.ui.ProfileActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = new LoginAuth().getAuth();
        sharedPref = SharedPref.getInstance(this);
        fireAuthListener = authStateListener();
    }

    private FirebaseAuth.AuthStateListener authStateListener() {
        return new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } else {
                    if (sharedPref.getProfileComplete()) {
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
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
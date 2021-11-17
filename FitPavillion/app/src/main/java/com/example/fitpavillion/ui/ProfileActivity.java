package com.example.fitpavillion.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.fitpavillion.R;
import com.example.fitpavillion.models.User;
import com.example.fitpavillion.utils.LoginAuth;
import com.example.fitpavillion.utils.SharedPref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private DatabaseReference dbRef;
    private AppCompatEditText name, email, phone, height, weight;
    private SharedPref sharedPref;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        sharedPref = SharedPref.getInstance(this);
        auth = LoginAuth.getInstance().getAuth();
        user = auth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("profile");


        name = findViewById(R.id.p_name);
        email = findViewById(R.id.p_email);
        phone = findViewById(R.id.p_phone);
        height = findViewById(R.id.p_height);
        weight = findViewById(R.id.p_weight);

        isUserExists();
    }

    private void isUserExists() {
        User profile = sharedPref.getUser();
        if (profile == null) return;
        name.setText(profile.getName());
        email.setText(profile.getEmail());
        phone.setText(profile.getPhone());
        height.setText((int) profile.getHeight());
        weight.setText((int) profile.getWeight());
    }

    private boolean validateFielda() {
        if (name.getText().toString().trim().equals("")) return false;
        if (email.getText().toString().trim().equals("")) return false;
        if (String.valueOf(phone.getText()).trim().equals("")) return false;
        if (String.valueOf(height.getText()).trim().equals("")) return false;
        if (String.valueOf(weight.getText()).trim().equals("")) return false;
        return true;
    }

    public void updateUser(View view) {
        if (!validateFielda()) {
            Toast.makeText(this, "Please provide valid inputs", Toast.LENGTH_SHORT).show();
            return;
        }
        User profile = new User();
        profile.setName(name.getText().toString().trim());
        profile.setEmail(email.getText().toString().trim());
        profile.setPhone(String.valueOf(phone.getText()));
        profile.setHeight(Double.parseDouble(height.getText().toString()));
        profile.setWeight(Double.parseDouble(weight.getText().toString()));

        dbRef.setValue(profile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    sharedPref.setUser(profile);
                    sharedPref.setProfileComplete(true);
                    Intent i = new Intent(ProfileActivity.this, HomeActivity.class);
                    startActivity(i);
                    finish();
                    Log.e(TAG, "User Profile Updated Successfully " + ref.toString());
                    Toast.makeText(ProfileActivity.this, "User Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to add", error.toException());
                    Toast.makeText(ProfileActivity.this, "Failed to Update User Profile try again!", Toast.LENGTH_SHORT).show();
                }

            }
        });

//        dbRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    User user1 = snapshot.getValue(User.class);
//                    user1.setUid(user.getUid());
//                    sharedPref.setUser(user1);
//                    sharedPref.setProfileComplete(true);
//                    Intent i = new Intent(ProfileActivity.this, HomeActivity.class);
//                    startActivity(i);
//                    finish();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.w(TAG, "updateUserProfile:failure", error.toException());
//            }
//        });


    }
}
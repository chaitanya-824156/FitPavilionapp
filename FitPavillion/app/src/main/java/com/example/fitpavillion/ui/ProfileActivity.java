package com.example.fitpavillion.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.example.fitpavillion.R;
import com.example.fitpavillion.models.User;
import com.example.fitpavillion.utils.LoginAuth;
import com.example.fitpavillion.utils.SharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    User profile;
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

        if (getIntent().getExtras() == null) isUserExists();
//        else
    }

    private void isUserExists() {
        profile = sharedPref.getUser();
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    profile = task.getResult().getValue(User.class);
                    updateFields(profile);
                }
            }
        });
        updateFields(profile);
    }

    private void updateFields(User profile) {
        if (profile == null) return;
        name.setText(profile.getName());
        email.setText(profile.getEmail());
        phone.setText(profile.getPhone());
        height.setText(String.valueOf(profile.getHeight()));
        weight.setText(String.valueOf(profile.getWeight()));
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

    }
}
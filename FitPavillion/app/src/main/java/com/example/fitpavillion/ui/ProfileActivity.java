package com.example.fitpavillion.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.example.fitpavillion.MainActivity;
import com.example.fitpavillion.R;
import com.example.fitpavillion.firebase.FCMUtils;
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
    private final FirebaseAuth auth = LoginAuth.getInstance().getAuth();
    private final FirebaseUser user = auth.getCurrentUser();
    private final String[] type = {"ADMIN", "USER", "TRAINER"};
    private User profile;
    private DatabaseReference dbRef;
    private AppCompatEditText name, email, phone;
    private RadioGroup genGroup;
    private SharedPref sharedPref;
    private String profileType;
    private ProgressDialog progressDialog;
    private boolean edit;
    private String fcmToken;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        progressDialog = new ProgressDialog(this);

        name = findViewById(R.id.p_name);
        email = findViewById(R.id.p_email);
        phone = findViewById(R.id.p_phone);
        genGroup = findViewById(R.id.p_rad_group);

    }

    @Override
    protected void onStart() {
        super.onStart();
        sharedPref = SharedPref.getInstance(this);
        profile = sharedPref.getUser();
        getExtraFromIntent();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(user != null ? user.getUid() : userId).child("profile");
        isUserExists();
    }

    private void getExtraFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            String eExtra = intent.getStringExtra("email");
            if (eExtra != null) email.setText(eExtra);
            profileType = intent.getStringExtra("profileType");
            edit = intent.getBooleanExtra("edit", false);
            if (profile != null) userId = profile.getUid();
            else if (user != null) userId = user.getUid();
            else userId = "dummyId";

            if (edit && getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Profile");
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void isUserExists() {
        progressDialog.setTitle("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    progressDialog.dismiss();
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    profile = task.getResult().getValue(User.class);
                    if (profile != null)
                        sharedPref.setProfileType(profile.getProfileType());
                    if (!edit) {
                        FCMUtils.updateToken(result -> {
                            if (result != null) {
                                if (profile != null)
                                    profile.setFcmToken(result);
                                uploadProfile(profile);
                                progressDialog.dismiss();
                            } else {
                                progressDialog.dismiss();
                                updateAndRedirect(profile);
                            }
                        });
                    } else {
                        progressDialog.dismiss();
//                        updateAndRedirect(profile);
                    }
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
        if (profile.getGender() != null && profile.getGender().equals("MALE"))
            genGroup.check(R.id.p_rad_type_male);
        else genGroup.check(R.id.p_rad_type_female);
        profileType = profile.getProfileType();
        fcmToken = profile.getFcmToken();
    }

    private boolean validateFields() {
        if (name.getText().toString().trim().equals("")) return false;
        if (email.getText().toString().trim().equals("")) return false;
        if (String.valueOf(phone.getText()).trim().equals("")) return false;
        int selectedId = genGroup.getCheckedRadioButtonId();
        if (selectedId == -1) return false;
        return true;
    }

    public void updateUser(View view) {
        if (!validateFields()) {
            Toast.makeText(this, "Please provide valid inputs", Toast.LENGTH_SHORT).show();
            return;
        }
        User profile = new User();
        profile.setUid(user.getUid());
        profile.setName(name.getText().toString().trim());
        profile.setEmail(email.getText().toString().trim());
        profile.setPhone(String.valueOf(phone.getText()));
        int id = genGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) findViewById(id);
        profile.setGender(radioButton.getText().toString());
        profile.setProfileType(profileType != null ? profileType : "USER");
        FCMUtils.updateToken(result -> {
            fcmToken = result;
            if (result != null) profile.setFcmToken(result);
            uploadProfile(profile);
        });
    }

    private void uploadProfile(User profile) {
        if (profile == null) return;
        dbRef.setValue(profile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    updateAndRedirect(profile);
                    Log.e(TAG, "User Profile Updated Successfully " + ref.toString());
                    Toast.makeText(ProfileActivity.this, "User Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to add", error.toException());
                    Toast.makeText(ProfileActivity.this, "Failed to Update User Profile try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateAndRedirect(User profile) {
        if (profile == null) return;
        sharedPref.setUser(profile);
        sharedPref.setProfileComplete(true);
        Intent i = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
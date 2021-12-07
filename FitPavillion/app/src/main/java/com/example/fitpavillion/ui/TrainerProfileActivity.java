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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TrainerProfileActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final String TAG = "TrainerProfileActivity";
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private User profile;
    private DatabaseReference dbRef;
    private MaterialButton btn;
    private AppCompatEditText name, email, phone, area, address, city, pincode;
    private RadioGroup genGroup;
    private SharedPref sharedPref;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String profileType;
    private String[] type = {"ADMIN", "USER", "TRAINER"};
    private ProgressDialog progressDialog;
    private boolean edit;
    private LatLng latLng;
    private MapView mapView;
    private GoogleMap mMap;
    private String fcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_profile);
        sharedPref = SharedPref.getInstance(this);
        auth = LoginAuth.getInstance().getAuth();
        user = auth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("profile");
        progressDialog = new ProgressDialog(this);

        name = findViewById(R.id.trainer_name);
        email = findViewById(R.id.trainer_email);
        phone = findViewById(R.id.trainer_phone);
        genGroup = findViewById(R.id.trainer_rad_group);
        area = findViewById(R.id.trainer_area);
        address = findViewById(R.id.trainer_address);
        city = findViewById(R.id.trainer_city);
        pincode = findViewById(R.id.trainer_pincode);
        btn = findViewById(R.id.trainer_btn_update);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.trainer_profile_map);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        isUserExists();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        getExtraFromIntent();
    }

    private void getExtraFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            String eExtra = intent.getStringExtra("email");
            if (eExtra != null) email.setText(eExtra);
            profileType = intent.getStringExtra("profileType");
            edit = intent.getBooleanExtra("edit", false);

            if (edit && getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Trainer Profile");
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

        profile = sharedPref.getUser();
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
        address.setText(profile.getAddress());
        area.setText(profile.getArea());
        city.setText(profile.getCity());
        pincode.setText(String.valueOf(profile.getPincode()));
        if (profile.getGender() != null && profile.getGender().equals("MALE"))
            genGroup.check(R.id.trainer_rad_type_male);
        else genGroup.check(R.id.trainer_rad_type_female);
        profileType = profile.getProfileType();
        fcmToken = profile.getFcmToken();
        if (mMap != null && profile.getLat() == 0 && profile.getLng() != 0) {
            latLng = new LatLng(profile.getLat(), profile.getLng());
            addMarker(latLng, profile.getName(), profile.getAddress());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
            mMap.animateCamera(cameraUpdate);
        }
    }

    private boolean validateFields() {
        if (name.getText().toString().trim().equals("")) return false;
        if (email.getText().toString().trim().equals("")) return false;
        if (String.valueOf(phone.getText()).trim().equals("")) return false;
        if (String.valueOf(pincode.getText()).trim().equals("")) return false;
        if (address.getText().toString().trim().equals("")) return false;
        if (area.getText().toString().trim().equals("")) return false;
        if (city.getText().toString().trim().equals("")) return false;
        int selectedId = genGroup.getCheckedRadioButtonId();
        if (selectedId == -1) return false;
        if (latLng == null || latLng.longitude == 0 || latLng.latitude == 0) return false;
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
        profile.setAddress(address.getText().toString().trim());
        profile.setCity(city.getText().toString().trim());
        profile.setArea(area.getText().toString().trim());
        profile.setPincode(Integer.parseInt(pincode.getText().toString().trim()));
        int id = genGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) findViewById(id);
        profile.setGender(radioButton.getText().toString());
        profile.setProfileType(profileType != null ? profileType : "ADMIN");
        profile.setLat(latLng.latitude);
        profile.setLng(latLng.longitude);
        FCMUtils.updateToken(result -> {
            fcmToken = result;
            if (result != null) profile.setFcmToken(result);
            uploadProfile(profile);
        });
    }

    private void uploadProfile(User profile) {
        dbRef.setValue(profile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    updateAndRedirect(profile);
                    Log.e(TAG, "Trainer Profile Updated Successfully " + ref.toString());
                    Toast.makeText(TrainerProfileActivity.this, "Trainer Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to add", error.toException());
                    Toast.makeText(TrainerProfileActivity.this, "Failed to Update Trainer Profile try again!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void updateAndRedirect(User profile) {
        if (profile == null) return;
        sharedPref.setUser(profile);
        sharedPref.setProfileComplete(true);
        Intent i = new Intent(TrainerProfileActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (profile != null && profile.getLat() != 0 && profile.getLng() != 0) {
            latLng = new LatLng(profile.getLat(), profile.getLng());
            addMarker(latLng, profile.getName(), profile.getAddress());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
            mMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void onMapClick(LatLng latlng) {
        this.latLng = latlng;
        mMap.clear();
        addMarker(latlng, null, null);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 14);
        mMap.animateCamera(cameraUpdate);
    }

    private void addMarker(LatLng latLng, @Nullable String title, @Nullable String description) {
        if (latLng != null) {
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .snippet(description)
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .anchor(0f, 0.5f)
                    .visible(true)
                    .draggable(false));
            m.showInfoWindow();

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}
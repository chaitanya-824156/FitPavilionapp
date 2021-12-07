package com.example.fitpavillion.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.R;
import com.example.fitpavillion.adapters.TrainersAdapter;
import com.example.fitpavillion.models.User;
import com.example.fitpavillion.utils.Callback;
import com.example.fitpavillion.utils.LoginAuth;
import com.example.fitpavillion.utils.SharedPref;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

public class TrainersActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = "TrainersActivity";
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private Query query;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private TrainersAdapter adapter;
    private ArrayList<User> dataList;
    private Map<String, User> dataMap;
    private RecyclerView recyclerView;
    private SharedPref sharedPref;
    private MapView mapView;
    private GoogleMap mMap;
    private boolean isMap;

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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainers);
        checkLocationPermission();
        auth = LoginAuth.getInstance().getAuth();
        user = auth.getCurrentUser();
        sharedPref = SharedPref.getInstance(this);
        User profile = sharedPref.getUser();

        query = db.getReference().child("users").orderByChild("profile/profileType").equalTo("TRAINER");
        query.keepSynced(true);

        recyclerView = findViewById(R.id.trainer_list_recycler);
        mapView = findViewById(R.id.map);

        dataList = new ArrayList<>();
        dataMap = new HashMap<>();

        adapter = new TrainersAdapter(dataList, this);
        adapter.setClickListener(ConversationCallback());
        recyclerView.setAdapter(adapter);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

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

                if (mapView != null) addMarkers(dataList);
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
                moveToChatPage(result);
            }
        };
    }

    private void moveToChatPage(User result) {
        Intent intent = new Intent(TrainersActivity.this, ChatActivity.class);
        Gson gson = new Gson();
        String json = gson.toJson(result);
        intent.putExtra("trainer", json);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission")
                        .setMessage("This app requires Location Permission.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(TrainersActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                                if (mMap != null) mMap.setMyLocationEnabled(true);

                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (mMap != null) mMap.setMyLocationEnabled(true);
                }

            }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        addMarkers(dataList);
    }

    private void addMarkers(ArrayList<User> trainers) {
        if (trainers == null) return;
        mMap.clear();
        for (User u : trainers) {
            if (u.getLat() == 0 || u.getLng() == 0) break;
            StringBuilder sb = new StringBuilder();
            sb.append(u.getAddress().concat(" "));
            sb.append(u.getCity().concat(" "));
            sb.append(u.getArea().concat(" "));
            sb.append(u.getPincode());
            sb.append("\n");
            sb.append("Gender : ".concat(u.getGender()));
            sb.append("\n");
            sb.append("contact : ".concat(u.getPhone()));

            StringBuilder b = new StringBuilder();
            b.append(u.getName());
            b.append(" - ");
            b.append(u.getCity().concat(" - "));
            b.append(u.getArea().concat(" "));

            addMarker(new LatLng(u.getLat(), u.getLng()), b.toString(), sb.toString(), u.getUid());
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        User trainer = dataMap.get(marker.getTag());
        if (trainer != null) moveToChatPage(trainer);
    }

    private void addMarker(LatLng latLng, @Nullable String title, @Nullable String description, String key) {
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
            m.setTag(key);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_trainer_menu, menu);
        menu.findItem(R.id.map).setVisible(!isMap);
        menu.findItem(R.id.list).setVisible(isMap);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map:
                recyclerView.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);
                isMap = !isMap;
                invalidateOptionsMenu();
                break;
            case R.id.list:
                recyclerView.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.GONE);
                isMap = !isMap;
                invalidateOptionsMenu();
                break;
            case android.R.id.home :
                onBackPressed();
                break;

        }
        return true;
    }

}
package com.example.fitpavillion.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitpavillion.MainActivity;
import com.example.fitpavillion.R;
import com.example.fitpavillion.utils.LoginAuth;
import com.example.fitpavillion.utils.SharedPref;
import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialButton btnBMR, btnWoPlans, btnTrainers, btnMessages, btnConsumption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnBMR = findViewById(R.id.btn_bmr);
        btnWoPlans = findViewById(R.id.btn_woplans);
        btnTrainers = findViewById(R.id.btn_trainer);
        btnMessages = findViewById(R.id.btn_messages);
        btnConsumption = findViewById(R.id.btn_consumption);


        btnBMR.setOnClickListener(this);
        btnWoPlans.setOnClickListener(this);
        btnTrainers.setOnClickListener(this);
        btnMessages.setOnClickListener(this);
        btnConsumption.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.user_logout:
                new LoginAuth().signOut(this);
                i = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            case R.id.user_profile_page:
                i = new Intent(HomeActivity.this, ProfileActivity.class);
                i.putExtra("edit", true);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            default:
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.btn_bmr:
                i = new Intent(HomeActivity.this, BMRActivity.class);
                startActivity(i);
                break;
            case R.id.btn_woplans:
                i = new Intent(HomeActivity.this, UserWorkoutPlansActivity.class);
                startActivity(i);
                break;
            case R.id.btn_trainer:
                i = new Intent(HomeActivity.this, TrainersActivity.class);
                startActivity(i);
                break;
            case R.id.btn_messages:
                i = new Intent(HomeActivity.this, UserMessagesActivity.class);
                startActivity(i);
                break;
            case R.id.btn_consumption:
                i = new Intent(HomeActivity.this, UserMessagesActivity.class);
                startActivity(i);
                break;
            default:
                break;
        }
    }
}
package com.example.fitpavillion.ui;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.example.fitpavillion.R;
import com.example.fitpavillion.models.FoodConsumption;
import com.example.fitpavillion.models.FoodItem;
import com.example.fitpavillion.models.User;
import com.example.fitpavillion.utils.SharedPref;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class FoodDairyFormActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "FoodDairyFormActivity";
    private final Calendar calendar = Calendar.getInstance();
    private final String myFormat = "yyyy-MM-dd hh:mm aaa";
    private final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
    private AppCompatEditText name, totalCal, qty, date, calorie;
    private Button btn_add, btn_calc;
    private FoodItem foodDetail;
    private ProgressDialog progressDialog;
    private User profile;

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Add Food Consumption");
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
        setContentView(R.layout.activity_food_dairy_form);
        profile = SharedPref.getInstance(this).getUser();

        name = findViewById(R.id.fc_name);
        totalCal = findViewById(R.id.fc_total_calorie);
        qty = findViewById(R.id.fc_quantity);
        date = findViewById(R.id.fc_date);
        calorie = findViewById(R.id.fc_calorie);
        btn_add = findViewById(R.id.fc_btn_add_update_food);
        btn_calc = findViewById(R.id.fc_btn_calc_cal);
        progressDialog = new ProgressDialog(this);

        calendar.setTime(new Date());
//        updateLabel();


        btn_add.setOnClickListener(this);
        btn_calc.setOnClickListener(this);
        date.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fc_btn_add_update_food:
                addOrModifyFoodDetails();
                break;
            case R.id.fc_btn_calc_cal:
                calculateTotalCalorie();
                break;
            case R.id.fc_date:
                openDateSelector();
                break;
            default:
                break;
        }
    }

    private boolean validateQty() {
        if (String.valueOf(qty.getText()).trim().equals("")) return false;
        if (Double.parseDouble(String.valueOf(qty.getText()).trim()) == 0) return false;
        if (String.valueOf(calorie.getText()).trim().equals("")) return false;
        if (Double.parseDouble(String.valueOf(calorie.getText()).trim()) == 0) return false;
        return true;
    }

    private void calculateTotalCalorie() {
        if (!validateQty()) {
            Toast.makeText(this, "Please provide valid inputs", Toast.LENGTH_SHORT).show();
            return;
        }
        double result = Double.parseDouble(qty.getText().toString()) * Double.parseDouble(calorie.getText().toString());
        totalCal.setText(String.valueOf(result));
    }

    private void openDateSelector() {
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        new DatePickerDialog(FoodDairyFormActivity.this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel() {
        date.setText(sdf.format(calendar.getTime()));
        Log.d(TAG, "updateLabel: " + calendar.getTimeInMillis());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                FoodItem item = getFoodItem(data.getStringExtra("item"));
                if (item != null) updateFields(item);
            }
        }

    }

    public FoodItem getFoodItem(String json) {
        Gson gson = new Gson();
        foodDetail = gson.fromJson(json, FoodItem.class);
        return foodDetail;
    }

    private void updateFields(FoodItem foodItem) {
        if (foodItem == null) return;
        name.setText(foodItem.getName());
        calorie.setText(String.valueOf(foodItem.getCalorie()));
    }

    private void addOrModifyFoodDetails() {
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Uploading data...");
        if (!validateFields()) {
            progressDialog.dismiss();
            Toast.makeText(this, "Please provide valid inputs", Toast.LENGTH_SHORT).show();
            return;
        }
        updateToFirebase();
    }


    private boolean validateFields() {
        if (name.getText().toString().trim().equals("")) return false;
        if (String.valueOf(totalCal.getText()).trim().equals("") || Double.parseDouble(String.valueOf(totalCal.getText()).trim()) == 0)
            return false;
        if (String.valueOf(date.getText()).trim().equals("")) {
            openDateSelector();
            return false;
        }
        calculateTotalCalorie();
        return true;
    }


    private void updateToFirebase() {
        FoodConsumption fc = new FoodConsumption();
        fc.setName(name.getText().toString().trim());
        fc.setQuantity(Double.parseDouble(qty.getText().toString()));
        fc.setCalorie(Double.parseDouble(calorie.getText().toString()));
        fc.setTotalCalorie(Double.parseDouble(totalCal.getText().toString()));
        fc.setDate(calendar.getTimeInMillis());
        fc.setUserId(profile.getUid());

        String uid;
        uid = UUID.randomUUID().toString();
        fc.setId(uid);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("daily_consumption").child(uid)
                .setValue(fc, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {
                                    Log.e(TAG, "uploaded Successfully! " + ref.toString());
                                    progressDialog.dismiss();
                                    Toast.makeText(FoodDairyFormActivity.this, "uploaded Successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Log.e(TAG, "Failed to add", error.toException());
                                    progressDialog.dismiss();
                                    Toast.makeText(FoodDairyFormActivity.this, "Failed to Upload, try again!", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.food_dairy_form_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.food_dairy_form) {
            Intent intent = new Intent(FoodDairyFormActivity.this, FoodSelectorActivity.class);
            startActivityForResult(intent, 101);
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }


}
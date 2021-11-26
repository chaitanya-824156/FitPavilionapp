package com.example.fitpavillion.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.example.fitpavillion.R;
import com.example.fitpavillion.models.FoodItem;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.UUID;

public class FoodFormActivity extends AppCompatActivity {
    private static final String TAG = "FoodFormActivity";
    private AppCompatEditText name, carb, protein, fat, calorie;
    private Button btn_add;
    private FoodItem foodDetail;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_form);

        name = findViewById(R.id.fd_name);
        carb = findViewById(R.id.fd_carb);
        protein = findViewById(R.id.fd_protien);
        fat = findViewById(R.id.fd_fat);
        calorie = findViewById(R.id.fd_calorie);
        btn_add = findViewById(R.id.fd_btn_add_update_food);
        progressDialog = new ProgressDialog(this);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOrModifyFoodDetails();
            }
        });

    }

    public FoodItem getFoodItem(String json) {
        Gson gson = new Gson();
        foodDetail = gson.fromJson(json, FoodItem.class);
        return foodDetail;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            FoodItem item = getFoodItem(intent.getStringExtra("item"));
            if (item != null) updateFields(item);
        }
    }

    private void updateFields(FoodItem foodItem) {
        if (foodItem == null) return;
        name.setText(foodItem.getName());
        carb.setText(String.valueOf(foodItem.getCarbohydrates()));
        protein.setText(String.valueOf(foodItem.getProtien()));
        fat.setText(String.valueOf(foodItem.getFat()));
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

    private void updateToFirebase() {
        if (foodDetail == null) foodDetail = new FoodItem();

        foodDetail.setName(name.getText().toString().trim());
        foodDetail.setCarbohydrates(Double.parseDouble(carb.getText().toString()));
        foodDetail.setProtien(Double.parseDouble(protein.getText().toString()));
        foodDetail.setFat(Double.parseDouble(fat.getText().toString()));
        foodDetail.setCalorie(Double.parseDouble(calorie.getText().toString()));

        String uid;
        if (foodDetail.getId() == null) {
            uid = UUID.randomUUID().toString();
            foodDetail.setId(uid);
        } else uid = foodDetail.getId();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("foodDetails").child(uid)
                .setValue(foodDetail, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {
                                    finish();
                                    Log.e(TAG, "Food detail uploaded Successfully! " + ref.toString());
                                    progressDialog.dismiss();
                                    Toast.makeText(FoodFormActivity.this, "Food detail uploaded Successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(TAG, "Failed to add", error.toException());
                                    progressDialog.dismiss();
                                    Toast.makeText(FoodFormActivity.this, "Failed to Upload Food detail, try again!", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                );
    }


    private boolean validateFields() {
        if (name.getText().toString().trim().equals("")) return false;
        if (String.valueOf(carb.getText()).trim().equals("")) return false;
        if (String.valueOf(protein.getText()).trim().equals("")) return false;
        if (String.valueOf(fat.getText()).trim().equals("")) return false;
        if (String.valueOf(calorie.getText()).trim().equals("")) return false;
        return true;
    }

}

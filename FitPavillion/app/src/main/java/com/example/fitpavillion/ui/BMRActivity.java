package com.example.fitpavillion.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.example.fitpavillion.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class BMRActivity extends AppCompatActivity implements View.OnClickListener {
    private String[] activityLevel;
    private double[] activityValues;
    private AppCompatEditText age, height, weight;
    private MaterialTextView bmr_result, activity_result;
    private MaterialButton btn_bmr, btn_cal;
    private FrameLayout activityFrame;
    private RadioGroup genGroup;
    private Spinner spinner;
    private double bmrPreviousResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmractivity);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("BMR Calculator");
        }

        age = findViewById(R.id.bmr_age);
        height = findViewById(R.id.bmr_height);
        weight = findViewById(R.id.bmr_weight);
        activityFrame = findViewById(R.id.bmr_drc_frame);
        spinner = findViewById(R.id.bmr_sp_activity);
        bmr_result = findViewById(R.id.bmr_result);
        activity_result = findViewById(R.id.bmr_cal_result);
        btn_bmr = findViewById(R.id.btn_bmr_calc);
        btn_cal = findViewById(R.id.btn_bmr_calc_cal);
        genGroup = findViewById(R.id.bmr_rad_group);

        btn_cal.setOnClickListener(this);
        btn_bmr.setOnClickListener(this);

        activityLevel = new String[]{"Sedentary", "Light Active", "Moderate", "Highly Active"};
        activityValues = new double[]{1.2, 1.375, 1.725, 1.9};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, activityLevel);
        spinner.setAdapter(adapter);
    }

    private boolean validateFields() {
        if (String.valueOf(height.getText()).trim().equals("")) return false;
        if (String.valueOf(weight.getText()).trim().equals("")) return false;
        if (String.valueOf(age.getText()).trim().equals("")) return false;
        int selectedId = genGroup.getCheckedRadioButtonId();
        if (selectedId == -1) return false;
        return true;
    }

    private boolean validateAddFields() {
        return (spinner.getSelectedItemPosition() != -1);
    }

    private void calcBMR() {
        if (!validateFields()) {
            Toast.makeText(this, "Please provide valid inputs", Toast.LENGTH_SHORT).show();
            return;
        }
        int id = genGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) findViewById(id);
        if (radioButton.getText().toString().equals("MALE")) {
            bmrPreviousResult = 88.362 + (13.397 * Double.parseDouble(weight.getText().toString())) + (4.799 * Double.parseDouble(height.getText().toString())) - (5.677 * Double.parseDouble(age.getText().toString()));
            bmr_result.setText(String.valueOf(bmrPreviousResult));
            activityFrame.setVisibility(View.VISIBLE);
        } else {
            bmrPreviousResult = 447.593 + (9.247 * Double.parseDouble(weight.getText().toString())) + (3.098 * Double.parseDouble(height.getText().toString())) - (4.330 * Double.parseDouble(age.getText().toString()));
            bmr_result.setText(String.valueOf(bmrPreviousResult));
            activityFrame.setVisibility(View.VISIBLE);
        }
    }

    private void calcCalorie() {
        if (!validateFields() && !validateAddFields()) {
            Toast.makeText(this, "Please provide valid inputs", Toast.LENGTH_SHORT).show();
            return;
        }
        calcBMR();
        double result = bmrPreviousResult * activityValues[spinner.getSelectedItemPosition()];
        activity_result.setText(String.valueOf(result));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bmr_calc:
                calcBMR();
                break;
            case R.id.btn_bmr_calc_cal:
                calcCalorie();
                break;
            default:
                break;
        }
    }
}
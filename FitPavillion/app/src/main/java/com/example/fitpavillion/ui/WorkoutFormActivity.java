package com.example.fitpavillion.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.fitpavillion.R;
import com.example.fitpavillion.constants.CONSTANTS;
import com.example.fitpavillion.models.WorkOutPlan;
import com.example.fitpavillion.utils.Callback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class WorkoutFormActivity extends AppCompatActivity {
    private static final String TAG = "WorkoutFormActivity";
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    boolean[] selectedDays;
    ArrayList<Integer> daysList = new ArrayList<>();
    String[] daysArray = {"Mon", "Tue", "Wed", "Thru", "Fri", "Sat", "Sun"};
    private AppCompatTextView days_tv;
    private AppCompatEditText name, reps, count, duration;
    private Button btn_add, btn_img;
    private ImageView imageView;
    private WorkOutPlan workOutPlan;
    private SwitchCompat toggle;
    private Uri fileUri;
    private boolean checked = false;
    private StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_form);
        toggle = findViewById(R.id.wo_active);
        days_tv = findViewById(R.id.wo_days);
        imageView = findViewById(R.id.wo_image_preview);
        name = findViewById(R.id.wo_name);
        reps = findViewById(R.id.wo_reps);
        count = findViewById(R.id.wo_count);
        duration = findViewById(R.id.wo_duration);
        btn_add = findViewById(R.id.wo_btn_add_update_workout);
        btn_img = findViewById(R.id.wo_btn_image_upload);
        selectedDays = new boolean[daysArray.length];

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checked = isChecked;
                } else {
                    checked = isChecked;
                }
            }
        });

        btn_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasStoragePermission(CONSTANTS.REQUEST_READ_PERMISSION);
            }
        });

        days_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMultiSelectDays();
            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOrModifyWorkoutPlans();
            }
        });


    }

    public WorkOutPlan getWorkoutPlan(String json) {
        Gson gson = new Gson();
        workOutPlan = gson.fromJson(json, WorkOutPlan.class);
        return workOutPlan;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            WorkOutPlan plan = getWorkoutPlan(intent.getStringExtra("item"));
            if (plan != null) updateFields(plan);
        }
    }

    private void updateFields(WorkOutPlan plan) {
        if (plan == null) return;
        name.setText(plan.getName());
        reps.setText(String.valueOf(plan.getReps()));
        count.setText(String.valueOf(plan.getCount()));
        duration.setText(String.valueOf(plan.getDurationInMins()));
        toggle.setChecked(plan.isActive());
        checked = plan.isActive();
        if (plan.getImageUrl() != null && plan.getImageUrl() != "") {
            Glide.with(this).load(plan.getImageUrl()).into(imageView)
                    .onLoadFailed(getApplicationContext().getResources().getDrawable(R.drawable.app_icon));
            imageView.setVisibility(View.VISIBLE);
        } else imageView.setVisibility(View.GONE);

        if (plan.getDays() != null && plan.getDays().size() > 0) {
            daysList = new ArrayList<>();
            selectedDays = new boolean[daysArray.length];
            HashSet<String> set = new HashSet<>(plan.getDays());
            for (int i = 0; i < daysArray.length; i++) {
                if (set.contains(daysArray[i])) {
                    daysList.add(i);
                    selectedDays[i] = true;
                }
            }
            onClickDaysTextView();
        }
    }

    // multi Select
    private void showMultiSelectDays() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select days");
        builder.setCancelable(false);
        builder.setMultiChoiceItems(daysArray, selectedDays, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                if (b) {
                    daysList.add(i);
                    Collections.sort(daysList);
                } else {
                    daysList.remove(i);
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onClickDaysTextView();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int j = 0; j < selectedDays.length; j++) {
                    selectedDays[j] = false;
                    daysList.clear();
                    days_tv.setText("");
                }
            }
        });
        builder.show();
    }

    private void hasStoragePermission(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
        } else selectImageFromGallery();

    }

    private void onClickDaysTextView() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < daysList.size(); j++) {
            stringBuilder.append(daysArray[daysList.get(j)]);
            if (j != daysList.size() - 1) stringBuilder.append(", ");
        }
        days_tv.setText(stringBuilder.toString());
    }


    private void selectImageFromGallery() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        startActivityForResult(chooserIntent, CONSTANTS.PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CONSTANTS.REQUEST_READ_PERMISSION) {
                Log.d(TAG, "onRequestPermissionsResult: PermissionGranted");
                selectImageFromGallery();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == CONSTANTS.PICK_IMAGE && data != null) {
            fileUri = data.getData();
            if (fileUri != null) {
                try {
                    Glide.with(getApplicationContext())
                            .asBitmap().load(fileUri)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    imageView.setImageBitmap(resource);
                                    imageView.setVisibility(View.VISIBLE);
                                    Bitmap bitmap = resource;
                                }
                            });
                } catch (Exception e) {
                    Log.e("TAG", "onActivityResult: ", e);
                    e.printStackTrace();
                }
            }
        }
    }

    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(WorkoutFormActivity.this.getContentResolver(), inImage, UUID.randomUUID().toString() + ".png", "drawing");
        return Uri.parse(path);
    }

    private void uploadImage(Callback<Uri> call) {
//        https://www.geeksforgeeks.org/android-how-to-upload-an-image-on-firebase-storage/
        if (fileUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference ref = storageRef.child("images/" + UUID.randomUUID().toString());
            ref.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(WorkoutFormActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d(TAG, "onSuccess: URI: " + uri);
                            call.result(uri);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    call.result(null);
                    Toast.makeText(WorkoutFormActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress
                            = (100.0
                            * taskSnapshot.getBytesTransferred()
                            / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                }
            });
        }
    }

    private void addOrModifyWorkoutPlans() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        if (!validateFields()) {
            Toast.makeText(this, "Please provide valid inputs", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fileUri != null) {
            if (workOutPlan != null && workOutPlan.getImageUrl() != null) {
                deleteFireStorageFile(workOutPlan.getImageUrl(), result -> {
                    if (result) {
                        uploadImage(result1 -> {
                            if (result1 != null) {
                                updateToFirebase(result1);
                            }
                        });
                    } else {
                        Toast.makeText(this, "Please Try Again", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                uploadImage(result1 -> {
                    if (result1 != null) {
                        updateToFirebase(result1);
                    }
                });
            }
        } else {
            updateToFirebase(null);
        }

    }

    private void updateToFirebase(Uri uri) {
        if (workOutPlan == null) workOutPlan = new WorkOutPlan();
        workOutPlan.setActive(checked);
        workOutPlan.setName(name.getText().toString().trim());
        workOutPlan.setCount(Integer.parseInt(count.getText().toString()));
        workOutPlan.setReps(Integer.parseInt(reps.getText().toString()));
        workOutPlan.setDurationInMins(Integer.parseInt(duration.getText().toString()));
        if (uri != null) workOutPlan.setImageUrl(uri.toString());
        List<String> list = new ArrayList<>();
        for (int i : daysList) list.add(daysArray[i]);
        workOutPlan.setDays(list);

        String uid;
        if (workOutPlan.getId() == null) {
            uid = UUID.randomUUID().toString();
            workOutPlan.setId(uid);
        } else uid = workOutPlan.getId();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("workoutPlans").child(uid)
                .setValue(workOutPlan, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {
                                    finish();
                                    Log.e(TAG, "Workout plan uploaded Successfully! " + ref.toString());
                                    Toast.makeText(WorkoutFormActivity.this, "Workout plan uploaded Successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(TAG, "Failed to add", error.toException());
                                    Toast.makeText(WorkoutFormActivity.this, "Failed to Upload Workout plan, try again!", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                );
    }

    private void deleteFireStorageFile(String imgUrl, Callback<Boolean> call) {
        StorageReference desertRef = FirebaseStorage.getInstance().getReferenceFromUrl(imgUrl);
        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                call.result(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                call.result(false);
            }
        });
    }

    private boolean validateFields() {
        if (name.getText().toString().trim().equals("")) return false;
        if (String.valueOf(reps.getText()).trim().equals("")) return false;
        if (String.valueOf(count.getText()).trim().equals("")) return false;
        if (String.valueOf(duration.getText()).trim().equals("")) return false;
        if (workOutPlan != null && (workOutPlan.getImageUrl() == null && fileUri == null))
            return false;
        if (daysList.size() == 0) return false;
        return true;
    }

}

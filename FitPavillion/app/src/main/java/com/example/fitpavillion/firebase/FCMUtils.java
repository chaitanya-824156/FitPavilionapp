package com.example.fitpavillion.firebase;

import android.util.Log;

import com.example.fitpavillion.utils.Callback;
import com.google.firebase.messaging.FirebaseMessaging;

public class FCMUtils {
    private static final String TAG = "FCMUtils";
    public static void updateToken(Callback<String> call) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                Log.d(TAG, "updateToken: token: "+token);
                call.result(token);
            } else {
                call.result(null);
            }
        });
    }
}

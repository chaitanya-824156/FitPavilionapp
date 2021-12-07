package com.example.fitpavillion.firebase;

import com.example.fitpavillion.models.ChatItem;
import com.example.fitpavillion.models.FcmNotification;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {

    private static final String KEY = "key=AAAAKZIr90U:APA91bGSMZcOtEHGDoG6Jmrh6qbzhBU9I8nnjibR3SkyXixl4ouYQAHb5QhIXIyw0zzm7yjoXOf1_tNrPVJvhTflkanMEo2grKvvIGCRXRalWsOVhTHsytI3oKPprre0N3tHtPM0Io_P";
    private static final String TYPE = "application/json";
    private static final String TAG = "Api";
    private static Api api;
    private final FCMApi fcmApi;

    private Api() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/fcm/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        fcmApi = retrofit.create(FCMApi.class);
    }

    public static Api getInstance() {
        if (api == null) {
            api = new Api();
        }
        return api;
    }

    public void sendFCMNotification(FcmNotification fcmNotification) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Call<ChatItem> res = fcmApi.sendNotification(KEY, TYPE, fcmNotification);
                    res.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}

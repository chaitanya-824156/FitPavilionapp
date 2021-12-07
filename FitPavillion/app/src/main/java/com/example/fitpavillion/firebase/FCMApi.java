package com.example.fitpavillion.firebase;

import com.example.fitpavillion.models.ChatItem;
import com.example.fitpavillion.models.FcmNotification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FCMApi {
    @POST("send")
    Call<ChatItem> sendNotification(@Header("Authorization") String key,
                                    @Header("Content-Type") String type,
                                    @Body FcmNotification fcmNotification);
}

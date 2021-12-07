package com.example.fitpavillion.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.fitpavillion.MainActivity;
import com.example.fitpavillion.R;
import com.example.fitpavillion.constants.CONSTANTS;
import com.example.fitpavillion.models.ChatItem;
import com.example.fitpavillion.models.Conversation;
import com.example.fitpavillion.models.User;
import com.example.fitpavillion.utils.SharedPref;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;
import java.util.UUID;

public class AppFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "AppFirebaseMessagingSer";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d(TAG, "onNewToken: token :" + s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message data payload: " + remoteMessage.getData());

        SharedPref sharedPref = SharedPref.getInstance(getApplicationContext());
        Conversation conv = sharedPref.getConvo();
        User user = sharedPref.getUser();
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            Log.d(TAG, "onMessageReceived: "+ data);
            ChatItem chat = getChatItem(data.get("chat"));
            remoteMessage.toIntent();
                    if (conv != null && user != null) {
                        if (!(user.getProfileType().equals("USER") ? conv.getTRAINER() : conv.getUSER()).equals(chat.getFrom()))
                            sendNotification(data.get("title"), data.get("body"), data.get("chat"));
                    } else sendNotification(data.get("title"), data.get("body"), data.get("chat"));
                }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }


    public ChatItem getChatItem(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ChatItem.class);
    }

    private void sendNotification(String title, String messageBody, String chat) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra("chat", chat);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String channelId = CONSTANTS.DEFAULT_NOTIFICATION_CHANNEL_ID;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.app_icon)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
        }
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(UUID.randomUUID().variant(), builder.build());
    }
}

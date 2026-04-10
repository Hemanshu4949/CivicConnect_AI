package com.example.civicconnectai.Services // Update to match your package!

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.civicconnectai.MainActivity
import com.example.civicconnectai.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // 1. This fires whenever a new token is generated for the device
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token Generated: $token")
        // Normally, you would save this new token to your database here
    }

    // 2. This fires when a notification arrives WHILE the app is OPEN on the screen
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Extract the title and body from the incoming message
        val title = remoteMessage.notification?.title ?: "New Notification"
        val body = remoteMessage.notification?.body ?: ""

        showNotification(title, body)
    }

    // 3. Helper function to actually draw the notification on the screen
    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "civic_connect_channel"

        // Android 8.0+ requires a Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "CivicConnect Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // What happens when they click the notification? Open MainActivity!
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the actual visual notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            // MAKE SURE TO CREATE a small icon in res/drawable! (e.g., ic_notification)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Show it!
        notificationManager.notify(0, notificationBuilder.build())
    }
}
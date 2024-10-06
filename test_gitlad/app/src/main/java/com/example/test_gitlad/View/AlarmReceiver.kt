package com.example.test_gitlad.View

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.test_gitlad.R
import java.text.SimpleDateFormat
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "No Title"
        val timeInMillis = intent.getLongExtra("time", 0L)
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(timeInMillis)

        // Create and display the notification
        sendNotification(context, title, time)
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification(context: Context, title: String, time: String) {
        val channelId = "alarm_channel_id"
        val notificationId = 1 // Unique ID for the notification

        // Create a notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Alarm Notifications"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Báo thức")
            .setContentText("Thông báo: $title at $time")
            .setSmallIcon(R.drawable.baseline_access_time_filled_24) // Replace with your own icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, notification)
    }
}
package com.example.test_gitlad.View

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.test_gitlad.Model.WidgetService
import com.example.test_gitlad.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            // Create an Intent to start the WidgetService
            val intent = Intent(context, WidgetService::class.java)
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            views.setRemoteAdapter(R.id.widget_list_view, intent)

            // Get the current date in English
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
            val currentDate = dateFormat.format(Calendar.getInstance().time)
            views.setTextViewText(R.id.widget_note_date, currentDate)

            val updateIntent = Intent(context, AppWidgetProvider::class.java).apply {
                action = "com.example.test_gitlad.ACTION_UPDATE_WIDGET"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                updateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            // Set the PendingIntent on the button in the widget layout
            views.setOnClickPendingIntent(R.id.widget_iconadd, pendingIntent)
            // Update the widget with the new RemoteViews
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            "com.example.test_gitlad.ACTION_UPDATE_WIDGET" -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, AppWidgetProvider::class.java)
                )
                // Notify the AppWidgetManager to update the data in the ListView
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view)
            }
        }
    }
}

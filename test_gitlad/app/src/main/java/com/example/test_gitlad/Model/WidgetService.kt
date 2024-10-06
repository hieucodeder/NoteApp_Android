package com.example.test_gitlad.Model

import android.content.Intent
import android.widget.RemoteViewsService
import com.example.test_gitlad.Adapter.NotesRemoteViewsFactory

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return NotesRemoteViewsFactory(applicationContext)
    }
}


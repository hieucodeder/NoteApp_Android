package com.example.test_gitlad.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.test_gitlad.Model.Note
import com.example.test_gitlad.Model.NotesRepository
import com.example.test_gitlad.R
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotesRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private val noteList = mutableListOf<Note>()
    private lateinit var notesRepository: NotesRepository
    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate() {
        notesRepository = NotesRepository(context)
    }

    override fun onDataSetChanged() {
        runBlocking {
            noteList.clear()
            noteList.addAll(fetchNotes())
        }
    }

    override fun onDestroy() {
        noteList.clear()
    }

    override fun getCount(): Int {
        return noteList.size
    }

    @SuppressLint("RemoteViewLayout")
    override fun getViewAt(position: Int): RemoteViews {

        if (position < 0 || position >= noteList.size) {
            return RemoteViews(context.packageName, R.layout.widget_empty_item)
        }

        val note = noteList[position]
        val views = RemoteViews(context.packageName, R.layout.item_widget)

        views.setTextViewText(R.id.dateTextwg, note.date)
        views.setTextViewText(R.id.tv_tieudewg, note.title)
        views.setTextViewText(R.id.tv_noidungwg, note.content)
        // Tạo Bitmap
        val color = note.color
        val width = 1
        val height = 10
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = color
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        views.setImageViewBitmap(R.id.colorStrip, bitmap)
        // Giờ
        note.alarmTime?.let {
            val calendar = Calendar.getInstance().apply { timeInMillis = it }
            val timeFormat = SimpleDateFormat("hh:mma", Locale.ENGLISH)
            views.setTextViewText(R.id.timeTextwg, timeFormat.format(calendar.time))
        } ?: run {
            views.setTextViewText(R.id.timeTextwg, "No Alarm Set")
        }

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = noteList[position].id.toLong()

    override fun hasStableIds(): Boolean = true

    private suspend fun fetchNotes(): List<Note> {
        return notesRepository.getNotesList()
    }
}

package com.example.test_gitlad.Model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.text.Html
import android.text.SpannableString
import com.example.test_gitlad.DataBase.DatabaseHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotesDAO(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val gson = Gson()

    fun insert(
        title: String,
        content: String,
        date: String,
        image_Uri: MutableList<String>,
        alarmTime: Long?,
        color: Int,
        pinned: Boolean,
        audioUri: String?,
        drawing: MutableList<String>
    ): Long {
        val db = dbHelper.writableDatabase

        val imageUrisJson = gson.toJson(image_Uri)
        val drawingUrisJson = gson.toJson(drawing)
        

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_TITLE, title)
            put(DatabaseHelper.COLUMN_CONTENT, content)
            put(DatabaseHelper.COLUMN_DATE, date)
            put(DatabaseHelper.COLUMN_IMAGE_URI, imageUrisJson)
            put(DatabaseHelper.COLUMN_ALARM_TIME, alarmTime)
            put(DatabaseHelper.COLUMN_COLOR, color)
            put(DatabaseHelper.COLUMN_PINNED, if (pinned) 1 else 0)
            put(DatabaseHelper.COLUMN_AUDIO_PATH, audioUri)
            put(DatabaseHelper.COLUMN_DRAWING, drawingUrisJson)
        }

        return db.insert(DatabaseHelper.TABLE_NAME, null, values)
    }


    fun update(
        id: Int,
        title: String,
        content: String,
        date: String,
        image_Uri: MutableList<String>,
        alarmTime: Long?,
        color: Int,
        pinned: Boolean,
        audioUri: String?,
        drawing: MutableList<String>
    ): Int {
        val db = dbHelper.writableDatabase

        val drawingUrisJson = gson.toJson(drawing)
        val imageUrisJson = gson.toJson(image_Uri)

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_TITLE, title)
            put(DatabaseHelper.COLUMN_CONTENT, content)
            put(DatabaseHelper.COLUMN_DATE, date)
            put(DatabaseHelper.COLUMN_IMAGE_URI, imageUrisJson)
            put(DatabaseHelper.COLUMN_ALARM_TIME, alarmTime)
            put(DatabaseHelper.COLUMN_COLOR, color)
            put(DatabaseHelper.COLUMN_PINNED, if (pinned) 1 else 0) // Thêm cột pinned
            put(DatabaseHelper.COLUMN_AUDIO_PATH, audioUri)
            put(DatabaseHelper.COLUMN_DRAWING, drawingUrisJson)
        }

        val selection = "${DatabaseHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        return db.update(DatabaseHelper.TABLE_NAME, values, selection, selectionArgs)
    }


    fun delete(id: Int): Int {
        val db = dbHelper.writableDatabase

        val selection = "${DatabaseHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        return db.delete(DatabaseHelper.TABLE_NAME, selection, selectionArgs)
    }

    fun getAll(): MutableList<Note> {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_TITLE,
            DatabaseHelper.COLUMN_CONTENT,
            DatabaseHelper.COLUMN_DATE,
            DatabaseHelper.COLUMN_IMAGE_URI,
            DatabaseHelper.COLUMN_ALARM_TIME,
            DatabaseHelper.COLUMN_COLOR,
            DatabaseHelper.COLUMN_PINNED,
            DatabaseHelper.COLUMN_AUDIO_PATH,
            DatabaseHelper.COLUMN_DRAWING
        )

        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_NAME, projection, null, null, null, null, null
        )

        val notes = mutableListOf<Note>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val title = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE))
                val contentHtml = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTENT))
                val date = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE))
                val imageUrisJson =
                    getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI))
                val alarmTime = getLong(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALARM_TIME))
                val color = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_COLOR))
                val pinned = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PINNED)) == 1
                val audioUri = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUDIO_PATH))
                val imageUrisType = object : TypeToken<MutableList<String>>() {}.type
                val imageUris: MutableList<String> = gson.fromJson(imageUrisJson, imageUrisType)
                val drawingUrisJson =
                    getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DRAWING))
                val drawingUrisType = object : TypeToken<MutableList<String>>() {}.type
                val drawingUris: MutableList<String> =
                    gson.fromJson(drawingUrisJson, drawingUrisType)
                // Convert contentHtml back to SpannableString
                val content: SpannableString =
                    SpannableString(Html.fromHtml(contentHtml, Html.FROM_HTML_MODE_LEGACY))

                notes.add(
                    Note(
                        id,
                        title,
                        content,
                        date,
                        imageUris,
                        alarmTime,
                        color,
                        pinned,
                        audioUri,
                        drawingUris
                    )
                )
            }
        }
        cursor.close()
        return notes
    }

    fun getNotesByColor(color: Int): MutableList<Note> {
        val db = dbHelper.readableDatabase

        // Define the projection to retrieve all columns
        val projection = arrayOf(
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_TITLE,
            DatabaseHelper.COLUMN_CONTENT,
            DatabaseHelper.COLUMN_DATE,
            DatabaseHelper.COLUMN_IMAGE_URI,
            DatabaseHelper.COLUMN_ALARM_TIME,
            DatabaseHelper.COLUMN_COLOR,
            DatabaseHelper.COLUMN_PINNED,
            DatabaseHelper.COLUMN_AUDIO_PATH,
            DatabaseHelper.COLUMN_DRAWING
        )

        // Define the selection criteria (the WHERE clause)
        val selection = "${DatabaseHelper.COLUMN_COLOR} = ?"
        val selectionArgs = arrayOf(color.toString())

        // Query the database
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, null
        )

        val notes = mutableListOf<Note>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val title = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE))
                val contentHtml = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTENT))
                val date = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE))
                val imageUrisJson = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI))
                val alarmTime = getLong(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALARM_TIME))
                val pinned = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PINNED)) == 1
                val audioUri = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUDIO_PATH))
                val imageUrisType = object : TypeToken<MutableList<String>>() {}.type
                val imageUris: MutableList<String> = gson.fromJson(imageUrisJson, imageUrisType)
                val drawingUrisJson = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DRAWING))
                val drawingUrisType = object : TypeToken<MutableList<String>>() {}.type
                val drawingUris: MutableList<String> = gson.fromJson(drawingUrisJson, drawingUrisType)
                // Convert contentHtml back to SpannableString
                val content: SpannableString = SpannableString(Html.fromHtml(contentHtml, Html.FROM_HTML_MODE_LEGACY))

                notes.add(
                    Note(
                        id,
                        title,
                        content,
                        date,
                        imageUris,
                        alarmTime,
                        color,
                        pinned,
                        audioUri,
                        drawingUris
                    )
                )
            }
        }
        cursor.close()
        return notes
    }


    suspend fun getNotesByDate(date: String): MutableList<Note> {
        return withContext(Dispatchers.IO) {
            val notes = mutableListOf<Note>()
            val db = dbHelper.readableDatabase
            var cursor: Cursor? = null

            try {
                val query =
                    "SELECT * FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_DATE} = ? ORDER BY ${DatabaseHelper.COLUMN_PINNED} DESC, ${DatabaseHelper.COLUMN_DATE} DESC"
                val args = arrayOf(date)
                cursor = db.rawQuery(query, args)

                if (cursor.moveToFirst()) {
                    do {
                        val id =
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                        val title =
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE))
                        val contentHtml =
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTENT))
                        val noteDate =
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE))
                        val imageUrisJson =
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI))
                        val alarmTime =
                            cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALARM_TIME))
                        val color =
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COLOR))
                        val pinned =
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PINNED)) == 1
                        val drawingUriJson =
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DRAWING))
                        val audioUri =
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUDIO_PATH))
                        val imageUris: MutableList<String> = gson.fromJson(
                            imageUrisJson, object : TypeToken<MutableList<String>>() {}.type
                        )
                        val drawingUris: MutableList<String> = gson.fromJson(
                            drawingUriJson, object : TypeToken<MutableList<String>>() {}.type
                        )
                        // Convert contentHtml back to SpannableString
                        val content: SpannableString =
                            SpannableString(Html.fromHtml(contentHtml, Html.FROM_HTML_MODE_LEGACY))

                        notes.add(
                            Note(
                                id,
                                title,
                                content,
                                noteDate,
                                imageUris,
                                alarmTime,
                                color,
                                pinned,
                                audioUri,
                                drawingUris

                            )
                        )
                    } while (cursor.moveToNext())
                }
            } catch (e: Exception) {
                e.printStackTrace() // Log or handle the error
            } finally {
                cursor?.close()
                db.close()
            }

            notes
        }
    }


    fun getNoteById(noteId: Int): Note? {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_TITLE,
            DatabaseHelper.COLUMN_CONTENT,
            DatabaseHelper.COLUMN_DATE,
            DatabaseHelper.COLUMN_IMAGE_URI,
            DatabaseHelper.COLUMN_ALARM_TIME,
            DatabaseHelper.COLUMN_COLOR,
            DatabaseHelper.COLUMN_PINNED,
            DatabaseHelper.COLUMN_AUDIO_PATH,
            DatabaseHelper.COLUMN_DRAWING
        )

        // Define 'where' part of query.
        val selection = "${DatabaseHelper.COLUMN_ID} = ?"
        // Specify arguments in placeholder order.
        val selectionArgs = arrayOf(noteId.toString())

        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, null
        )

        var note: Note? = null
        with(cursor) {
            if (moveToFirst()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val title = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE))
                val contentHtml = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTENT))
                val date = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE))
                val imageUrisJson =
                    getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI))
                val alarmTime = getLong(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALARM_TIME))
                val color = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_COLOR))
                val pinned = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PINNED)) == 1
                val audioUri = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUDIO_PATH))
                val drawingUriJson = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DRAWING))
                val drawingUrisType = object : TypeToken<MutableList<String>>() {}.type
                val drawingUris: MutableList<String> =
                    gson.fromJson(drawingUriJson, drawingUrisType)

                val imageUrisType = object : TypeToken<MutableList<String>>() {}.type
                val imageUris: MutableList<String> = gson.fromJson(imageUrisJson, imageUrisType)

                // Convert contentHtml back to SpannableString
                val content: SpannableString =
                    SpannableString(Html.fromHtml(contentHtml, Html.FROM_HTML_MODE_LEGACY))

                note = Note(
                    id,
                    title,
                    content,
                    date,
                    imageUris,
                    alarmTime,
                    color,
                    pinned,
                    audioUri,
                    drawingUris
                )
            }
        }
        cursor.close()
        return note
    }


    // Retrieves image URIs for a specific note ID
    fun getImageUris(noteId: Int): List<String>? {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(DatabaseHelper.COLUMN_IMAGE_URI)
        val selection = "${DatabaseHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(noteId.toString())

        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, null
        )

        var imageUris: List<String>? = null
        with(cursor) {
            if (moveToFirst()) {
                val imageUrisJson =
                    getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI))
                val imageUrisType = object : TypeToken<List<String>>() {}.type
                imageUris = gson.fromJson(imageUrisJson, imageUrisType)
            }
        }
        cursor.close()
        return imageUris
    }

    // Search for notes based on the query
    fun searchNotes(query: String): List<Note> {
        val db = dbHelper.readableDatabase

        // Define the projection to specify which columns to retrieve
        val projection = arrayOf(
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_TITLE,
            DatabaseHelper.COLUMN_CONTENT,
            DatabaseHelper.COLUMN_DATE,
            DatabaseHelper.COLUMN_IMAGE_URI,
            DatabaseHelper.COLUMN_ALARM_TIME,
            DatabaseHelper.COLUMN_COLOR,
            DatabaseHelper.COLUMN_PINNED,
            DatabaseHelper.COLUMN_AUDIO_PATH,
            DatabaseHelper.COLUMN_DRAWING
        )

        // Define the 'WHERE' part of the query and the arguments
        val selection =
            "(${DatabaseHelper.COLUMN_TITLE} LIKE ? OR ${DatabaseHelper.COLUMN_CONTENT} LIKE ?)"
        val selectionArgs = arrayOf("%$query%", "%$query%")

        // Perform the query
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, null
        )

        // Process the results
        val notes = mutableListOf<Note>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val title = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE))
                val contentHtml = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTENT))
                val date = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE))
                val imageUrisJson =
                    getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI))
                val alarmTime = getLong(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALARM_TIME))
                val color = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_COLOR))
                val pinned = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PINNED)) == 1
                val audioUri = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUDIO_PATH))
                val drawingUriJson = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DRAWING))
                val drawingUrisType = object : TypeToken<MutableList<String>>() {}.type
                val drawingUris: MutableList<String> =
                    gson.fromJson(drawingUriJson, drawingUrisType)
                val imageUrisType = object : TypeToken<MutableList<String>>() {}.type
                val imageUris: MutableList<String> = gson.fromJson(imageUrisJson, imageUrisType)

                // Convert contentHtml back to SpannableString
                val content: SpannableString =
                    SpannableString(Html.fromHtml(contentHtml, Html.FROM_HTML_MODE_LEGACY))

                notes.add(
                    Note(
                        id,
                        title,
                        content,
                        date,
                        imageUris,
                        alarmTime,
                        color,
                        pinned,
                        audioUri,
                        drawingUris
                    )
                )
            }
        }
        cursor.close()
        return notes
    }
}


data class Note(
    val id: Int,
    var title: String,
    var content: SpannableString,
    var date: String,
    var image_Uri: MutableList<String> = mutableListOf(),
    var alarmTime: Long? = null,
    var color: Int,
    var pinned: Boolean,
    var audioUri: String? = null,
    var drawing: MutableList<String> = mutableListOf()
)
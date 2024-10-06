package com.example.test_gitlad.Model

import android.content.Context
import android.content.Intent
import android.text.Html
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesRepository(private val context: Context) {
    private val notesDAO = NotesDAO(context)

    private val _allNotes = MutableLiveData<List<Note>>()
    val allNotes: LiveData<List<Note>> get() = _allNotes

    // Inserts a new note object into the database and updates the LiveData
    suspend fun insert(note: Note): Long = withContext(Dispatchers.IO) {
        try {
            // Convert SpannableString content to HTML
            val contentHtml = Html.toHtml(note.content, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)

            // Insert the note into the database
            val noteId = notesDAO.insert(
                note.title,
                contentHtml,
                note.date,
                note.image_Uri,
                note.alarmTime,
                note.color,
                note.pinned,
                note.audioUri,
                note.drawing
            )

            // Update LiveData and send update broadcast
            updateNotesLiveData()
            sendUpdateBroadcast()

            return@withContext noteId
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext -1
        }
    }


    suspend fun update(note: Note) = withContext(Dispatchers.IO) {
        try {
            // Convert SpannableString content to HTML
            val contentHtml = Html.toHtml(note.content, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)

            // Update the note in the database
            notesDAO.update(
                note.id,
                note.title,
                contentHtml,
                note.date,
                note.image_Uri,
                note.alarmTime,
                note.color,
                note.pinned,
                note.audioUri,
                note.drawing
            )

            // Update LiveData and send update broadcast
            updateNotesLiveData()
            sendUpdateBroadcast()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        try {
            notesDAO.delete(id)
            updateNotesLiveData()
            sendUpdateBroadcast()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getNoteById(id: Int): Note? = withContext(Dispatchers.IO) {
        try {
            return@withContext notesDAO.getNoteById(id)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    suspend fun getAllNotes() = withContext(Dispatchers.IO) {
        updateNotesLiveData()
    }

    // Hàm để lấy các ghi chú theo màu sắc
//    suspend fun getColorNotes(color: Int) = withContext(Dispatchers.IO) {
//        updateColorLiveData(color)
//    }
    suspend fun getColorNotes(color: Int): List<Note> {
        return withContext(Dispatchers.IO) {
            val notesByColor = notesDAO.getNotesByColor(color)
            updateColorLiveData(color)
            // Trả về danh sách ghi chú theo màu
            notesByColor
        }
    }

    suspend fun getNotesList(limit: Int = 3): List<Note> = withContext(Dispatchers.IO) {
        try {
            // Fetch all notes from DAO
            val notes = notesDAO.getAll()

            return@withContext notes.take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    // Retrieves the three most recent notes
    suspend fun getRecentNotes(): List<Note> = withContext(Dispatchers.IO) {
        try {
            return@withContext notesDAO.getAll()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    suspend fun getNotesByDate(date: String): List<Note> = withContext(Dispatchers.IO) {
        try {
            notesDAO.getNotesByDate(date)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getNotesByColor(color: Int): List<Note> = withContext(Dispatchers.IO) {
        try {
            notesDAO.getNotesByColor(color)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun updateNotesLiveData() = withContext(Dispatchers.IO) {
        _allNotes.postValue(notesDAO.getAll())
        val notes = notesDAO.getAll()
        // Sắp xếp ghi chú
        val sortedNotes =
            notes.sortedWith(compareByDescending<Note> { it.pinned }.thenByDescending { it.date })
        _allNotes.postValue(sortedNotes)
    }

    private suspend fun updateColorLiveData(color: Int? = null) = withContext(Dispatchers.IO) {
        // Lấy danh sách ghi chú từ cơ sở dữ liệu
        val notes = if (color != null) {
            notesDAO.getNotesByColor(color)
        } else {
            notesDAO.getAll()
        }

        val sortedNotes =
            notes.sortedWith(compareByDescending<Note> { it.pinned }.thenByDescending { it.date })

        // Cập nhật LiveData với danh sách đã lọc và sắp xếp
        _allNotes.postValue(sortedNotes)
    }


    // Update the image URIs for a specific note
    suspend fun updateNoteWithNewImageList(noteId: Int, newImageUris: List<String>) {
        updateNoteWithNewImageListInternal(noteId, newImageUris)
    }

    private suspend fun updateNoteWithNewImageListInternal(
        noteId: Int, newImageUris: List<String>
    ) {
        try {
            val note = getNoteById(noteId)
            note?.let {
                it.image_Uri = newImageUris.toMutableList()
                update(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Search for notes based on query
    fun searchNotes(query: String): LiveData<List<Note>> {
        val searchResults = MutableLiveData<List<Note>>()
        CoroutineScope(Dispatchers.IO).launch {
            val notes = notesDAO.searchNotes(query)
            val sortedNotes =
                notes.sortedWith(compareByDescending<Note> { it.pinned }.thenByDescending { it.date })
            searchResults.postValue(sortedNotes)
        }
        return searchResults
    }

    // Retrieves image URIs for a specific note ID
    fun getImageUris(noteId: Int): LiveData<List<String>> {
        return MutableLiveData<List<String>>().apply {
            value = notesDAO.getImageUris(noteId)
        }
    }


    // Sends a broadcast to update the widget
    private fun sendUpdateBroadcast() {
        val intent = Intent("com.example.test_gitlad.ACTION_UPDATE_WIDGET").apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
        Log.d("NotesRepository", "Broadcast sent to update widget")
    }
}

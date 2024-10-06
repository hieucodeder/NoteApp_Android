package com.example.test_gitlad.ViewModel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.test_gitlad.Model.Note
import com.example.test_gitlad.Model.NotesRepository
import com.example.test_gitlad.View.AlarmReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotesViewModel(application: Application, private val repository: NotesRepository) :
    AndroidViewModel(application) {

    private val _notesByDate = MutableLiveData<List<Note>>()
    val notesByDate: LiveData<List<Note>> get() = _notesByDate

    val allNotes: LiveData<List<Note>> = repository.allNotes

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> get() = _selectedDate

    private val _recentNotes = MutableLiveData<List<Note>>()
    val recentNotes: LiveData<List<Note>> get() = _recentNotes

    private val _currentNote = MutableLiveData<Note?>()
    val currentNote: LiveData<Note?> get() = _currentNote
    private val _colorNotes = MutableLiveData<List<Note>>()
    val colorNotes: LiveData<List<Note>> get() = _colorNotes


    init {
        val currentDate = Calendar.getInstance().time
        _selectedDate.value =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate)
    }

    fun setCurrentNote(note: Note) {
        _currentNote.value = note
        Log.d("NotesViewModel", "Setting Current Note: $note")
    }

    fun getCurrentNote(): Note? {
        val note = _currentNote.value
        Log.d("NotesViewModel", "Getting Current Note: $note")
        return note
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun insert(note: Note) {
        viewModelScope.launch {
            repository.insert(note)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch {
            repository.update(note)
        }
    }

    suspend fun getNoteById(id: Int): Note? {
        return repository.getNoteById(id)
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun fetchAllNotes() {
        viewModelScope.launch {
            repository.getAllNotes()
        }
    }
    // Gọi hàm để lấy các ghi chú theo màu
    fun fetchColorNotes(color: Int) {
        viewModelScope.launch {
            val notesByColor = withContext(Dispatchers.IO) {
                repository.getColorNotes(color)
            }
            _colorNotes.value = notesByColor
        }
    }
    fun getRecentNotes(onResult: (List<Note>) -> Unit) {
        viewModelScope.launch {
            val notes = withContext(Dispatchers.IO) {
                repository.getRecentNotes()
            }
            onResult(notes)
        }
    }

    fun fetchNotesByDate(date: String) {
        viewModelScope.launch {
            val notes = repository.getNotesByDate(date)
            _notesByDate.postValue(notes)
        }
    }

    fun fetchRecentNotes(limit: Int = 3) {
        viewModelScope.launch {
            val notes = repository.getNotesList(limit)
            _recentNotes.postValue(notes)
        }
    }

    fun getImageUris(noteId: Int): LiveData<List<String>> {
        return repository.getImageUris(noteId)
    }

    fun updateNoteImages(noteId: Int, imageUris: List<String>) {
        viewModelScope.launch {
            val note = repository.getNoteById(noteId)
            note?.let {
                it.image_Uri = ArrayList(imageUris.take(4)) // Keep a maximum of 4 images
                repository.update(it)
            }
        }
    }

    fun updateNoteWithNewImageList(noteId: Int, newImageUris: List<String>) {
        viewModelScope.launch {
            try {
                repository.updateNoteWithNewImageList(noteId, newImageUris)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun scheduleNoteReminder(noteId: Int, calendar: Calendar) {
        viewModelScope.launch {
            val note = getNoteById(noteId)
            note?.let {
                scheduleAlarm(it, calendar)
            }
        }
    }

    fun pinNote(note: Note) = viewModelScope.launch {
        note.pinned = true
        repository.update(note)
    }

    fun unpinNote(note: Note) = viewModelScope.launch {
        note.pinned = false
        repository.update(note)
    }

    fun searchNotes(query: String): LiveData<List<Note>> {
        return repository.searchNotes(query)
    }

    private fun scheduleAlarm(note: Note, calendar: Calendar) {
        val context = getApplication<Application>().applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", note.title)
            putExtra("time", calendar.timeInMillis)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    class NotesViewModelFactory(
        private val application: Application, private val repository: NotesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return NotesViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

package com.example.test_gitlad.View

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.test_gitlad.Adapter.NotesAdapter
import com.example.test_gitlad.Model.Note
import com.example.test_gitlad.Model.NotesRepository
import com.example.test_gitlad.R
import com.example.test_gitlad.ViewModel.NotesViewModel
import com.example.test_gitlad.databinding.ActivityCalendarBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var sharedPreferences: SharedPreferences

    private val notesViewModel: NotesViewModel by viewModels {
        NotesViewModel.NotesViewModelFactory(application, NotesRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("layout_prefs", Context.MODE_PRIVATE)

        setupRecyclerView()
        setupCalendarView()
        observeNotesByDate()

        fetchNotesForCurrentDate()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_calender -> true
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(mutableListOf(), notesViewModel)
        val layoutType = sharedPreferences.getString("layout_type", "two_columns") ?: "two_columns"
        val staggeredGridLayoutManager = when (layoutType) {
            "one_column" -> StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            else -> StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
        binding.recyclerViewNotes.apply {
            layoutManager = staggeredGridLayoutManager
            adapter = notesAdapter
            clearItemDecorations()
            addItemDecoration(SpaceItemDecoration(16)) // Ensure SpaceItemDecoration is implemented
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = formatDateString(year, month, dayOfMonth)
            notesViewModel.fetchNotesByDate(date)
        }
    }

    private fun observeNotesByDate() {
        notesViewModel.notesByDate.observe(this) { notes ->
            if (notes.isEmpty()) {
                binding.recyclerViewNotes.visibility = View.GONE
                binding.textViewNoNotes.visibility = View.VISIBLE
            } else {
                binding.recyclerViewNotes.visibility = View.VISIBLE
                binding.textViewNoNotes.visibility = View.GONE
                notesAdapter.refreshData(notes.toMutableList())
            }
        }
    }

    private fun fetchNotesForCurrentDate() {
        val currentDate = getCurrentDate()
        val formattedDate = formatDateString(
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )
        notesViewModel.fetchNotesByDate(formattedDate)
    }

    private fun getCurrentDate(): Calendar {
        return Calendar.getInstance()
    }

    private fun formatDateString(year: Int, month: Int, dayOfMonth: Int): String {
        val adjustedMonth = month + 1
        return String.format("%04d-%02d-%02d", year, adjustedMonth, dayOfMonth)
    }

    private fun startNoteEditActivity(note: Note) {
        val intent = Intent(this, UpdateActivity::class.java).apply {
            putExtra("note_id", note.id)
        }
        startActivityForResult(intent, EDIT_NOTE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK) {
            // Refresh note data after editing
            fetchNotesForCurrentDate()
        }
    }

    private fun RecyclerView.clearItemDecorations() {
        val itemDecorationCount = itemDecorationCount
        for (i in 0 until itemDecorationCount) {
            removeItemDecorationAt(0)
        }
    }

    companion object {
        private const val EDIT_NOTE_REQUEST = 1
    }
}

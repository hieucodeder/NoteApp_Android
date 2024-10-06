package com.example.test_gitlad

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.test_gitlad.Adapter.NotesAdapter
import com.example.test_gitlad.Model.NotesRepository
import com.example.test_gitlad.ViewModel.NotesViewModel
import com.example.test_gitlad.databinding.ActivitySearchNotesBinding

class SearchNotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchNotesBinding
    private lateinit var notesAdapter: NotesAdapter

    private val notesViewModel: NotesViewModel by viewModels {
        NotesViewModel.NotesViewModelFactory(application, NotesRepository(application))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


}

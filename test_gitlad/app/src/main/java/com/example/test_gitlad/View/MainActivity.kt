package com.example.test_gitlad.View

import android.Manifest
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.test_gitlad.Adapter.NotesAdapter
import com.example.test_gitlad.Model.NotesRepository
import com.example.test_gitlad.R
import com.example.test_gitlad.ViewModel.NotesViewModel
import com.example.test_gitlad.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val notesViewModel: NotesViewModel by viewModels {
        NotesViewModel.NotesViewModelFactory(application, NotesRepository(applicationContext))
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnTrangChu: Button
    private lateinit var btnCongViec: Button
    private lateinit var btnGiaDinh: Button
    private lateinit var btnBanbe: Button
    private val colorCongViec = Color.GREEN
    private val colorBanBe = Color.BLUE
    private val colorGiaDinh = Color.RED

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissions()
        //Lưu dữ liệu cục bộ đươn giản SharedPreferences
        sharedPreferences = getSharedPreferences("layout_prefs", Context.MODE_PRIVATE)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bottom navigation setup
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    if (this !is MainActivity) {
                        startActivity(Intent(this, MainActivity::class.java))
                        overridePendingTransition(
                            0, 0
                        )
                        finish()
                    }
                    true
                }

                R.id.navigation_calender -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                else -> false
            }
        }

        // Toolbar setup
        val toolbar: Toolbar = findViewById(R.id.toolbarmenu)
        setSupportActionBar(toolbar)

        // Load and apply the saved layout type
        val layoutType = sharedPreferences.getString("layout_type", "two_columns")
        if (layoutType == "one_column") {
            changeLayoutToOneColumn()
        } else {
            changeLayoutToTwoColumns()
        }
        // Khởi tạo adapter
        notesAdapter = NotesAdapter(mutableListOf(), notesViewModel)
        binding.recyclerView.adapter = notesAdapter
        val newNoteButton: View = findViewById(R.id.newNoteButton)
        newNoteButton.setOnClickListener {
            val intent = Intent(this, MainActivityAdd::class.java)
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
            val currentDate = dateFormat.format(Calendar.getInstance().time)
            intent.putExtra("date", currentDate)
            startActivityForResult(intent, UPDATE_NOTE_REQUEST)
        }
        // seach
        binding.ivSearchIcon.setOnClickListener {
            if (binding.edtSearch.visibility == View.GONE) {
                binding.edtSearch.visibility = View.VISIBLE
                binding.edtSearch.requestFocus()
                // chọn bàn phím luôn
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.edtSearch, InputMethodManager.SHOW_IMPLICIT)
            } else {
                binding.edtSearch.visibility = View.GONE
                binding.tvNoResults.visibility = View.GONE

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
            }
        }
        //set click
        btnTrangChu = findViewById(R.id.btnTrangChu)
        btnTrangChu.setOnClickListener {
            notesViewModel.allNotes.observe(this, Observer { notes ->
                notes?.let {
                    notesAdapter.refreshData(it.toMutableList())
                }
            })
            notesViewModel.fetchAllNotes()
        }
        btnCongViec = findViewById(R.id.btnCongViec)
        btnCongViec.setOnClickListener {
            notesViewModel.fetchColorNotes(colorCongViec)
            notesViewModel.colorNotes.observe(this, Observer { notes ->
                notes?.let {
                    notesAdapter.refreshData(it.toMutableList())
                }
            })
        }
        btnBanbe = findViewById(R.id.btnBanbe)
        btnBanbe.setOnClickListener {
            notesViewModel.fetchColorNotes(colorBanBe)
            notesViewModel.colorNotes.observe(this, Observer { notes ->
                notes?.let {
                    notesAdapter.refreshData(it.toMutableList())
                }
            })
        }
        btnGiaDinh = findViewById(R.id.btnGiaDinh)
        btnGiaDinh.setOnClickListener {
            notesViewModel.fetchColorNotes(colorGiaDinh)
            notesViewModel.colorNotes.observe(this, Observer { notes ->
                notes?.let {
                    notesAdapter.refreshData(it.toMutableList())
                }
            })
        }
        // Thiết lập phương thức tìm kiếm
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        notesViewModel.allNotes.observe(this, Observer { notes ->
            notes?.let {
                notesAdapter.refreshData(it.toMutableList())
            }
        })
        notesViewModel.fetchAllNotes()
        updateWidgetData()
    }

    private fun performSearch(query: String) {
        notesViewModel.searchNotes(query).observe(this, { notes ->
            if (notes.isNullOrEmpty()) {
                binding.tvNoResults.visibility = View.VISIBLE
                notesAdapter.refreshData(notes.toMutableList())
            } else {
                binding.tvNoResults.visibility = View.GONE
                notesAdapter.refreshData(notes.toMutableList())
            }
        })
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), UPDATE_NOTE_REQUEST
            )
        }
    }

    //Cấp quyền
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == UPDATE_NOTE_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_NOTE_REQUEST && resultCode == RESULT_OK) {
            notesViewModel.fetchRecentNotes(3)
            updateWidgetData()
        }
    }

    private fun updateWidgetData() {
        notesViewModel.getRecentNotes { notes ->
            notesAdapter.refreshData(notes.toMutableList())
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(this, com.example.test_gitlad.View.AppWidgetProvider::class.java)
            )
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_layout_change, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_one_column -> {
                changeLayoutToOneColumn()
                saveLayoutType("one_column")
                true
            }

            R.id.action_two_columns -> {
                changeLayoutToTwoColumns()
                saveLayoutType("two_columns")
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveLayoutType(layoutType: String) {
        val editor = sharedPreferences.edit()
        editor.putString("layout_type", layoutType)
        editor.apply() // Save changes
    }

    private fun changeLayoutToOneColumn() {
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = staggeredGridLayoutManager
        clearItemDecorations(binding.recyclerView)
        val spaceDecoration = SpaceItemDecoration(16)
        binding.recyclerView.addItemDecoration(spaceDecoration)
    }

    private fun changeLayoutToTwoColumns() {
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = staggeredGridLayoutManager
        clearItemDecorations(binding.recyclerView)
        val spaceDecoration = SpaceItemDecoration(16)
        binding.recyclerView.addItemDecoration(spaceDecoration)
    }

    private fun clearItemDecorations(recyclerView: RecyclerView) {
        val itemDecorationCount = recyclerView.itemDecorationCount
        for (i in 0 until itemDecorationCount) {
            recyclerView.removeItemDecorationAt(0)
        }
    }

    override fun onResume() {
        super.onResume()
        notesViewModel.fetchAllNotes()
    }

    companion object {
        const val UPDATE_NOTE_REQUEST = 1
    }
}

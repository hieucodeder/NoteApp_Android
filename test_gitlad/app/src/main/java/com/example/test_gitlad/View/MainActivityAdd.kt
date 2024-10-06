package com.example.test_gitlad.View

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.test_gitlad.Adapter.CustomSpinnerAdapter
import com.example.test_gitlad.Model.Note
import com.example.test_gitlad.Model.NotesRepository
import com.example.test_gitlad.R
import com.example.test_gitlad.ViewModel.NotesViewModel
import com.example.test_gitlad.databinding.ActivityAddBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivityAdd : AppCompatActivity() {
    private val viewModel: NotesViewModel by viewModels {
        NotesViewModel.NotesViewModelFactory(application, NotesRepository(applicationContext))
    }
    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: AppCompatEditText
    private lateinit var dateEditText: EditText
    private lateinit var buttonSave: ImageView
    private lateinit var buttonSangToi: ImageView
    private lateinit var imageView: ImageView
    private lateinit var binding: ActivityAddBinding
    private lateinit var imgBack: ImageView
    private lateinit var toolbar: LinearLayout
    private var selectedImageUris: MutableList<String> = mutableListOf()
    private var selectedColor: Int = Color.WHITE

    // Format buttons
    private lateinit var btnBold: ImageButton
    private lateinit var btnItalic: ImageButton
    private lateinit var btnUnderline: ImageButton
    private lateinit var btnBackgroundColor: ImageButton
    private lateinit var btnToggleGroup: ImageButton
    private lateinit var btnBullet: ImageButton
    private lateinit var record: ImageView
    private lateinit var formattingGroup: LinearLayout
    private var currentSpan: Any? = null
    private lateinit var mediaRecorder: MediaRecorder
    private var audioFilePath: String = ""
    private var noteId: Int = -1

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainAdd) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Toolbar setup
        val toolbarmenu: Toolbar = findViewById(R.id.toolbarmenu)
        setSupportActionBar(toolbarmenu)
        val checkboxPin: CheckBox = findViewById(R.id.checkboxPin)

        editTextTitle = binding.edtAddtieude
        editTextContent = binding.edtAddnoidung
        dateEditText = binding.edtDate
        buttonSave = binding.imgSave
        imageView = binding.imageView
        imgBack = binding.imgBack
        toolbar = findViewById(R.id.toolbar)
        btnBold = findViewById(R.id.btnBold)
        btnItalic = findViewById(R.id.btnItalic)
        btnUnderline = findViewById(R.id.btnUnderline)
        record = findViewById(R.id.btnMic)
        btnBullet = findViewById(R.id.btnBullet)
        btnBackgroundColor = findViewById(R.id.btnBackgroundColor)
        btnToggleGroup = findViewById(R.id.btnToggleGroup)
        formattingGroup = findViewById(R.id.formattingGroup)
        setupTextWatcher()
        setupButtons()
        setBackgroundColor(selectedColor, 15)
        setupColorSpinner(null)
        record.setOnClickListener {
            showRecordingDialog()
        }
        // Hiển thị thanh công cụ khi nhấn vào EditText
        editTextContent.setOnClickListener {
            toolbar.visibility = View.VISIBLE
        }

        editTextContent.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                toolbar.visibility = View.GONE
            }
        }
        btnBullet.setOnClickListener {
            insertBulletPoint(editTextContent)
        }
        buttonSangToi = binding.swSangToi
        var currentMode = AppCompatDelegate.getDefaultNightMode()
        buttonSangToi.setImageResource(if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) R.drawable.sun else R.drawable.moon)

        buttonSangToi.setOnClickListener {
            currentMode = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                AppCompatDelegate.MODE_NIGHT_NO
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                AppCompatDelegate.MODE_NIGHT_YES
            }
            buttonSangToi.setImageResource(if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) R.drawable.sun else R.drawable.moon)
        }

        viewModel.selectedDate.observe(this, { date ->
            dateEditText.setText(date)
        })

        imgBack.setOnClickListener {
            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }

        dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        imageView.setOnClickListener {
            openGallery()
        }


        buttonSave.setOnClickListener {
            // Convert EditText content to SpannableString
            val spannableContent = editTextContent.text as Spannable
            // Convert SpannableString to HTML
            val contentHtml =
                Html.toHtml(spannableContent, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
            val title = editTextTitle.text.toString().trim()
            val date = dateEditText.text.toString().trim()

            if (title.isNotEmpty() && contentHtml.isNotEmpty()) {
                // Create a new Note with the content as HTML
                val newNote = Note(
                    id = 0,
                    title = title,
                    content = SpannableString(
                        Html.fromHtml(
                            contentHtml, Html.FROM_HTML_MODE_LEGACY
                        )
                    ), // Convert back to SpannableString for Note
                    date = date,
                    image_Uri = selectedImageUris,
                    alarmTime = System.currentTimeMillis(),
                    color = selectedColor,
                    pinned = checkboxPin.isChecked,
                    audioUri = audioFilePath
                )

                // Insert the new note into the database
                lifecycleScope.launch {
                    viewModel.insert(newNote)
                }

                Toast.makeText(this, "Thêm note thành công!!!", Toast.LENGTH_SHORT).show()
                clearInputFields()

                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_read_mode -> {
                return true
            }

            R.id.action_set_reminder -> {
                return true
            }

            R.id.action_PDF -> {
                return true
            }

            R.id.action_pin_note -> {
                return true
            }

            R.id.action_view_details -> {
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun showRecordingDialog() {
        val dialog = AlertDialog.Builder(this).setTitle("Record Audio")
            .setMessage("Press start to begin recording.")
            .setPositiveButton("Start") { _, _ -> startRecording() }
            .setNegativeButton("Cancel", null).show()
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val audioFile = File(externalCacheDir, "audiorecord.3gp")
            audioFilePath = audioFile.absolutePath

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }

            Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Log.e("AudioRecord", "Failed to start recording: ${e.message}", e)
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder.apply {
                stop()
                reset()
                release()
            }
            Toast.makeText(this, "Recording saved at: $audioFilePath", Toast.LENGTH_SHORT).show()
            saveAudioToDatabase(audioFilePath)
        } catch (e: Exception) {
            Log.e("AudioRecord", "Failed to stop recording: ${e.message}", e)
            Toast.makeText(this, "Failed to stop recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAudioToDatabase(audioPath: String) {
        if (File(audioPath).exists()) {
            lifecycleScope.launch {
                val note = viewModel.getNoteById(noteId)
                note?.let {
                    it.audioUri = audioPath
                    viewModel.update(it)
                    Log.d("AudioRecord", "Audio path saved to database: $audioPath")
                }
            }
        } else {
            Log.e("AudioRecord", "Audio file does not exist at: $audioPath")
        }
    }

    private fun applyStyle(style: Int) {
        currentSpan = StyleSpan(style)
        applySpan(currentSpan)
    }

    // Apply underline style
    private fun applyUnderline() {
        currentSpan = UnderlineSpan()
        applySpan(currentSpan)
    }

    // Apply background color style
    private fun applyBackgroundColor(color: Int) {
        currentSpan = BackgroundColorSpan(color)
        applySpan(currentSpan)
    }


    // Apply the currentSpan to the selected text
    private fun applySpan(span: Any?) {
        if (span == null) return

        val spannable = editTextContent.text as Spannable
        val start = editTextContent.selectionStart
        val end = editTextContent.selectionEnd

        // Only apply the span if text is selected
        if (start != end && start >= 0 && end > start) {
            try {
                spannable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                editTextContent.setSelection(end)
            } catch (e: Exception) {
                Log.e("applySpan", "Error applying span", e)
            }
        } else {
            Toast.makeText(this, "Please select text to apply style.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleFormattingGroup() {
        if (formattingGroup.visibility == View.GONE) {
            formattingGroup.apply {
                visibility = View.VISIBLE
                animate().alpha(1.0f).duration = 200
            }
        } else {
            formattingGroup.animate().alpha(0.0f).setDuration(200).withEndAction {
                formattingGroup.visibility = View.GONE
            }
        }
    }

    // Handles text changes and applies the currentSpan to new text
    private fun onTextChange() {
        val spannable = editTextContent.text as Spannable
        val start = editTextContent.selectionStart
        val end = editTextContent.selectionEnd

        // Apply the current style to newly typed text
        if (currentSpan != null && start >= 0 && end > start) {
            try {
                spannable.setSpan(currentSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } catch (e: Exception) {
                Log.e("onTextChange", "Error applying span on text change", e)
            }
        }
    }

    // Initialize TextWatcher to detect text changes
    private fun setupTextWatcher() {
        editTextContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onTextChange()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun requestFocus() {
        editTextContent.requestFocus()
    }

    private fun setupButtons() {
        btnToggleGroup.setOnClickListener {
            requestFocus()
            toggleFormattingGroup()
        }
        btnBold.setOnClickListener {
            requestFocus()
            applyStyle(Typeface.BOLD)
        }
        btnItalic.setOnClickListener {
            requestFocus()
            applyStyle(Typeface.ITALIC)
        }
        btnUnderline.setOnClickListener {
            requestFocus()
            applyUnderline()
        }
        btnBackgroundColor.setOnClickListener {
            requestFocus()
            applyBackgroundColor(Color.YELLOW)
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        resultLauncher.launch(intent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                if (result.data!!.clipData != null) {
                    val count = result.data!!.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = result.data!!.clipData!!.getItemAt(i).uri.toString()
                        handleImageSelection(imageUri)
                    }
                } else if (result.data!!.data != null) {
                    val imageUri = result.data!!.data!!.toString()
                    handleImageSelection(imageUri)
                }

                Toast.makeText(
                    this@MainActivityAdd,
                    "Selected ${selectedImageUris.size} images",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun handleImageSelection(uri: String) {
        selectedImageUris.add(uri)
    }

    private fun setBackgroundColor(selectedColor: Int, alpha: Int) {
        val fadedColor = ColorUtils.setAlphaComponent(selectedColor, alpha)
        binding.mainAdd.setBackgroundColor(fadedColor)

        val toolbarAlphaColor = ColorUtils.setAlphaComponent(selectedColor, alpha)
        val toolbar: Toolbar = findViewById(R.id.toolbarmenu)
        toolbar.setBackgroundColor(toolbarAlphaColor)

        val transparentGray = ColorUtils.setAlphaComponent(Color.DKGRAY, 128) // 50% opacity
        binding.colorSpinner.setBackgroundColor(transparentGray)
    }

    private fun setupColorSpinner(defaultColor: Int?) {
        val colors = arrayOf(
            "Chưa chọn nhóm", "Gia đình", "Công việc", "Bạn bè"
        )
        val colorValues = intArrayOf(Color.WHITE, Color.RED, Color.GREEN, Color.BLUE)

        // Use the custom adapter
        val adapter = CustomSpinnerAdapter(this, colors)

        binding.colorSpinner.adapter = adapter

        val defaultColorPosition = defaultColor?.let { color ->
            colorValues.indexOf(color).takeIf { it >= 0 }
        } ?: 0

        binding.colorSpinner.setSelection(defaultColorPosition)
        selectedColor = colorValues[defaultColorPosition]
        setBackgroundColor(selectedColor, 15)

        binding.colorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                selectedColor = colorValues[position]
                setBackgroundColor(selectedColor, 15)  // Update background color on selection
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val selectedDate = dateFormat.format(selectedCalendar.time)
                viewModel.setSelectedDate(selectedDate)
            }, year, month, day
        )

        datePickerDialog.show()
    }

    // Hàm chèn dấu bullet point vào nội dung ghi chú
    private fun insertBulletPoint(editText: AppCompatEditText) {
        val bulletPoint = "\u2022 " // Dấu bullet point
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd

        // Lấy nội dung hiện tại
        val currentText = editText.text

        // Chèn bullet point vào vị trí con trỏ
        currentText?.replace(selectionStart, selectionEnd, bulletPoint)

        // Đặt lại con trỏ ngay sau dấu bullet point
        editText.setSelection(selectionStart + bulletPoint.length)
    }

    private fun clearInputFields() {
        editTextTitle.text.clear()
        editTextContent.text?.clear()
        dateEditText.text.clear()
        imageView.setImageDrawable(null)
        selectedImageUris.clear()
    }
}

package com.example.test_gitlad.View

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Images
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.test_gitlad.Adapter.CustomSpinnerAdapter
import com.example.test_gitlad.Adapter.DrawingUpdateAdapter
import com.example.test_gitlad.Adapter.DrawingsAdapter
import com.example.test_gitlad.Adapter.ImagesUpdateAdapter
import com.example.test_gitlad.Model.NotesRepository
import com.example.test_gitlad.R
import com.example.test_gitlad.ViewModel.NotesViewModel
import com.example.test_gitlad.databinding.ActivityUpdateBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.Calendar
import java.util.Stack

class UpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateBinding
    private val viewModel: NotesViewModel by viewModels {
        NotesViewModel.NotesViewModelFactory(application, NotesRepository(applicationContext))
    }
    private lateinit var calendar: Calendar
    private var noteId: Int = -1
    private lateinit var imagesUpdateAdapter: ImagesUpdateAdapter
    private lateinit var drawingUpdateAdapter: DrawingUpdateAdapter
    private lateinit var buttonSangToi: ImageView
    private var selectedColor: Int = Color.WHITE
    private var imageUris: MutableList<String> = mutableListOf()
    private var drawingUris: MutableList<String> = mutableListOf()
    private val REQUEST_CODE = 1002
    private val TAG = "UpdateActivity"

    // Format buttons
    private lateinit var toolbar: LinearLayout
    private lateinit var btnBold: ImageButton
    private lateinit var btnItalic: ImageButton
    private lateinit var btnUnderline: ImageButton
    private lateinit var btnBackgroundColor: ImageButton
    private var currentSpan: Any? = null
    private val undoStack = Stack<CharSequence>()// Stack ngăn xếp
    private val redoStack = Stack<CharSequence>()
    private lateinit var btnToggleGroup: ImageButton
    private lateinit var formattingGroup: LinearLayout
    private lateinit var btnMic: ImageButton
    private lateinit var btnDraw: ImageButton
    private lateinit var btnBullet: ImageButton
    private lateinit var mediaRecorder: MediaRecorder
    private var audioFilePath: String = ""
    private lateinit var frameLayout: FrameLayout
    private lateinit var frameLayoutDrawing: FrameLayout
    private var isReadMode = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainUpdate) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Toolbar setup
        val toolbarmenu: Toolbar = findViewById(R.id.toolbarmenu)
        setSupportActionBar(toolbarmenu)

        calendar = Calendar.getInstance()
        checkNotificationPermission()
        setupRecyclerView()
        setupRecyclerViewDrawing()
        buttonSangToi = binding.swSangToi
        toolbar = findViewById(R.id.toolbar)
        formattingGroup = findViewById(R.id.formattingGroup)
        btnBold = findViewById(R.id.btnBold)
        btnToggleGroup = findViewById(R.id.btnToggleGroup)
        btnItalic = findViewById(R.id.btnItalic)
        btnMic = findViewById(R.id.btnMic)
        btnBullet = findViewById(R.id.btnBullet)
        btnDraw = findViewById(R.id.btnDrawing)
        btnUnderline = findViewById(R.id.btnUnderline)
        btnBackgroundColor = findViewById(R.id.btnBackgroundColor)
        frameLayout = findViewById(R.id.famnelayout)
        frameLayoutDrawing = findViewById(R.id.famnelayoutdrawing)
        // Hiển thị thanh công cụ khi nhấn vào EditText
        binding.edtupAddnoidung.setOnClickListener {
            toolbar.visibility = View.VISIBLE
        }
        btnBullet.setOnClickListener {
            insertBulletPoint(binding.edtupAddnoidung)
        }
        // nút mic
        btnMic.setOnClickListener {
            showRecordingDialog()
        }

        btnDraw.setOnClickListener {
            startDrawingActivity()
        }
        // Ẩn thanh công cụ khi EditText mất focus
        binding.edtupAddnoidung.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                toolbar.visibility = View.GONE
            }
        }
        btnBackgroundColor.setOnClickListener { applyBackgroundColor(Color.YELLOW) }
        var currentMode = AppCompatDelegate.getDefaultNightMode()
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            buttonSangToi.setImageResource(R.drawable.sun)
        } else {
            buttonSangToi.setImageResource(R.drawable.moon)
        }

        buttonSangToi.setOnClickListener {
            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                currentMode = AppCompatDelegate.MODE_NIGHT_NO
                buttonSangToi.setImageResource(R.drawable.moon)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                currentMode = AppCompatDelegate.MODE_NIGHT_YES
                buttonSangToi.setImageResource(R.drawable.sun)
            }
        }

        noteId = intent.getIntExtra("note_id", -1)
        selectedColor = intent.getIntExtra("note_color", Color.WHITE)
        setBackgroundColor()
        imageUris = intent.getStringArrayListExtra("image_uri")?.toMutableList() ?: mutableListOf()
        drawingUris =
            intent.getStringArrayListExtra("drawingUris")?.toMutableList() ?: mutableListOf()
        if (noteId == -1) {
            finish()
            return
        }

        // Load each image URI from image_Uri list
        binding.colorSpinner.setBackgroundColor(selectedColor)
        loadNoteData()
        setClickListeners()
        requestStoragePermission()
        checkAndRequestPermissions()

    }

    private fun requestFocus() {
        binding.edtupAddnoidung.requestFocus()
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
        val btnUndo: ImageButton = findViewById(R.id.undoStack)
        val btnRedo: ImageButton = findViewById(R.id.redoStack)

        btnUndo.setOnClickListener { undo() }
        btnRedo.setOnClickListener { redo() }
    }


    private fun loadNoteData() {
        lifecycleScope.launch {
            val note = viewModel.getNoteById(noteId)
            note?.let {
                binding.apply {
                    edtupAddtieude.setText(it.title)
                    edtupAddnoidung.setText(it.content)
                    edtDate.setText(it.date)

                    it.image_Uri.forEach { uri ->
                        loadImageFromUri(uri)
                    }
                    val base64Image = intent.getStringExtra("drawingUris") ?: ""
                    if (base64Image.isNotEmpty()) {
                        loadBase64Image(base64Image)
                    }

                }
                selectedColor = it.color
                imageUris = it.image_Uri.toMutableList()
                updateLayoutVisibility()
                setupRecyclerView()
                drawingUris = it.drawing.toMutableList()
                updateLayoutVisibilityDrawing()
                setupRecyclerViewDrawing()
            }

        }
    }

    private fun setClickListeners() {
        binding.apply {
            imgBack.setOnClickListener {
                onBackPressed()
            }

            imgAddAnh.setOnClickListener {
                openGallery()
            }

            imgupSave.setOnClickListener {
                updateNote()

            }
            //gọi hàm styles
            setupButtons()
            setupTextWatcher()
            setupColorSpinner(selectedColor)
        }
    }

    private fun setBackgroundColor() {
        val fadedColor = ColorUtils.setAlphaComponent(selectedColor, 15)
        binding.mainUpdate.setBackgroundColor(fadedColor)
        val toolbarAlphaColor = ColorUtils.setAlphaComponent(selectedColor, 15)
        val toolbar: Toolbar = findViewById(R.id.toolbarmenu)
        toolbar.setBackgroundColor(toolbarAlphaColor)
        val transparentGray = ColorUtils.setAlphaComponent(Color.DKGRAY, 128)
        binding.colorSpinner.setBackgroundColor(transparentGray)
    }

    private fun setupColorSpinner(defaultColor: Int?) {
        val colors = arrayOf(
            "Chưa chọn nhóm",
            "Gia đình",
            "Công việc",
            "Bạn bè"
        )
        val colorValues = intArrayOf(Color.WHITE, Color.RED, Color.GREEN, Color.BLUE)

        val adapter =CustomSpinnerAdapter(this, colors)
        binding.colorSpinner.adapter = adapter

        // Determine the default position if a default color is provided, otherwise set no selection (-1)
        val defaultColorPosition = defaultColor?.let { color ->
            colorValues.indexOf(color).takeIf { it >= 0 }
        } ?: 0

        binding.colorSpinner.setSelection(defaultColorPosition)
        selectedColor = colorValues[defaultColorPosition]
        setBackgroundColor()

        binding.colorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                selectedColor = colorValues[position]
                setBackgroundColor()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_read_mode -> {
                toggleReadMode(item)
                return true
            }

            R.id.action_set_reminder -> {
                showDateTimePicker()
                return true
            }

            R.id.action_PDF -> {
                if (checkPermissions()) {
                    generatePdf()
                } else {
                    requestPermissions()
                }
                true
            }

            R.id.action_pin_note -> {
                // Xử lý khi chọn gim
                return true
            }

            R.id.action_view_details -> {
                showNoteDetailsDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE_WRITE_EXTERNAL_STORAGE
        )
    }

    private fun generatePdf() {
        val pdfGenerator = PdfGenerator()
        pdfGenerator.createPdf(
            this, "example.pdf", "Hello, this is a simple PDF document created in Android."
        )
        Toast.makeText(this, "PDF created successfully", Toast.LENGTH_SHORT).show()
    }

    private fun toggleReadMode(menuItem: MenuItem) {
        // Toggle the read mode state
        isReadMode = !isReadMode

        val visibility = if (isReadMode) View.GONE else View.VISIBLE

        findViewById<ImageButton>(R.id.undoStack).visibility = visibility
        findViewById<ImageButton>(R.id.redoStack).visibility = visibility
        findViewById<ImageButton>(R.id.imgupSave).visibility = visibility
        findViewById<ImageView>(R.id.img_addAnh).visibility = visibility
        findViewById<ImageButton>(R.id.sw_sang_toi).visibility = visibility

        if (isReadMode) {
            menuItem.title = "Chế độ chỉnh sửa"
        } else {
            menuItem.title = "Chế độ đọc"
        }
    }

    private fun showNoteDetailsDialog() {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_note_details, null)
        val dialog = AlertDialog.Builder(this).setTitle("Chi tiết ghi chú").setView(dialogView)
            .setPositiveButton("Đóng", null).create()

        // Find TextViews in the dialog layout
        val tvCreatedTime: TextView = dialogView.findViewById(R.id.tvCreatedTime)
        val tvLastModifiedTime: TextView = dialogView.findViewById(R.id.tvLastModifiedTime)
        val tvWordCount: TextView = dialogView.findViewById(R.id.tvWordCount)
        val tvCharacterCount: TextView = dialogView.findViewById(R.id.tvCharacterCount)

        // Retrieve data from the Intent
        val createdTime = intent.getStringExtra("EXTRA_CREATED_TIME") ?: "Không có"
        val lastModifiedTime = intent.getStringExtra("EXTRA_LAST_MODIFIED_TIME") ?: "Không có"
        val wordCount = intent.getIntExtra("EXTRA_WORD_COUNT", 0)
        val characterCount = intent.getIntExtra("EXTRA_CHARACTER_COUNT", 0)

        // Set values for the TextViews
        tvCreatedTime.text = "Thời gian tạo: $createdTime"
        tvLastModifiedTime.text = "Sửa đổi lần cuối: $lastModifiedTime"
        tvWordCount.text = "Số từ: $wordCount"
        tvCharacterCount.text = "Số ký tự: $characterCount"

        // Show the dialog
        dialog.show()
    }


    private fun setupRecyclerViewDrawing() {
        if (drawingUris.isNotEmpty()) {
            drawingUpdateAdapter = DrawingUpdateAdapter(this, drawingUris) { position ->
                val intent = Intent(this, DrawingViewerActivity::class.java).apply {
                    putStringArrayListExtra("drawingUris", ArrayList(drawingUris))
                    putExtra("position", position)
                }
                startActivity(intent)
            }

            binding.rvDrawingDetail.apply {
                layoutManager =
                    LinearLayoutManager(this@UpdateActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = drawingUpdateAdapter
            }
            binding.rvDrawingDetail.visibility = View.VISIBLE
        } else {
            binding.rvDrawingDetail.visibility = View.GONE
        }
    }

    //xử lý logic trên RecyclerView ảnh
    private fun setupRecyclerView() {
        if (imageUris.isEmpty()) {
            return
        }
        imagesUpdateAdapter = ImagesUpdateAdapter(this, imageUris, { position ->
            val intent = Intent(this, ImageViewerActivity::class.java).apply {
                putStringArrayListExtra("image_uris", ArrayList(imageUris))
                putExtra("position", position)
            }
            // xử lý trả intent về resul
            resultLauncher.launch(intent)
        }, { position ->
            showDeleteConfirmationDialog(position)
        })

        // Log RecyclerView setup
        binding.rvImagesDetail?.let { recyclerView ->
            recyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = imagesUpdateAdapter
        } ?: run {
            Log.e(TAG, "setupRecyclerView: RecyclerView is null")
        }
    }


    private fun updateNoteImages() {
        viewModel.updateNoteImages(noteId, imageUris)
        updateLayoutVisibility()
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        MaterialAlertDialogBuilder(this).setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa ảnh này?").setPositiveButton("Xóa") { _, _ ->
                imageUris.removeAt(position)
                imagesUpdateAdapter.notifyDataSetChanged()
                updateNoteImages()
            }.setNegativeButton("Huỷ bỏ") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun updateNote() {
        val newTitle = binding.edtupAddtieude.text.toString().trim()
        val spannableContent = binding.edtupAddnoidung.text as Spannable
        val newContentHtml = Html.toHtml(spannableContent, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
        val newDate = binding.edtDate.text.toString().trim()
        val alarmTime = calendar.timeInMillis
        val drawingBase64List = intent.getStringArrayListExtra("drawingUris") ?: arrayListOf()

        lifecycleScope.launch {
            val note = viewModel.getNoteById(noteId)
            note?.let {
                it.title = newTitle
                it.content = SpannableString(
                    Html.fromHtml(
                        newContentHtml, Html.FROM_HTML_MODE_LEGACY
                    )
                )
                it.date = newDate
                it.alarmTime = alarmTime
                it.color = selectedColor
                it.image_Uri = ArrayList(imageUris.take(10))
                it.audioUri = audioFilePath
                it.drawing = drawingBase64List
                viewModel.update(it)
                Toast.makeText(this@UpdateActivity, "Đã cập nhật", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    // Hàm chèn dấu bullet point vào nội dung ghi chú
    private fun insertBulletPoint(editText: AppCompatEditText) {
        val bulletPoint = "\u2022 "
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd

        val currentText = editText.text

        currentText?.replace(selectionStart, selectionEnd, bulletPoint)

        editText.setSelection(selectionStart + bulletPoint.length)
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


    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    //ảnh
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    // Use this launcher when starting ImageViewerActivity
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    // Kiểm tra số lượng ảnh đã chọn
                    if (imageUris.size < 10) {
                        // Thêm ảnh vào danh sách và cập nhật giao diện
                        val imageUriString = it.toString()
                        if (!imageUris.contains(imageUriString)) { // Tránh trùng lặp
                            loadImageFromUri(imageUriString)
                            binding.imageView.setTag(R.id.imageView, imageUriString)
                            imageUris.add(imageUriString)
                            setupRecyclerView()
                            updateLayoutVisibility()
                        } else {
                            Toast.makeText(
                                this, "Ảnh này đã được chọn", Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this, "Bạn chỉ có thể chọn tối đa 10 ảnh", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


    //vẽ dùng ActivityResultContracts mới
    private fun startDrawingActivity() {
        val intent = Intent(this, DrawingActivity::class.java).apply {
            putStringArrayListExtra("existingDrawings", ArrayList(drawingUris))
        }
        drawingResultLauncher.launch(intent)
    }

    private val drawingResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val newDrawings =
                    result.data?.getStringArrayListExtra("drawingUris")?.toMutableList()
                        ?: mutableListOf()

                if (newDrawings.isNotEmpty()) {
                    // Clear and update the drawingUris list
                    drawingUris.clear()
                    drawingUris.addAll(newDrawings)

                    // Update or setup RecyclerView adapter
                    val adapter = binding.rvDrawingDetail.adapter as? DrawingsAdapter
                    if (adapter == null) {
                        setupRecyclerViewDrawing()
                    } else {
                        adapter.updateDrawings(drawingUris)
                    }

                    // Update the intent if necessary
                    intent.putStringArrayListExtra("drawingUris", ArrayList(drawingUris))

                    // Update layout visibility if necessary
                    updateLayoutVisibilityDrawing()
                } else {
                    Toast.makeText(this, "No drawings found", Toast.LENGTH_SHORT).show()
                }
            }
        }


    private fun loadImageFromUri(imageUri: String) {
        try {
            val uri = Uri.parse(imageUri)
            Glide.with(this).load(uri).into(binding.imageView)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image from URI", e)
        }
    }

    private fun loadBase64Image(base64String: String) {
        try {
            val decodedBytes: ByteArray = Base64.decode(base64String, Base64.DEFAULT)

            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            binding.imageView.setImageBitmap(bitmap)

        } catch (e: Exception) {
            Log.e(TAG, "Error decoding Base64 image", e)
        }
    }


    private fun showDateTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            calendar.set(Calendar.SECOND, 0)
            //sử dụng hàm
            scheduleNoteReminder(calendar)
        }, hour, minute, true)

        timePickerDialog.show()
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
            )
        } else {
        }
    }

    // gọi hàm hẹn giờ cho từng item
    private fun scheduleNoteReminder(calendar: Calendar) {
        viewModel.scheduleNoteReminder(noteId, calendar)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Handle permission granted
                } else {
                    Toast.makeText(
                        this,
                        "Notification permission is required for reminders",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Handle permission granted
                } else {
                    Toast.makeText(
                        this, "Storage permission is required to access images", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_CODE
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE
                )
            }
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

    private fun applyStyle(style: Int) {
        applySpan(StyleSpan(style))
    }

    private fun applyUnderline() {
        applySpan(UnderlineSpan())
    }

    private fun applyBackgroundColor(color: Int) {
        applySpan(BackgroundColorSpan(color))
    }

    private fun applySpan(span: Any) {
        val spannable = binding.edtupAddnoidung.text as Spannable
        val start = binding.edtupAddnoidung.selectionStart
        val end = binding.edtupAddnoidung.selectionEnd

        if (start != end && start >= 0 && end > start) {
            try {
                spannable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                binding.edtupAddnoidung.setSelection(end)
            } catch (e: Exception) {
                Log.e(TAG, "Error applying span", e)
            }
        } else {
            Toast.makeText(this, "Please select text to apply style.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTextWatcher() {
        binding.edtupAddnoidung.addTextChangedListener(object : TextWatcher {
            private var isUndoRedo = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (!isUndoRedo) {
                    undoStack.push(s.toString())
                    redoStack.clear()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }


    private fun undo() {
        if (undoStack.isNotEmpty()) {
            val currentText = binding.edtupAddnoidung.text.toString()
            redoStack.push(currentText)

            val previousText = undoStack.pop()
            binding.edtupAddnoidung.setText(previousText)
            binding.edtupAddnoidung.setSelection(previousText.length)
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val currentText = binding.edtupAddnoidung.text.toString()
            undoStack.push(currentText)

            val nextText = redoStack.pop()
            binding.edtupAddnoidung.setText(nextText)
            binding.edtupAddnoidung.setSelection(nextText.length)
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(RESULT_OK, intent)
        super.onBackPressed()
    }

    private fun updateLayoutVisibility() {
        if (imageUris.isEmpty()) {
            frameLayout.visibility = View.GONE
        } else {
            frameLayout.visibility = View.VISIBLE
        }
    }

    private fun updateLayoutVisibilityDrawing() {
        if (imageUris.isEmpty()) {
            frameLayoutDrawing.visibility = View.GONE
        } else {
            frameLayoutDrawing.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
        private val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1
    }
}

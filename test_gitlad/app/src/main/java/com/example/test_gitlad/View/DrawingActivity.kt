package com.example.test_gitlad.View

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.example.test_gitlad.R
import com.example.test_gitlad.databinding.ActivityDrawingBinding
import java.io.ByteArrayOutputStream

class DrawingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDrawingBinding
    private val drawings = mutableListOf<Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDrawingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainDraw) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Truy xuất dữ liệu nếu có
        val existingDrawings = intent.getStringArrayListExtra("existingDrawings")
        existingDrawings?.let { base64Strings ->
            base64Strings.forEach { base64String ->
                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                bitmap?.let { drawings.add(it) }
            }
        }

        // Thiết lập SeekBar cho kích thước bút vẽ
        binding.seekBarBrushSize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvBrushSize.text = progress.toString()
                binding.drawingView.setBrushSize(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnfreely.setOnClickListener {
            binding.drawingView.setBrushShape(DrawingView.BrushShape.FREEHAND)
        }
        binding.btnRoundBrush.setOnClickListener {
            binding.drawingView.setBrushShape(DrawingView.BrushShape.ROUND)
        }

        binding.btnSquareBrush.setOnClickListener {
            binding.drawingView.setBrushShape(DrawingView.BrushShape.SQUARE)
        }

        binding.btnstraightBrush.setOnClickListener {
            binding.drawingView.setBrushShape(DrawingView.BrushShape.STRAIGHT)
        }
        binding.imgBack.setOnClickListener {
            finish()
        }
        // Nút xóa toàn bộ bản vẽ
        binding.btnClear.setOnClickListener {
            binding.drawingView.clear()
        }

        // Nút lưu bản vẽ
        binding.btnSave.setOnClickListener {
            val bitmap = binding.drawingView.save()
            bitmap?.let {
                drawings.add(it)
                binding.drawingView.clear()
            } ?: Log.e("DrawingActivity", "Failed to save drawing as bitmap.")
        }
        // Thiết lập màu bút khi viewBrushColor được nhấp
        binding.viewBrushColor.setOnClickListener {
            MaterialDialog(this).show {
                title(R.string.app_name)
                colorChooser(
                    colors = intArrayOf(
                        Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLACK
                    ),
                    initialSelection = (binding.viewBrushColor.background as ColorDrawable).color,
                    allowCustomArgb = true
                ) { dialog, color ->
                    binding.viewBrushColor.setBackgroundColor(color)
                    //lấy màu vào bút luôn
                    binding.drawingView.setBrushColor(color)
                }
                positiveButton(R.string.select)
                negativeButton(R.string.cancel)
            }
        }

        // Nút hoàn tất và trả về kết quả
        binding.btnDone.setOnClickListener {
            if (drawings.isNotEmpty()) {
                val base64Drawings = drawings.map { bitmap ->
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    Base64.encodeToString(byteArray, Base64.DEFAULT)
                }

                val intent = Intent().apply {
                    putStringArrayListExtra("drawingUris", ArrayList(base64Drawings))
                }
                setResult(RESULT_OK, intent)
            } else {
                Log.e("DrawingActivity", "No drawings to save.")
            }
            finish()
        }
    }

}


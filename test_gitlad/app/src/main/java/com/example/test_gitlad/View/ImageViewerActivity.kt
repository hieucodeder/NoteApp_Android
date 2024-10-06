package com.example.test_gitlad.View

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.test_gitlad.Adapter.ImagePagerAdapter
import com.example.test_gitlad.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ImageViewerActivity : AppCompatActivity() {

    private lateinit var imageUris: MutableList<String>
    private var currentPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        imageUris = intent.getStringArrayListExtra("image_uris")?.toMutableList() ?: mutableListOf()
        currentPosition = intent.getIntExtra("position", 0)

        val adapter = ImagePagerAdapter(this, imageUris)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(currentPosition, false)

        val imageCounter: TextView = findViewById(R.id.imageCounter)
        imageCounter.text = "${currentPosition + 1}/${imageUris.size}"

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
                updateImageCounter()
            }
        })

        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            // Prepare the intent to send back the remaining image URIs
            val resultIntent = Intent().apply {
                putStringArrayListExtra("image_uris", ArrayList(imageUris))
            }
            // Set the result to OK and pass the data back
            setResult(RESULT_OK, resultIntent)
            finish()
        }


        val deleteButton: ImageButton = findViewById(R.id.btn_delete)
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(currentPosition)
        }
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        MaterialAlertDialogBuilder(this).setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa ảnh này?").setPositiveButton("Xóa") { _, _ ->
                // Xóa ảnh và cập nhật dữ liệu
                imageUris.removeAt(position)
                if (imageUris.isEmpty()) {
                    // Nếu không còn ảnh, trả kết quả hủy
                    setResult(RESULT_CANCELED)
                    finish()
                } else {
                    // Cập nhật ViewPager và gửi dữ liệu mới
                    val adapter =
                        (findViewById<ViewPager2>(R.id.viewPager).adapter as? ImagePagerAdapter)
                    adapter?.notifyDataSetChanged()
                    updateImageCounter()

                    val intent = Intent().apply {
                        putStringArrayListExtra("image_uris", ArrayList(imageUris))
                    }
                    setResult(RESULT_OK, intent)

                    // Cập nhật vị trí hiện tại để ViewPager không bị lỗi khi xóa ảnh
                    if (currentPosition >= imageUris.size) {
                        currentPosition = imageUris.size - 1
                    }
                    findViewById<ViewPager2>(R.id.viewPager).setCurrentItem(currentPosition, false)
                }
            }.setNegativeButton("Huỷ bỏ") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun updateImageCounter() {
        val imageCounter: TextView = findViewById(R.id.imageCounter)
        imageCounter.text = "${currentPosition + 1}/${imageUris.size}"
    }
}

package com.example.test_gitlad.View

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.test_gitlad.Adapter.DrawingPagerAdapter
import com.example.test_gitlad.R

class DrawingViewerActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing_viewer)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val imageUris = intent.getStringArrayListExtra("drawingUris") ?: arrayListOf()
        val position = intent.getIntExtra("position", 0)

        val adapter = DrawingPagerAdapter(this, imageUris)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(position, false)

        val imageCounter: TextView = findViewById(R.id.imageCounter)
        imageCounter.text = "${position + 1}/${imageUris.size}"

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                imageCounter.text = "${position + 1}/${imageUris.size}"
            }
        })

        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            finish()
        }
    }
}
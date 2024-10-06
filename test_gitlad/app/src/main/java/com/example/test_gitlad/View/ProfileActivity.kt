package com.example.test_gitlad.View

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.test_gitlad.R
import com.example.test_gitlad.databinding.ActivityProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {
    private lateinit var bingding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bingding=ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bingding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //bottom navigation
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                        startActivity(Intent(this, MainActivity::class.java))
                        overridePendingTransition(0, 0) // Prevents animation for a smoother transition
                        finish() // Finishes the current activity to avoid multiple instances

                    true
                }
                R.id.navigation_calender -> {
                        startActivity(Intent(this, CalendarActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()

                    true
                }
                R.id.navigation_profile -> {
                    // Check if the current activity is not already ProfileActivity
                    if (this !is ProfileActivity) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                else -> false
            }
        }
    }
}
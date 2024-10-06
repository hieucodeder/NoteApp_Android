package com.example.test_gitlad.Adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test_gitlad.R

class DrawingPagerAdapter(private val context: Context, private var drawingBase64Strings: MutableList<String>) :
    RecyclerView.Adapter<DrawingPagerAdapter.DrawingViewHolder>() {

    inner class DrawingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewPager)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawingViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image_viewpager, parent, false)
        return DrawingViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrawingViewHolder, position: Int) {
        val base64String = drawingBase64Strings[position]
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

        Glide.with(context)
            .load(bitmap)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return drawingBase64Strings.size
    }

    fun updateDrawings(newDrawings: MutableList<String>) {
        this.drawingBase64Strings = newDrawings
        notifyDataSetChanged()
    }
}

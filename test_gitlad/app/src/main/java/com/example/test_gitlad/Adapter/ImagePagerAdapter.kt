package com.example.test_gitlad.Adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test_gitlad.R

class ImagePagerAdapter(private val context: Context, private var imageUris: MutableList<String>) :
    RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewPager)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image_viewpager, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = Uri.parse(imageUris[position])
        Log.d("ImagePagerAdapter", "Loading image for position: $position with URI: $imageUri")
        Glide.with(context)
            .load(imageUri)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return imageUris.size
    }
}


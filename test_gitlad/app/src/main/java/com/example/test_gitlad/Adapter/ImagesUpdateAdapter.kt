package com.example.test_gitlad.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test_gitlad.R

class ImagesUpdateAdapter(
    private val context: Context,
    private var imageUris: MutableList<String>,
    private val onImageClick: (Int) -> Unit,
    private val onImageLongClick: (Int) -> Unit
) : RecyclerView.Adapter<ImagesUpdateAdapter.ImageUpdateViewHolder>() {

    inner class ImageUpdateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewDetail)

        init {
            itemView.setOnClickListener {
                onImageClick(adapterPosition)
            }

            itemView.setOnLongClickListener {
                onImageLongClick(adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageUpdateViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image_update, parent, false)
        return ImageUpdateViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageUpdateViewHolder, position: Int) {
        Glide.with(context).load(imageUris[position]).into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return imageUris.size
    }

}

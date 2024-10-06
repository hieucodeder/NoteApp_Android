package com.example.test_gitlad.Adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.test_gitlad.R

class DrawingUpdateAdapter(
    private val context: Context,
    private var drawingUri: MutableList<String>,
    private val onImageClick: (Int) -> Unit,
) : RecyclerView.Adapter<DrawingUpdateAdapter.DrawingUpdateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawingUpdateViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_drawing_update, parent, false)
        return DrawingUpdateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrawingUpdateViewHolder, position: Int) {
        val base64String = drawingUri[position]
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

        holder.imageView.setImageBitmap(bitmap)
        holder.itemView.setOnClickListener {
            onImageClick(position)
        }
    }

    override fun getItemCount(): Int {
        return drawingUri.size
    }

    fun updateDrawings(newDrawings: MutableList<String>) {
        this.drawingUri = newDrawings
        notifyDataSetChanged()
    }

    class DrawingUpdateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.drawingViewDetail)
    }
}

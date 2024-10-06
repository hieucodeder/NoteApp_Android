package com.example.test_gitlad.Adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.test_gitlad.R

class DrawingsAdapter(
    private var drawings: MutableList<String>  // Changed to List for immutability
) : RecyclerView.Adapter<DrawingsAdapter.DrawingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawingViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_drawing, parent, false)
        return DrawingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DrawingViewHolder, position: Int) {
        val drawingBase64 = drawings[position]
        if (drawingBase64.isNotBlank()) {
            val bitmap = base64ToBitmap(drawingBase64)
            if (bitmap != null) {
                holder.drawingImageView.setImageBitmap(bitmap)
                holder.drawingImageView.visibility = View.VISIBLE
            } else {
                holder.drawingImageView.visibility = View.GONE
            }
        } else {
            holder.drawingImageView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = drawings.size

    fun updateDrawings(newDrawings: List<String>) {
        drawings.clear()
        drawings.addAll(newDrawings)
        notifyDataSetChanged()
    }

    class DrawingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val drawingImageView: ImageView = itemView.findViewById(R.id.imageViewDrawing)
    }

    private fun base64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }
}

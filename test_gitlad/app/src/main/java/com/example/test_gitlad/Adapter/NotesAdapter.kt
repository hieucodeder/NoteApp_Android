package com.example.test_gitlad.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_gitlad.Model.Note
import com.example.test_gitlad.R
import com.example.test_gitlad.View.ImageViewerActivity
import com.example.test_gitlad.View.UpdateActivity
import com.example.test_gitlad.ViewModel.NotesViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotesAdapter(
    private var notes: MutableList<Note>, private val viewModel: NotesViewModel
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitle: TextView = itemView.findViewById(R.id.tv_tieude)
        val textViewContent: TextView = itemView.findViewById(R.id.tv_noidung)
        val textViewDate: TextView = itemView.findViewById(R.id.dateText)
        val textViewTime: TextView = itemView.findViewById(R.id.tv_time)
        val recyclerViewImages: RecyclerView = itemView.findViewById(R.id.rv_images)
        val delete: ImageView = itemView.findViewById(R.id.imageView_delete)
        val record: ImageView = itemView.findViewById(R.id.imageView_recording)
        val recyclerViewDrawing: RecyclerView = itemView.findViewById(R.id.iv_drawing)
        val colorView: View = itemView.findViewById(R.id.it_colorView)
        val pin: ImageView = itemView.findViewById(R.id.imageView_pin)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]

        holder.textViewTitle.text = note.title
        holder.textViewContent.text = note.content
        holder.textViewDate.text = note.date
        // Handle drawing display
        if (note.drawing.isNullOrEmpty()) {
            holder.recyclerViewDrawing.visibility = View.GONE
        } else {
            holder.recyclerViewDrawing.visibility = View.VISIBLE

            if (holder.recyclerViewDrawing.adapter == null) {
                holder.recyclerViewDrawing.layoutManager =
                    LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
                holder.recyclerViewDrawing.adapter = DrawingsAdapter(note.drawing)
            } else {
                (holder.recyclerViewDrawing.adapter as DrawingsAdapter).updateDrawings(note.drawing)
            }
        }

        if (note.audioUri.isNullOrEmpty()) {
            holder.record.visibility = View.GONE
        } else {
            holder.record.visibility = View.VISIBLE

            // Set click listener to play the audio
            holder.record.setOnClickListener {
                note.audioUri?.let { path ->
                    playAudio(path)
                }
            }
        }


        // Set background color for the item
        val fadedColor = ColorUtils.setAlphaComponent(note.color, 15)
        holder.itemView.foreground = ColorDrawable(fadedColor)

        // Load time
        note.alarmTime?.let {
            val calendar = Calendar.getInstance().apply { timeInMillis = it }
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            holder.textViewTime.text = timeFormat.format(calendar.time)
        } ?: run {
            holder.textViewTime.text = "No Alarm Set"
        }

        // Set color for the color view
        note.color?.let {
            holder.colorView.setBackgroundColor(it)
        } ?: run {
            holder.colorView.setBackgroundColor(Color.RED)
        }
        // Set pin icon
        holder.pin.setImageResource(if (note.pinned) R.drawable.baseline_push_pin_24 else R.drawable.baseline_offline_pin_24)
        holder.pin.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, it)
            popup.inflate(R.menu.note_menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_pin -> {
                        viewModel.pinNote(note)
                        true
                    }

                    R.id.action_unpin -> {
                        viewModel.unpinNote(note)
                        true
                    }

                    else -> false
                }
            }
            popup.show()
        }
        // Đảm bảo rằng RecyclerView được hiển thị hoặc ẩn dựa trên số lượng hình ảnh
        if (note.image_Uri.isEmpty()) {
            holder.recyclerViewImages.visibility = View.GONE
        } else {
            holder.recyclerViewImages.visibility = View.VISIBLE
            holder.recyclerViewImages.layoutManager =
                LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)

            holder.recyclerViewImages.adapter = ImagesAdapter(holder.itemView.context,
                note.image_Uri.toMutableList(),
                { imagePosition ->
                    val intent =
                        Intent(holder.itemView.context, ImageViewerActivity::class.java).apply {
                            putStringArrayListExtra("image_uris", ArrayList(note.image_Uri))
                            putExtra("position", imagePosition)
                        }
                    holder.itemView.context.startActivity(intent)
                },
                { imagePosition ->
                    showDeleteConfirmationDialog(holder.itemView.context) { confirmed ->
                        if (confirmed) {
                            val imageUri = note.image_Uri[imagePosition]
                            note.image_Uri.removeAt(imagePosition)
                            holder.recyclerViewImages.adapter?.notifyItemRemoved(imagePosition)

                            viewModel.updateNoteWithNewImageList(note.id, note.image_Uri)

                            if (note.image_Uri.isEmpty()) {
                                holder.recyclerViewImages.visibility = View.GONE
                            }
                        }
                    }
                })
        }
        // Handle item click
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateActivity::class.java).apply {
                putExtra("note_id", note.id)
                putExtra("note_color", note.color)
                putStringArrayListExtra("image_uris", ArrayList(note.image_Uri))
                putStringArrayListExtra("drawingUris", ArrayList(note.drawing))
            }
            holder.itemView.context.startActivity(intent)
        }

        // Handle delete click
        holder.delete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context).setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa ghi chú này không?")
                .setPositiveButton("Xóa") { dialog, _ ->
                    val id = note.id
                    removeItem(position)
                    viewModel.delete(id)

                    Snackbar.make(holder.itemView, "Đã xóa ghi chú", Snackbar.LENGTH_LONG)
                        .setAction("Hoàn tác") {
                            viewModel.insert(note)
                            notes.add(position, note)
                            notifyItemInserted(position)
                        }.show()

                    dialog.dismiss()
                }.setNegativeButton("Hủy bỏ") { dialog, _ ->
                    notifyItemChanged(position)
                    dialog.dismiss()
                }.show()
        }
    }

    private fun showDeleteConfirmationDialog(context: Context, callback: (Boolean) -> Unit) {
        MaterialAlertDialogBuilder(context).setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa ảnh này không?").setPositiveButton("Xóa") { _, _ ->
                callback(true) // Confirmed
            }.setNegativeButton("Hủy bỏ") { dialog, _ ->
                dialog.dismiss()
                callback(false) // Canceled
            }.show()
    }

    private fun base64ToBitmap(base64List: MutableList<String>): List<Bitmap?> {
        return base64List.map { base64Str ->
            try {
                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: IllegalArgumentException) {
                // Handle the case where the Base64 string is not valid
                e.printStackTrace()
                null
            }
        }
    }


    private fun playAudio(audioFilePath: String) {
        val mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFilePath)
            prepare()
            start()
        }

        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
        }
    }

    fun removeItem(position: Int) {
        notes.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int = notes.size

    fun getItem(position: Int): Note = notes[position]

    fun refreshData(newNotes: MutableList<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }
}

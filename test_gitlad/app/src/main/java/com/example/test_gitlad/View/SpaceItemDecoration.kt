package com.example.test_gitlad.View

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = space
        outRect.right = space
        outRect.bottom = space

        // Thêm khoảng cách ở trên cùng của RecyclerView
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = space
        }
    }
}

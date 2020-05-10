package com.kastolars.expirationreminderproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

// TODO: Implement swipe left
class SwipeItemCallback(
    private val mDatabaseHelper: DatabaseHelper,
    private val mItems: ArrayList<Item>,
    private val mAdapter: ItemListAdapter
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val item = mItems.removeAt(viewHolder.adapterPosition)
        mDatabaseHelper.deleteItem(item)
        mAdapter.notifyDataSetChanged()
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val view = viewHolder.itemView
            val p = Paint()
            p.color = Color.RED
            if (dX > 0) {
                c.drawRect(
                    view.left.toFloat(), view.top.toFloat(), dX,
                    view.bottom.toFloat(), p
                )
            } else {
                c.drawRect(
                    view.right + dX, view.top.toFloat(),
                    view.right.toFloat(), view.bottom.toFloat(), p
                )
            }
        }
        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        );
    }
}
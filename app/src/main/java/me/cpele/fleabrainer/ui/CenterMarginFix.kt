package me.cpele.fleabrainer.ui

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

class CenterMarginFix : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        var left = 0
        if (position.rem(2) != 0) {
            val marginParams = view.layoutParams as ViewGroup.MarginLayoutParams
            marginParams.leftMargin
            left = -marginParams.leftMargin
        }
        outRect.set(left, 0, 0, 0)
    }
}
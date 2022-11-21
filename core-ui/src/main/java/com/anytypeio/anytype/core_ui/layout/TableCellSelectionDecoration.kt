package com.anytypeio.anytype.core_ui.layout

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import kotlin.math.roundToInt

class TableCellSelectionDecoration(
    private val drawable: Drawable
) : RecyclerView.ItemDecoration() {

    private var selectionState: Map<Int, BlockView.Table.CellSelection> = emptyMap()

    fun setSelectionState(newState: Map<Int, BlockView.Table.CellSelection>) {
        selectionState = newState
    }

    override fun onDrawOver(
        canvas: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        canvas.save()
        val rect = Rect()
        parent.children.forEach { view ->
            val position = parent.getChildAdapterPosition(view)
            if (position != RecyclerView.NO_POSITION) {
                val cellSelection = selectionState[position]
                if (cellSelection != null) {
                    parent.getDecoratedBoundsWithMargins(view, rect)
                    if (cellSelection.left) {
                        drawable.setBounds(
                            rect.left,
                            rect.top,
                            rect.left + drawable.intrinsicWidth,
                            rect.bottom
                        )
                        drawable.draw(canvas)
                    }
                    if (cellSelection.top) {
                        val top = rect.top + view.translationY.roundToInt()
                        val bottom = top + drawable.intrinsicHeight
                        drawable.setBounds(
                            rect.left,
                            top,
                            rect.right,
                            bottom
                        )
                        drawable.draw(canvas)
                    }
                    if (cellSelection.right) {
                        val right = rect.right + view.translationX.roundToInt()
                        val left = right - drawable.intrinsicWidth
                        drawable.setBounds(
                            left,
                            rect.top,
                            right,
                            rect.bottom
                        )
                        drawable.draw(canvas)
                    }
                    if (cellSelection.bottom) {
                        val bottom = rect.bottom + view.translationY.roundToInt()
                        val top = bottom - drawable.intrinsicHeight
                        drawable.setBounds(
                            rect.left,
                            top,
                            rect.right,
                            bottom
                        )
                        drawable.draw(canvas)
                    }
                }
            }
        }
        canvas.restore()
    }
}
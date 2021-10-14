package com.anytypeio.anytype.core_ui.features.editor

import android.graphics.Canvas
import android.view.DragEvent
import android.view.View
import android.widget.EditText
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import timber.log.Timber

class EditorDragAndDropListener(
    val onDragLocation: (v: View, ratio: Float) -> Unit,
    val onDrop: (v: View, ratio: Float) -> Unit,
    val onDragEnded: (v: View) -> Unit,
    val onDragExited: (v: View) -> Unit
) : View.OnDragListener {

    override fun onDrag(v: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_ENTERED -> {
                val ratio = event.y / v.height
                onDragLocation(v, ratio)
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                val ratio = event.y / v.height
                onDragLocation(v, ratio)
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                onDragExited(v)
            }
            DragEvent.ACTION_DROP -> {
                val ratio = event.y / v.height
                onDrop(v, ratio)
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                onDragEnded(v)
            }
        }
        return true
    }
}

class DefaultEditorDragShadow(view: View) : View.DragShadowBuilder(view)
class TextInputDragShadow(private val textInputId: Int?, parent: View) : View.DragShadowBuilder(parent) {

    val input: EditText? get() {
        return if (textInputId != null)
            view.findViewById(textInputId)
        else
            null
    }

    override fun onDrawShadow(canvas: Canvas?) {
        input?.isCursorVisible = false
        super.onDrawShadow(canvas)
        input?.isCursorVisible = true
    }
}
package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import com.anytypeio.anytype.core_ui.R
import kotlinx.android.synthetic.main.widet_undo_redo_panel.view.*

class UndoRedoToolbarWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.widet_undo_redo_panel, this)
    }

    val undo: View get() = btnUndo
    val redo: View get() = btnRedo
}
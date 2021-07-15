package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.StatusBadgeWidget
import kotlinx.android.synthetic.main.widget_document_top_toolbar.view.*

class DocumentTopToolbar : ConstraintLayout {

    val status: StatusBadgeWidget get() = syncStatusBadge
    val back: View get() = toolbarBackButton
    val menu: View get() = toolbarMenu
    val container: FrameLayout get() = toolbarIconContainer
    val title: TextView get() = toolbarTitle
    val emoji: TextView get() = toolbarEmojiIcon
    val image: ImageView get() = toolbarImageIcon
    val undo: ImageView get() = btnUndo
    val redo: ImageView get() = btnRedo

    constructor(
        context: Context
    ) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        inflate()
    }

    private fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.widget_document_top_toolbar, this)
    }

    fun setUndoState(isEnabled: Boolean) {
        //btnUndo.alpha = if (isEnabled) 1.0f else 0.3f
        //btnUndo.isEnabled = isEnabled
    }

    fun setRedoState(isEnabled: Boolean) {
        //btnRedo.alpha = if (isEnabled) 1.0f else 0.3f
        //btnRedo.isEnabled = isEnabled
    }
}
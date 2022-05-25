package com.anytypeio.anytype.core_ui.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.PopupExtensions.calculateRectInWindow
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.presentation.editor.editor.BlockDimensions
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel

fun Context.toast(
    msg: CharSequence,
    gravity: Int? = null,
    duration: Int = Toast.LENGTH_LONG
) = Toast
    .makeText(this, msg, duration)
    .apply {
        if (gravity != null) setGravity(
            gravity,
            0,
            resources.getDimension(R.dimen.default_toast_top_gravity_offset).toInt()
        )
    }
    .show()

fun View.tint(color: Int) {
    backgroundTintList = ColorStateList.valueOf(color)
}

fun LinearLayout.addVerticalDivider(
    alpha: Float,
    height: Int,
    color: Int
) = addView(
    View(context).apply {
        setBackgroundColor(color)
        setAlpha(alpha)
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            height
        )
    }
)

fun EditText.cursorYBottomCoordinate(): Int {
    with(this.layout) {
        val pos = selectionStart
        val line = getLineForOffset(pos)
        val baseLine = getLineBaseline(line)
        val descent = getLineDescent(line)
        val rect = calculateRectInWindow(this@cursorYBottomCoordinate)
        return rect.top + baseLine + descent - this.topPadding
    }
}

fun TextView.range(): IntRange = selectionStart..selectionEnd

fun View.dimensions(): BlockDimensions {
    val rect = calculateRectInWindow(this)
    return BlockDimensions(
        left = rect.left,
        top = rect.top,
        bottom = rect.bottom,
        right = rect.right,
        height = this.height,
        width = this.width
    )
}

fun EditText.setInputTypeBaseOnFormat(format: ColumnView.Format) = when (format) {
    ColumnView.Format.SHORT_TEXT -> {
        inputType = InputType.TYPE_CLASS_TEXT
        isSingleLine = true
    }
    ColumnView.Format.LONG_TEXT -> {
        inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        isSingleLine = false
    }
    ColumnView.Format.NUMBER -> {
        inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
    }
    ColumnView.Format.URL -> {
        inputType = InputType.TYPE_TEXT_VARIATION_URI
    }
    ColumnView.Format.EMAIL -> {
        inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    }
    ColumnView.Format.PHONE -> {
        inputType = InputType.TYPE_CLASS_PHONE
    }
    else -> {
    }
}

fun RecyclerView.addTextFromSelectedStart(text: String) {
    this.findFocus()?.let { child: View? ->
        if (child is TextInputWidget) {
            val selectionStart = child.selectionStart
            val length = child.text?.length
            if (length != null && selectionStart in 0..length) {
                try {
                    child.text?.insert(child.selectionStart, text)
                } catch (e: Exception) {
                    this.context.toast("Error, can't set $text")
                }
            } else {
                if (BuildConfig.DEBUG) {
                    this.context.toast("Selection position error, can't set $text")
                }
            }
        }
    }
}

fun ShapeableImageView.setCircularShape() {
    val shapeModel = ShapeAppearanceModel.builder(
        context,
        R.style.ShapeAppearance_MaterialComponents_Circle,
        0
    ).build()
    shapeAppearanceModel = shapeModel
}

fun ShapeableImageView.setCorneredShape(cornerRadius: Float) {
    val shapeModel = shapeAppearanceModel
        .toBuilder()
        .setAllCornerSizes(cornerRadius)
        .build()
    shapeAppearanceModel = shapeModel
}
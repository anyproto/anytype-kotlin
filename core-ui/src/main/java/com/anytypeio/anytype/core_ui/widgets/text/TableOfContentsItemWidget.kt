package com.anytypeio.anytype.core_ui.widgets.text

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.TvTableOfContentsBinding

class TableOfContentsItemWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val binding = TvTableOfContentsBinding.inflate(LayoutInflater.from(context), this)
    val textView = binding.text

    init {
        textView.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
        }
    }

    fun setName(name: String) {
        textView.text = name.ifBlank { resources.getString(R.string.untitled) }
    }
}
package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.toast

class ObjectRemovalProgressWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.widget_object_deletion_progress_bar, this)

        setBackgroundColor(Color.parseColor("#40000000"))
        setOnClickListener {
            context.toast(context.getString(R.string.please_wait_your_object_deletion))
        }
    }
}
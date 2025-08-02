package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.simpleIcon

class RelationFormatIconWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    fun bind(format: RelationFormat, isName: Boolean = false) {
        if (isName) {
            setImageResource(R.drawable.ic_relation_name)
        } else {
            setImageResource(format.simpleIcon())
        }
    }
}
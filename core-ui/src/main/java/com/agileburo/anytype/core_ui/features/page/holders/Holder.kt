package com.agileburo.anytype.core_ui.features.page.holders

import android.view.Gravity
import android.widget.TextView
import com.agileburo.anytype.core_ui.common.Alignment

interface Holder {

    interface Selectable {
        fun select(isSelected: Boolean)
    }

    interface Indentable {
        fun indentize(indent: Int)
    }

    interface Alignable {

        val alignable: TextView

        fun align(alignment: Alignment?) {
            if (alignment != null) {
                alignable.gravity = when (alignment) {
                    Alignment.START -> Gravity.START
                    Alignment.CENTER -> Gravity.CENTER
                    Alignment.END -> Gravity.END
                }
            }
        }
    }
}
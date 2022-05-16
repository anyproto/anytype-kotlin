package com.anytypeio.anytype.core_ui.features.relations.holders

import android.text.InputType
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R

abstract class RelationTextValueViewHolderBase(view: View) : RecyclerView.ViewHolder(view) {
    abstract val input: TextView
    fun enableReadMode() {
        input.inputType = InputType.TYPE_NULL
        input.setHint(R.string.empty)
    }
}
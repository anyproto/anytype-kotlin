package com.anytypeio.anytype.core_ui.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class AbstractViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: T)
}
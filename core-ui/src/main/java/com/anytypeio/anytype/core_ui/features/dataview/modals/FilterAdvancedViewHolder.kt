package com.anytypeio.anytype.core_ui.features.dataview.modals

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemDvViewerFilterAdvancedBinding
import com.anytypeio.anytype.presentation.sets.model.FilterView

/**
 * ViewHolder for displaying advanced filters in the filter list.
 * Advanced filters are read-only on mobile and can only be edited on Desktop.
 * They display a static title and description without remove/navigation icons.
 */
class FilterAdvancedViewHolder(
    val binding: ItemDvViewerFilterAdvancedBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: FilterView.Advanced) {
        // The layout uses static text from string resources,
        // so no dynamic binding is needed for title and description.
        // The item simply displays "Advanced filter" with a description
        // indicating it can only be edited on Desktop.
    }
}

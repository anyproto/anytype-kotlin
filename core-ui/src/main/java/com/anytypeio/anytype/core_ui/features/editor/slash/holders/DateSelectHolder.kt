package com.anytypeio.anytype.core_ui.features.editor.slash.holders

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetSelectDateBinding
import com.anytypeio.anytype.core_models.ui.ObjectIcon

class DateSelectHolder(
    val binding: ItemSlashWidgetSelectDateBinding
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.ivIcon.setIcon(
            ObjectIcon.TypeIcon.Default.DATE
        )
    }
}
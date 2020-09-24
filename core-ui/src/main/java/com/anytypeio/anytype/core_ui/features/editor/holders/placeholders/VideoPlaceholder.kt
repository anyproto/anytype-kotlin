package com.anytypeio.anytype.core_ui.features.editor.holders.placeholders

import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.ListenerType
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.indentize

class VideoPlaceholder(view: View) : MediaPlaceholder(view) {

    override val root: View = itemView

    override fun placeholderClick(target: String, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.Video.Placeholder(target))
    }

    override fun indentize(item: BlockView.Indentable) {
        root.indentize(
            indent = item.indent,
            defIndent = dimen(R.dimen.indent),
            margin = dimen(R.dimen.bookmark_default_margin_start)
        )
    }

    override fun select(isSelected: Boolean) {
        root.isSelected = isSelected
    }
}
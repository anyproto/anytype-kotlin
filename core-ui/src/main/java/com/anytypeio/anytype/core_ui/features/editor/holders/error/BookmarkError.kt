package com.anytypeio.anytype.core_ui.features.editor.holders.error

import android.view.View
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockBookmarkErrorBinding
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.indentize
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class BookmarkError(val binding: ItemBlockBookmarkErrorBinding) : MediaError(binding.root) {

    override val root: View = binding.bookmarkErrorRoot
    private val urlView: TextView = binding.errorBookmarkUrl

    fun setUrl(url: String) {
        urlView.text = url
    }

    override fun errorClick(item: BlockView.Error, clicked: (ListenerType) -> Unit) {
        if (item is BlockView.Error.Bookmark) {
            clicked(ListenerType.Bookmark.Error(item))
        }
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
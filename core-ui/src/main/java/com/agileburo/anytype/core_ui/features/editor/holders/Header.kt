package com.agileburo.anytype.core_ui.features.editor.holders

import android.text.Editable
import android.view.View
import androidx.core.view.updatePadding
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.features.page.TextHolder
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.dimen

abstract class Header(
    view: View
) : Text(view), TextHolder, BlockViewHolder.IndentableHolder {

    abstract val header: TextInputWidget

    fun bind(
        block: BlockView.Header,
        onTextChanged: (String, Editable) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit
    ) = super.bind(
        item = block,
        onSelectionChanged = onSelectionChanged,
        onTextChanged = onTextChanged,
        onFocusChanged = onFocusChanged,
        clicked = clicked
    )

    override fun indentize(item: BlockView.Indentable) {
        header.updatePadding(
            left = dimen(R.dimen.default_document_content_padding_start) + item.indent * dimen(R.dimen.indent)
        )
    }
}
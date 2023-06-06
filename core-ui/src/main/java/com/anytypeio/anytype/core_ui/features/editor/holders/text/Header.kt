package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.view.View
import androidx.core.view.updatePadding
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.TextBlockHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

abstract class Header<T : BlockView.Text.Header>(
    view: View,
    clicked: (ListenerType) -> Unit,
) : Text<T>(view, clicked),
    TextBlockHolder,
    BlockViewHolder.IndentableHolder,
    DecoratableViewHolder {

    abstract val header: TextInputWidget

    @Deprecated("Pre-nested-styling legacy.")
    override fun indentize(item: BlockView.Indentable) {
        // Do nothing.
    }
}
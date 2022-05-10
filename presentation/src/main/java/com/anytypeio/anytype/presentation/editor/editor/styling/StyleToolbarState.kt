package com.anytypeio.anytype.presentation.editor.editor.styling

import com.anytypeio.anytype.core_models.TextStyle
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment

sealed class StyleToolbarState {

    data class Text(val textStyle: TextStyle?) : StyleToolbarState() {
        companion object {
            fun empty() = Text(null)
        }
    }

    data class Other(
        val isSupportBold: Boolean = false,
        val isSupportItalic: Boolean = false,
        val isSupportStrikethrough: Boolean = false,
        val isSupportCode: Boolean = false,
        val isSupportLinked: Boolean = false,
        val isSupportAlignStart: Boolean = false,
        val isSupportAlignCenter: Boolean = false,
        val isSupportAlignEnd: Boolean = false,
        val isBoldSelected: Boolean = false,
        val isItalicSelected: Boolean = false,
        val isStrikethroughSelected: Boolean = false,
        val isCodeSelected: Boolean = false,
        val isLinkedSelected: Boolean = false,
        val isAlignStartSelected: Boolean = false,
        val isAlignCenterSelected: Boolean = false,
        val isAlignEndSelected: Boolean = false
    ) : StyleToolbarState() {
        companion object {
            fun empty() = Other()
        }
    }

    data class ColorBackground(val color: String?, val background: String?) : StyleToolbarState() {
        companion object {
            fun empty() = ColorBackground(null, null)
        }
    }

    data class Background(val background: String?) : StyleToolbarState() {
        companion object {
            fun empty() = Background(null)
        }
    }
}

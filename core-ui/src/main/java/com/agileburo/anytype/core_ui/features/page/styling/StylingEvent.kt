package com.agileburo.anytype.core_ui.features.page.styling

import com.agileburo.anytype.core_ui.common.ThemeColor

sealed class StylingEvent {

    sealed class Alignment : StylingEvent() {
        object Left : Alignment()
        object Center : Alignment()
        object Right : Alignment()
    }

    sealed class Markup : StylingEvent() {
        object Bold : Markup()
        object Italic : Markup()
        object Strikethrough : Markup()
        object Code : Markup()
    }

    sealed class Coloring : StylingEvent() {
        data class Text(val color: ThemeColor) : StylingEvent()
        data class Background(val color: ThemeColor) : StylingEvent()
    }
}
package com.anytypeio.anytype.presentation.editor.editor.styling

import com.anytypeio.anytype.presentation.editor.editor.ThemeColor


sealed class StylingEvent {

    sealed class Alignment : StylingEvent() {
        object Left : Alignment()
        object Center : Alignment()
        object Right : Alignment()
    }

    sealed class Markup : StylingEvent() {
        object Bold : Markup()
        object Italic : Markup()
        object StrikeThrough : Markup()
        object Code : Markup()
        object Link : Markup()
    }

    sealed class Coloring : StylingEvent() {
        data class Text(val color: ThemeColor) : StylingEvent()
        data class Background(val color: ThemeColor) : StylingEvent()
    }

    sealed class Sliding : StylingEvent() {
        object Background : Sliding()
        object Color : Sliding()
    }
}
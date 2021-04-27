package com.anytypeio.anytype.presentation.page.markup

sealed class MarkupColorView {
    abstract val code: String
    abstract val isSelected: Boolean

    data class Text(
        override val code: String,
        override val isSelected: Boolean
    ) : MarkupColorView()

    data class Background(
        override val code: String,
        override val isSelected: Boolean
    ) : MarkupColorView()
}
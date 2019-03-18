package com.agileburo.anytype.feature_editor.domain

sealed class Content {
    data class Text(
        val text : CharSequence,
        val marks : List<Mark>
    ) : Content()
}

data class Mark(
    val start : Int,
    val end : Int,
    val type : MarkType,
    val param : Any
) {
    enum class MarkType {
        BOLD, ITALIC, UNDERLINE, STRIKE_THROUGH
    }
}
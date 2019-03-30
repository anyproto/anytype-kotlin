package com.agileburo.anytype.feature_editor.domain

sealed class Content {
    data class Text(
        val text : CharSequence,
        val param : Any,
        val marks : List<Mark>
    ) : Content()
}

data class Mark(
    val start : Int,
    val end : Int,
    val type : MarkType
) {
    enum class MarkType {
        BOLD, ITALIC, UNDERLINE, STRIKE_THROUGH
    }
}
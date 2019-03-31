package com.agileburo.anytype.feature_editor.domain

sealed class Content {
    data class Text(
        val text : CharSequence,
        val param : ContentParam,
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

data class ContentParam(val map : Map<String, Any?>) {
    val number : Int by map
    val checked : Boolean by map


    companion object {
        fun numberedListDefaultParam(): ContentParam {
            return ContentParam(
                mapOf(
                    "number" to 1,
                    "checked" to null
                )
            )
        }
    }
}
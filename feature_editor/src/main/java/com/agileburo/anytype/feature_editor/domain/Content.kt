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

        fun empty() : ContentParam {
            return ContentParam(
                mapOf(
                    "number" to null,
                    "checked" to null
                )
            )
        }

        fun numberedList(number : Int = 1): ContentParam {
            return ContentParam(
                mapOf(
                    "number" to number,
                    "checked" to null
                )
            )
        }
    }
}
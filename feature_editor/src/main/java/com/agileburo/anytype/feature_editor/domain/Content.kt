package com.agileburo.anytype.feature_editor.domain

sealed class Content {
    data class Text(
        var text : CharSequence,
        val param : ContentParam,
        val marks : List<Mark>
    ) : Content()
}

data class Mark(
    val start : Long,
    val end : Long,
    val type : MarkType,
    val param: String
) {
    enum class MarkType {
        BOLD, ITALIC, UNDERLINE, STRIKE_THROUGH, HYPERTEXT, CODE, UNDEFINED
    }
}

data class ContentParam(val map : MutableMap<String, Any?>) {
    var number : Int by map
    var checked : Boolean by map


    companion object {

        fun empty() : ContentParam {
            return ContentParam(
                mutableMapOf(
                    "number" to 0,
                    "checked" to false
                )
            )
        }

        fun numberedList(number : Int = 1): ContentParam {
            return ContentParam(
                mutableMapOf(
                    "number" to number,
                    "checked" to false
                )
            )
        }
    }
}
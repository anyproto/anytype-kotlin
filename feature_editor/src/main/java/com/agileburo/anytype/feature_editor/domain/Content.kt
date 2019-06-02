package com.agileburo.anytype.feature_editor.domain

sealed class Content {

    data class Text(
        val text : String,
        val param : ContentParam,
        val marks : List<Mark>
    ) : Content()

    data class Page(
        val id : String
    ) : Content()

    data class Bookmark(
        val type : String,
        val url : String,
        val title : String,
        val description : String,
        val site : String,
        val icon : String,
        val images : List<Image>
    ) : Content() {

        data class Image(val url : String)

    }

    @Deprecated("Picture will be downloaded from device or in some other way")
    data class Picture(
        val url : String,
        val type : Type
    ) : Content() {
        enum class Type { ORIGINAL, THUMBNAIL }
    }

    object Empty : Content()


}

data class ContentParam(val map : MutableMap<String, Any?>) {
    var number : Int by map
    var checked : Boolean by map


    companion object {

        fun checkbox(checked : Boolean) : ContentParam {
            return ContentParam(
                mutableMapOf(
                    "number" to 0,
                    "checked" to checked
                )
            )
        }

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

data class Mark(
    val start : Int,
    val end : Int,
    val type : MarkType,
    val param: String
) {
    enum class MarkType {
        BOLD, ITALIC, UNDERLINE, STRIKE_THROUGH, HYPERTEXT, CODE, UNDEFINED
    }
}

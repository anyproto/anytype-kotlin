package com.anytypeio.anytype.core_models

data class LinkPreview(
    val url: String,
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val faviconUrl: String = "",
    val type: Type = Type.Unknown
) {
    enum class Type {
        Unknown,
        Page,
        Image,
        Text;

        companion object {
            fun fromProto(value: Int): Type = when (value) {
                1    -> Page
                2    -> Image
                3    -> Text
                else -> Unknown
            }
        }

        fun toProto(): Int = when (this) {
            Page    -> 1
            Image   -> 2
            Text    -> 3
            Unknown -> 0
        }
    }
}
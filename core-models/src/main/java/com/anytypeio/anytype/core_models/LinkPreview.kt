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
    }
}
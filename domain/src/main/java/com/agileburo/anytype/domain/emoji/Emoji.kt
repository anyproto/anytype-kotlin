package com.agileburo.anytype.domain.emoji

data class Emoji(
    val unicode: String,
    val alias: String
) {
    val name: String
        get() = ":$alias:"
}
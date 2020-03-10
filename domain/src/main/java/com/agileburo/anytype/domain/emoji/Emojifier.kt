package com.agileburo.anytype.domain.emoji

interface Emojifier {
    suspend fun fromAlias(alias: String): Emoji
    suspend fun fromShortName(name: String): Emoji
}
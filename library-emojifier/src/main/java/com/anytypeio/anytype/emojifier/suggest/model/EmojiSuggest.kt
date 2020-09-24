package com.anytypeio.anytype.emojifier.suggest.model

/**
 * @property emoji emoji char
 * @property name short name for this [emoji]
 * @property category category for this [emoji]
 */
interface EmojiSuggest {
    val emoji: String
    val name: String
    val category: String
}
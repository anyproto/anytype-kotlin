package com.anytypeio.anytype.presentation.picker

/**
 * Data model for emoji picker items.
 * Used by ViewModels to represent emoji picker state.
 */
sealed class EmojiPickerView {
    /**
     * A custom section header with a title.
     */
    data class Section(val title: String) : EmojiPickerView()
    
    /**
     * An emoji category header.
     */
    data class Category(val index: Int) : EmojiPickerView()
    
    /**
     * An individual emoji item.
     */
    data class Emoji(
        val unicode: String,
        val page: Int,
        val index: Int,
        val emojified: String = ""
    ) : EmojiPickerView()
}

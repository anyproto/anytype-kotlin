package com.agileburo.anytype.presentation.page.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.icon.SetDocumentEmojiIcon
import com.agileburo.anytype.emojifier.data.EmojiProvider
import com.agileburo.anytype.emojifier.suggest.EmojiSuggester

class DocumentEmojiIconPickerViewModelFactory(
    private val setEmojiIcon: SetDocumentEmojiIcon,
    private val emojiSuggester: EmojiSuggester,
    private val emojiProvider: EmojiProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DocumentEmojiIconPickerViewModel(
            setEmojiIcon = setEmojiIcon,
            suggester = emojiSuggester,
            provider = emojiProvider
        ) as T
    }
}
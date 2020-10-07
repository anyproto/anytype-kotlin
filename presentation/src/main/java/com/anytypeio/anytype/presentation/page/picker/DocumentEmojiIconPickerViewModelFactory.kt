package com.anytypeio.anytype.presentation.page.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.icon.SetDocumentEmojiIcon
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.presentation.util.Bridge

class DocumentEmojiIconPickerViewModelFactory(
    private val setEmojiIcon: SetDocumentEmojiIcon,
    private val emojiSuggester: EmojiSuggester,
    private val emojiProvider: EmojiProvider,
    private val bridge: Bridge<Payload>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DocumentEmojiIconPickerViewModel(
            setEmojiIcon = setEmojiIcon,
            suggester = emojiSuggester,
            provider = emojiProvider,
            bridge = bridge
        ) as T
    }
}
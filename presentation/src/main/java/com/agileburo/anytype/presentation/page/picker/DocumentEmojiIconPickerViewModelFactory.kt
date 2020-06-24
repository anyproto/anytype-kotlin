package com.agileburo.anytype.presentation.page.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.icon.SetDocumentEmojiIcon

class DocumentEmojiIconPickerViewModelFactory(
    private val setEmojiIcon: SetDocumentEmojiIcon
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DocumentEmojiIconPickerViewModel(
            setEmojiIcon = setEmojiIcon
        ) as T
    }
}
package com.agileburo.anytype.presentation.page.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.icon.SetDocumentEmojiIcon
import com.agileburo.anytype.domain.icon.SetDocumentImageIcon

class DocumentIconActionMenuViewModelFactory(
    private val setEmojiIcon: SetDocumentEmojiIcon,
    private val setImageIcon: SetDocumentImageIcon
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = DocumentIconActionMenuViewModel(
        setEmojiIcon = setEmojiIcon,
        setImageIcon = setImageIcon
    ) as T
}
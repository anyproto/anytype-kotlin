package com.anytypeio.anytype.presentation.page.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.icon.SetDocumentEmojiIcon
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.presentation.util.Bridge

class DocumentIconActionMenuViewModelFactory(
    private val setEmojiIcon: SetDocumentEmojiIcon,
    private val setImageIcon: SetDocumentImageIcon,
    private val bridge: Bridge<Payload>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = DocumentIconActionMenuViewModel(
        setEmojiIcon = setEmojiIcon,
        setImageIcon = setImageIcon,
        bridge = bridge
    ) as T
}
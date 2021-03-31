package com.anytypeio.anytype.presentation.page.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.icon.SetDocumentEmojiIcon
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.presentation.page.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.util.Dispatcher

class DocumentIconActionMenuViewModelFactory(
    private val setEmojiIcon: SetDocumentEmojiIcon,
    private val setImageIcon: SetDocumentImageIcon,
    private val dispatcher: Dispatcher<Payload>,
    private val details: DetailModificationManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = DocumentIconActionMenuViewModel(
        setEmojiIcon = setEmojiIcon,
        setImageIcon = setImageIcon,
        dispatcher = dispatcher,
        details = details
    ) as T
}
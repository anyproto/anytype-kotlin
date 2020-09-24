package com.anytypeio.anytype.features.emoji

import com.anytypeio.anytype.presentation.page.picker.DocumentEmojiIconPickerViewModelFactory
import com.anytypeio.anytype.ui.page.modals.DocumentEmojiIconPickerFragment

class TestDocumentEmojiPickerFragment : DocumentEmojiIconPickerFragment() {
    init {
        factory = testViewModelFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testViewModelFactory: DocumentEmojiIconPickerViewModelFactory
    }
}
package com.agileburo.anytype.features.emoji

import com.agileburo.anytype.presentation.page.picker.DocumentEmojiIconPickerViewModelFactory
import com.agileburo.anytype.ui.page.modals.DocumentEmojiIconPickerFragment

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
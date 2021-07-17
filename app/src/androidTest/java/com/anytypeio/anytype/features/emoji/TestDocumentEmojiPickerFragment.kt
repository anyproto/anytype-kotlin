package com.anytypeio.anytype.features.emoji

import com.anytypeio.anytype.presentation.page.picker.ObjectIconPickerViewModelFactory
import com.anytypeio.anytype.ui.page.modals.ObjectIconPickerFragment

class TestDocumentEmojiPickerFragment : ObjectIconPickerFragment() {
    init {
        factory = testViewModelFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testViewModelFactory: ObjectIconPickerViewModelFactory
    }
}
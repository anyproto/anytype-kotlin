package com.anytypeio.anytype.features.editor.base

import com.anytypeio.anytype.presentation.editor.EditorViewModelFactory
import com.anytypeio.anytype.ui.editor.EditorFragment

class TestEditorFragment : EditorFragment() {
    init {
        factory = testViewModelFactory
    }
    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testViewModelFactory: EditorViewModelFactory
    }
}
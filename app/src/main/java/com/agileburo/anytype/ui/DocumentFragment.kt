package com.agileburo.anytype.ui

import com.agileburo.anytype.AndroidApplication
import com.agileburo.anytype.feature_editor.ui.EditorFragment

class DocumentFragment : EditorFragment() {

    private val appComponent by lazy {
        (requireActivity().application as AndroidApplication).applicationComponent
    }

    private val editorComponent by lazy {
        appComponent.editorComponent()
    }

    override fun inject() {
        editorComponent.inject(this)
    }
}
package com.agileburo.anytype.ui

import android.content.Context
import com.agileburo.anytype.AndroidApplication
import com.agileburo.anytype.feature_editor.ui.EditorFragment

class DocumentFragment : EditorFragment() {

    private val appComponent by lazy {
        (requireActivity().application as AndroidApplication).applicationComponent
    }

    private val editorComponent by lazy {
        appComponent.editorComponent()
    }

    override fun onAttach(context: Context?) {
        editorComponent.inject(this)
        super.onAttach(context)
    }

    override fun inject() {
        editorComponent.inject(this)
    }
}
package com.agileburo.anytype

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agileburo.anytype.editor.EditorTextWatcher
import kotlinx.android.synthetic.main.fragment_b.*

class FragmentB : Fragment() {

    private val codeBlockTypeface by lazy {
        ResourcesCompat.getFont(context!!.applicationContext, R.font.inconsolata)
    }

    private lateinit var editorTextWatcher: EditorTextWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editorTextWatcher = EditorTextWatcher(codeBlockTypeface!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_b, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editText.addTextChangedListener(editorTextWatcher)

        editorToolbar.setMainActions(
            boldClick = { editorTextWatcher.isBoldActive = it },
            italicClick = { editorTextWatcher.isItalicActive = it },
            strokeClick = { editorTextWatcher.isStrokeThroughActive = it },
            underlineClick = { editorTextWatcher.isUnderlineActive = it },
            codeBlockClick = { editorTextWatcher.isCodeBlockActive = it }
        )
    }

    override fun onDestroyView() {
        editText.removeTextChangedListener(editorTextWatcher)
        super.onDestroyView()
    }
}
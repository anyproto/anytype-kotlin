package com.agileburo.anytype.feature_editor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.feature_editor.R
import com.agileburo.anytype.feature_editor.presentation.EditorViewModel
import com.agileburo.anytype.feature_editor.presentation.EditorViewModelFactory
import kotlinx.android.synthetic.main.fragment_editor.*
import javax.inject.Inject

abstract class EditorFragment: Fragment(){

    @Inject
    lateinit var factory: EditorViewModelFactory
    private val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(EditorViewModel::class.java)
    }

    abstract fun inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getBlocks()
        btnBack.setOnClickListener { viewModel.sendToIPFS() }
    }
}
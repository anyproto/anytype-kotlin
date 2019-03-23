package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.feature_editor.R
import com.agileburo.anytype.feature_editor.disposedBy
import com.agileburo.anytype.feature_editor.presentation.EditorViewModel
import com.agileburo.anytype.feature_editor.presentation.EditorViewModelFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_editor.*
import javax.inject.Inject

abstract class EditorFragment : Fragment() {

    @Inject
    lateinit var factory: EditorViewModelFactory
    private val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(EditorViewModel::class.java)
    }
    private var disposable = CompositeDisposable()

    abstract fun inject()

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        viewModel.observeState()
            .subscribe { handleState(it) }
            .disposedBy(disposable)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeView()
        viewModel.getBlocks()
    }

    private fun initializeView() = with(blockList) {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = EditorAdapter(mutableListOf())
        setHasFixedSize(true)
        addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        disposable.clear()
        super.onPause()
    }

    private fun handleState(state: EditorState) = when (state) {
        is EditorState.Loading -> {
        }
        is EditorState.Result -> {
            (blockList.adapter as? EditorAdapter)?.setBlocks(state.blocks)
        }
    }
}
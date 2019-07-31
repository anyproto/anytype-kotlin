package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.core_utils.UIExtensions
import com.agileburo.anytype.core_utils.toast
import com.agileburo.anytype.feature_editor.R
import com.agileburo.anytype.feature_editor.disposedBy
import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.ContentType
import com.agileburo.anytype.feature_editor.domain.toView
import com.agileburo.anytype.feature_editor.presentation.mapper.BlockModelMapper
import com.agileburo.anytype.feature_editor.presentation.mvvm.EditorViewModel
import com.agileburo.anytype.feature_editor.presentation.mvvm.EditorViewModelFactory
import com.agileburo.anytype.feature_editor.presentation.mapper.BlockViewMapper
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_editor.*
import java.lang.UnsupportedOperationException
import javax.inject.Inject

abstract class EditorFragment : Fragment() {

    private val mapper by lazy { BlockViewMapper() }
    private val viewToModelMapper by lazy { BlockModelMapper() }

    @Inject
    lateinit var factory: EditorViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(EditorViewModel::class.java)
    }

    private val disposable = CompositeDisposable()

    private val blockAdapter by lazy {
        EditorAdapter(
            blocks = mutableListOf(),
            blockContentListener = { viewModel.onBlockContentChanged(viewToModelMapper.mapToModel(it)) },
            menuListener = viewModel::onBlockMenuAction,
            focusListener = viewModel::onBlockFocus,
            onExpandClick = viewModel::onExpandClicked
        ).apply {
            //После добавления helper ломается выделение текста в блоках
//            val helper = ItemTouchHelper(
//                DragAndDropBehavior(
//                    onFinished = viewModel::onSwapFinished,
//                    onItemMoved = viewModel::onSwap
//                )
//            )
//            helper.attachToRecyclerView(blockList)
        }
    }

    abstract fun inject()

    override fun onAttach(context: Context?) {
        inject()
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.observeState()
            .subscribe(this::handleState)
            .disposedBy(disposable)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
    }

    private fun initializeView() = with(blockList) {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = blockAdapter
    }
    
    override fun onDestroyView() {
        disposable.clear()
        super.onDestroyView()
    }

    private fun handleState(state: EditorState) = when (state) {
        is EditorState.Loading -> {
        }
        is EditorState.Result -> setBlocks(state.blocks)
        is EditorState.Update -> updateBlock(state.block)

        is EditorState.Updates -> render(state.blocks)

        is EditorState.Swap -> blockAdapter.swap(state.request)

        is EditorState.Archive -> {
        }
        is EditorState.Error -> onError(state.msg)
        is EditorState.ClearBlockFocus -> clearBlockFocus(state.position, state.contentType)
        is EditorState.HideKeyboard -> UIExtensions.hideSoftKeyBoard(requireActivity(), blockList)
    }

    private fun clearBlockFocus(position: Int, contentType: ContentType) {
        blockList.layoutManager?.findViewByPosition(position)?.let {
            it.findViewById<View>(getEditTextId(contentType))?.clearFocus()
        }
    }

    private fun getEditTextId(contentType: ContentType) =
        when (contentType) {
            is ContentType.P -> R.id.textEditable
            is ContentType.H1 -> R.id.textHeaderOne
            is ContentType.H2 -> R.id.textHeaderTwo
            is ContentType.H3 -> R.id.textHeaderThree
            is ContentType.H4 -> R.id.textHeaderFour
            is ContentType.Quote -> R.id.textQuote
            is ContentType.Check -> R.id.textCheckBox
            is ContentType.Code -> R.id.textCode
            is ContentType.UL -> R.id.textBullet
            is ContentType.NumberedList -> R.id.contentText
            is ContentType.None -> throw IllegalStateException()
            is ContentType.Toggle -> throw UnsupportedOperationException("need implement Toggle")
        }

    private fun setBlocks(blocks: List<Block>) =
        blockAdapter.setBlocks(blocks.toMutableList().toView())

    private fun updateBlock(block: Block) =
        blockAdapter.updateBlock(mapper.mapToView(block))

    private fun render(blocks: List<Block>) =
        blockAdapter.update(blocks.toMutableList().toView())

    private fun onError(msg: CharSequence) = requireContext().toast(msg)
}
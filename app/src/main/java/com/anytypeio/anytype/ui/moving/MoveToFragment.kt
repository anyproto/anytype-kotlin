package com.anytypeio.anytype.ui.moving

import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.navigation.DefaultObjectViewAdapter
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.moving.MoveToView
import com.anytypeio.anytype.presentation.moving.MoveToViewModel
import com.anytypeio.anytype.presentation.moving.MoveToViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_object_search.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MoveToFragment : BaseBottomSheetFragment() {

    private val vm by viewModels<MoveToViewModel> { factory }

    @Inject
    lateinit var factory: MoveToViewModelFactory

    private lateinit var clearSearchText: View
    private lateinit var filterInputField: EditText

    private val blocks get() = arg<List<Id>>(ARG_BLOCKS)
    private val ctx get() = arg<Id>(ARG_CTX)
    private val restorePosition get() = argOrNull<Int>(ARG_RESTORE_POSITION)
    private val restoreBlock get() = argOrNull<Id>(ARG_RESTORE_BLOCK)

    private val moveToAdapter by lazy {
        DefaultObjectViewAdapter(
            onClick = vm::onObjectClicked
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_object_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFullHeight()
        setTransparent()
        BottomSheetBehavior.from(sheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = true
            skipCollapsed = true
            addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            vm.onDialogCancelled()
                        }
                    }
                }
            )
        }
        clearSearchText = searchView.findViewById(R.id.clearSearchText)
        filterInputField = searchView.findViewById(R.id.filterInputField)
        filterInputField.setHint(R.string.search)
        filterInputField.imeOptions = EditorInfo.IME_ACTION_DONE
        filterInputField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                return@setOnEditorActionListener false
            }
            true
        }
        initialize()
    }

    override fun onStart() {
        lifecycleScope.launch {
            jobs += subscribe(vm.viewState) { observe(it) }
            jobs += subscribe(vm.commands) { execute(it) }
        }
        super.onStart()
        vm.onStart(ctx = ctx)
        expand()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        vm.onDialogCancelled()
    }

    private fun observe(state: MoveToView) {
        when (state) {
            MoveToView.Loading -> {
                recyclerView.invisible()
                tvScreenStateMessage.invisible()
                tvScreenStateSubMessage.invisible()
                progressBar.visible()
            }
            is MoveToView.Success -> {
                progressBar.invisible()
                tvScreenStateMessage.invisible()
                tvScreenStateSubMessage.invisible()
                recyclerView.visible()
                moveToAdapter.submitList(state.objects)
            }
            MoveToView.EmptyPages -> {
                progressBar.invisible()
                recyclerView.invisible()
                tvScreenStateMessage.visible()
                tvScreenStateMessage.text = getString(R.string.search_empty_pages)
                tvScreenStateSubMessage.invisible()
            }
            is MoveToView.NoResults -> {
                progressBar.invisible()
                recyclerView.invisible()
                tvScreenStateMessage.visible()
                tvScreenStateMessage.text = getString(R.string.search_no_results, state.searchText)
                tvScreenStateSubMessage.visible()
            }
            is MoveToView.Error -> {
                progressBar.invisible()
                recyclerView.invisible()
                tvScreenStateMessage.visible()
                tvScreenStateMessage.text = state.error
                tvScreenStateSubMessage.invisible()
            }
            MoveToView.Init -> {
                recyclerView.invisible()
                tvScreenStateMessage.invisible()
                tvScreenStateSubMessage.invisible()
                progressBar.invisible()
            }
            else -> Timber.d("Skipping state: $state")
        }
    }

    private fun execute(command: MoveToViewModel.Command) {
        when (command) {
            MoveToViewModel.Command.Exit -> {
                withParent<OnMoveToAction> {
                    onMoveToClose(
                        blocks = blocks,
                        restorePosition = restorePosition,
                        restoreBlock = restoreBlock
                    )
                }
                hideSoftInput()
                dismiss()
            }
            is MoveToViewModel.Command.Move -> {
                if (restorePosition == null && restoreBlock == null) {
                    hideSoftInput()
                }
                withParent<OnMoveToAction> {
                    onMoveTo(
                        target = command.target,
                        blocks = blocks
                    )
                }
                hideSoftInput()
                dismiss()
            }
        }
    }

    private fun initialize() {
        with(tvScreenTitle) {
            text = getString(R.string.move_to)
            visible()
        }
        recyclerView.invisible()
        tvScreenStateMessage.invisible()
        progressBar.invisible()
        clearSearchText.setOnClickListener {
            filterInputField.setText(EMPTY_FILTER_TEXT)
            clearSearchText.invisible()
        }
        filterInputField.doAfterTextChanged { newText ->
            if (newText != null) {
                vm.onSearchTextChanged(newText.toString())
            }
            if (newText.isNullOrEmpty()) {
                clearSearchText.invisible()
            } else {
                clearSearchText.visible()
            }
        }
        with(recyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moveToAdapter
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_object_search))
                }
            )
        }
    }

    private fun setupFullHeight() {
        val lp = (root.layoutParams as FrameLayout.LayoutParams)
        lp.height =
            Resources.getSystem().displayMetrics.heightPixels - requireActivity().statusBarHeight
        root.layoutParams = lp
    }

    private fun setTransparent() {
        with(root) {
            background = null
            (parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun injectDependencies() {
        componentManager().moveToComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().moveToComponent.release()
    }

    companion object {
        const val ARG_BLOCKS = "arg.move_to.blocks"
        const val ARG_CTX = "arg.move_to.ctx"
        const val ARG_RESTORE_POSITION = "arg.move_to.position"
        const val ARG_RESTORE_BLOCK = "arg.move_to.restore_block"
        const val EMPTY_FILTER_TEXT = ""

        fun new(
            ctx: Id,
            blocks: List<Id>,
            restorePosition: Int?,
            restoreBlock: Id?
        ) = MoveToFragment().apply {
            arguments = bundleOf(
                ARG_CTX to ctx,
                ARG_BLOCKS to blocks,
                ARG_RESTORE_POSITION to restorePosition,
                ARG_RESTORE_BLOCK to restoreBlock
            )
        }
    }
}

interface OnMoveToAction {
    fun onMoveTo(target: Id, blocks: List<Id>)
    fun onMoveToClose(blocks: List<Id>, restorePosition: Int?, restoreBlock: Id?)
}
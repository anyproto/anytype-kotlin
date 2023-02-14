package com.anytypeio.anytype.ui.moving

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
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_ui.features.navigation.DefaultObjectViewAdapter
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.statusBarHeight
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetTextInputFragment
import com.anytypeio.anytype.core_utils.ui.TextInputDialogBottomBehaviorApplier
import com.anytypeio.anytype.databinding.FragmentObjectSearchBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.moving.MoveToView
import com.anytypeio.anytype.presentation.moving.MoveToViewModel
import com.anytypeio.anytype.presentation.moving.MoveToViewModelFactory
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class MoveToFragment : BaseBottomSheetTextInputFragment<FragmentObjectSearchBinding>() {

    private val vm by viewModels<MoveToViewModel> { factory }

    @Inject
    lateinit var factory: MoveToViewModelFactory

    override val textInput: EditText get() = binding.searchView.root.findViewById(R.id.filterInputField)

    private lateinit var clearSearchText: View

    private val blocks get() = arg<List<Id>>(ARG_BLOCKS)
    private val ctx get() = arg<Id>(ARG_CTX)
    private val restorePosition get() = argOrNull<Int>(ARG_RESTORE_POSITION)
    private val restoreBlock get() = argOrNull<Id>(ARG_RESTORE_BLOCK)
    private val title get() = argOrNull<String>(ARG_TITLE)

    private val moveToAdapter by lazy {
        DefaultObjectViewAdapter(
            onClick = vm::onObjectClicked
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFullHeight()
        setTransparent()
        BottomSheetBehavior.from(binding.sheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = true
            skipCollapsed = true
        }
        closeKeyboardOnDismiss()
        clearSearchText = binding.searchView.root.findViewById(R.id.clearSearchText)
        textInput.setHint(R.string.search)
        textInput.imeOptions = EditorInfo.IME_ACTION_DONE
        textInput.setOnEditorActionListener { _, actionId, _ ->
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

    private fun closeKeyboardOnDismiss() {
        sheet?.let { sheet ->
            TextInputDialogBottomBehaviorApplier(sheet, textInput, vm).apply()
        }
    }

    private fun observe(state: MoveToView) {
        when (state) {
            MoveToView.Loading -> {
                with(binding) {
                    recyclerView.invisible()
                    tvScreenStateMessage.invisible()
                    tvScreenStateSubMessage.invisible()
                    showProgress()
                }
            }
            is MoveToView.Success -> {
                with(binding) {
                    hideProgress()
                    tvScreenStateMessage.invisible()
                    tvScreenStateSubMessage.invisible()
                    recyclerView.visible()
                    moveToAdapter.submitList(state.objects)
                }
            }
            MoveToView.EmptyPages -> {
                with(binding) {
                    hideProgress()
                    recyclerView.invisible()
                    tvScreenStateMessage.visible()
                    tvScreenStateMessage.text = getString(R.string.search_empty_pages)
                    tvScreenStateSubMessage.invisible()
                }
            }
            is MoveToView.NoResults -> {
                with(binding) {
                    hideProgress()
                    recyclerView.invisible()
                    tvScreenStateMessage.visible()
                    tvScreenStateMessage.text =
                        getString(R.string.search_no_results, state.searchText)
                    tvScreenStateSubMessage.visible()
                }
            }
            is MoveToView.Error -> {
                with(binding) {
                    hideProgress()
                    recyclerView.invisible()
                    tvScreenStateMessage.visible()
                    tvScreenStateMessage.text = state.error
                    tvScreenStateSubMessage.invisible()
                }
            }
            MoveToView.Init -> {
                with(binding) {
                    hideProgress()
                    recyclerView.invisible()
                    tvScreenStateMessage.invisible()
                    tvScreenStateSubMessage.invisible()
                }
            }
            else -> Timber.d("Skipping state: $state")
        }
    }

    private fun execute(command: MoveToViewModel.Command) {
        try {
            when (command) {
                MoveToViewModel.Command.Exit -> {
                    withParent<OnMoveToAction> {
                        onMoveToClose(
                            blocks = blocks,
                            restorePosition = restorePosition,
                            restoreBlock = restoreBlock
                        )
                    }
                    textInput.hideKeyboard()
                    dismiss()
                }
                is MoveToViewModel.Command.Move -> {
                    withParent<OnMoveToAction> {
                        onMoveTo(
                            target = command.view.id,
                            text = command.view.name,
                            icon = command.view.icon,
                            blocks = blocks,
                            isSet = command.view.layout == ObjectType.Layout.SET
                        )
                    }
                    textInput.hideKeyboard()
                    dismiss()
                }
                MoveToViewModel.Command.Init -> {}
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while executing command: $command")
            // TODO find out how to fix this issue:
            // https://issuetracker.google.com/issues/244910446?pli=1
        }
    }

    private fun initialize() {
        with(binding.tvScreenTitle) {
            text = if (title != null) {
                title
            } else {
                getString(R.string.move_to)
            }
            visible()
        }
        binding.recyclerView.invisible()
        binding.tvScreenStateMessage.invisible()
        binding.hideProgress()
        clearSearchText.setOnClickListener {
            textInput.setText(EMPTY_FILTER_TEXT)
            clearSearchText.invisible()
        }
        textInput.doAfterTextChanged { newText ->
            if (newText != null) {
                vm.onSearchTextChanged(newText.toString())
            }
            if (newText.isNullOrEmpty()) {
                clearSearchText.invisible()
            } else {
                clearSearchText.visible()
            }
        }
        with(binding.recyclerView) {
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
        val lp = (binding.root.layoutParams as FrameLayout.LayoutParams)
        lp.height =
            Resources.getSystem().displayMetrics.heightPixels - requireActivity().statusBarHeight
        binding.root.layoutParams = lp
    }

    private fun setTransparent() {
        with(binding.root) {
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

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentObjectSearchBinding = FragmentObjectSearchBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val ARG_BLOCKS = "arg.move_to.blocks"
        const val ARG_CTX = "arg.move_to.ctx"
        const val ARG_RESTORE_POSITION = "arg.move_to.position"
        const val ARG_RESTORE_BLOCK = "arg.move_to.restore_block"
        const val ARG_TITLE = "arg.move_to.title"
        const val EMPTY_FILTER_TEXT = ""

        fun new(
            ctx: Id,
            blocks: List<Id>,
            restorePosition: Int?,
            restoreBlock: Id?,
            title: String? = null
        ) = MoveToFragment().apply {
            arguments = bundleOf(
                ARG_CTX to ctx,
                ARG_BLOCKS to blocks,
                ARG_RESTORE_POSITION to restorePosition,
                ARG_RESTORE_BLOCK to restoreBlock,
                ARG_TITLE to title
            )
        }
    }
}

interface OnMoveToAction {
    fun onMoveTo(target: Id, blocks: List<Id>, text: String, icon: ObjectIcon, isSet: Boolean)
    fun onMoveToClose(blocks: List<Id>, restorePosition: Int?, restoreBlock: Id?)
}


fun FragmentObjectSearchBinding.hideProgress() {
    searchView.progressBar.invisible()
    progressBarRecycler.invisible()
}

fun FragmentObjectSearchBinding.showProgress() {
    searchView.progressBar.visible()
    progressBarRecycler.visible()
}
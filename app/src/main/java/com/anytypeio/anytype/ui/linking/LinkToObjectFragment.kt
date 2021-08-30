package com.anytypeio.anytype.ui.linking

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
import com.anytypeio.anytype.presentation.linking.LinkToObjectViewModel
import com.anytypeio.anytype.presentation.linking.LinkToObjectViewModelFactory
import com.anytypeio.anytype.presentation.search.ObjectSearchView
import com.anytypeio.anytype.ui.search.ObjectSearchFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_link_to_object.progressBar
import kotlinx.android.synthetic.main.fragment_object_search.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LinkToObjectFragment : BaseBottomSheetFragment() {

    private val vm by viewModels<LinkToObjectViewModel> { factory }

    @Inject
    lateinit var factory: LinkToObjectViewModelFactory

    private lateinit var clearSearchText: View
    private lateinit var filterInputField: EditText

    private val target get() = arg<Id>(ARG_TARGET)
    private val position get() = argOrNull<Int>(ARG_POSITION)

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
        vm.state.observe(viewLifecycleOwner, { observe(it) })
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
            jobs += subscribe(vm.commands) { execute(it) }
        }
        super.onStart()
        vm.onStart()
        expand()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        vm.onDialogCancelled()
    }

    private fun observe(state: ObjectSearchView) {
        when (state) {
            ObjectSearchView.Loading -> {
                recyclerView.invisible()
                tvScreenStateMessage.invisible()
                tvScreenStateSubMessage.invisible()
                progressBar.visible()
            }
            is ObjectSearchView.Success -> {
                progressBar.invisible()
                tvScreenStateMessage.invisible()
                tvScreenStateSubMessage.invisible()
                recyclerView.visible()
                moveToAdapter.submitList(state.objects)
            }
            ObjectSearchView.EmptyPages -> {
                progressBar.invisible()
                recyclerView.invisible()
                tvScreenStateMessage.visible()
                tvScreenStateMessage.text = getString(R.string.search_empty_pages)
                tvScreenStateSubMessage.invisible()
            }
            is ObjectSearchView.NoResults -> {
                progressBar.invisible()
                recyclerView.invisible()
                tvScreenStateMessage.visible()
                tvScreenStateMessage.text = getString(R.string.search_no_results, state.searchText)
                tvScreenStateSubMessage.visible()
            }
            is ObjectSearchView.Error -> {
                progressBar.invisible()
                recyclerView.invisible()
                tvScreenStateMessage.visible()
                tvScreenStateMessage.text = state.error
                tvScreenStateSubMessage.invisible()
            }
            else -> Timber.d("Skipping state: $state")
        }
    }

    private fun execute(command: LinkToObjectViewModel.Command) {
        when (command) {
            LinkToObjectViewModel.Command.Exit -> {
                withParent<OnLinkToAction> {
                    onLinkToClose(
                        block = target,
                        position = position
                    )
                }
                dismiss()
            }
            is LinkToObjectViewModel.Command.Link -> {
                withParent<OnLinkToAction> {
                    onLinkTo(
                        link = command.link,
                        target = target
                    )
                }
                dismiss()
            }
        }
    }

    private fun initialize() {
        with(tvScreenTitle) {
            text = getString(R.string.link_to)
            visible()
        }
        recyclerView.invisible()
        tvScreenStateMessage.invisible()
        progressBar.invisible()
        clearSearchText.setOnClickListener {
            filterInputField.setText(ObjectSearchFragment.EMPTY_FILTER_TEXT)
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
        componentManager().linkToObjectComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().linkToObjectComponent.release()
    }

    companion object {
        const val ARG_TARGET = "arg.link_to.target"
        const val ARG_POSITION = "arg.link_to.position"

        fun new(target: Id, position: Int?) = LinkToObjectFragment().apply {
            arguments = bundleOf(
                ARG_TARGET to target,
                ARG_POSITION to position
            )
        }
    }
}

interface OnLinkToAction {
    fun onLinkTo(link: Id, target: Id)
    fun onLinkToClose(block: Id, position: Int?)
}
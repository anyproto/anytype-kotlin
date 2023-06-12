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
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.navigation.DefaultObjectViewAdapter
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.hideSoftInput
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.statusBarHeight
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetTextInputFragment
import com.anytypeio.anytype.databinding.FragmentObjectSearchBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.linking.LinkToObjectViewModel
import com.anytypeio.anytype.presentation.linking.LinkToObjectViewModelFactory
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchView
import com.anytypeio.anytype.ui.moving.hideProgress
import com.anytypeio.anytype.ui.moving.showProgress
import com.anytypeio.anytype.ui.search.ObjectSearchFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class LinkToObjectFragment : BaseBottomSheetTextInputFragment<FragmentObjectSearchBinding>() {

    private val vm by viewModels<LinkToObjectViewModel> { factory }

    @Inject
    lateinit var factory: LinkToObjectViewModelFactory

    private lateinit var clearSearchText: View
    private lateinit var filterInputField: EditText

    override val textInput: EditText get() = binding.searchView.root.findViewById(R.id.filterInputField)

    private val target get() = arg<Id>(ARG_TARGET)
    private val position get() = argOrNull<Int>(ARG_POSITION)
    private val ignore get() = arg<Id>(ARG_IGNORE)

    private val moveToAdapter by lazy {
        DefaultObjectViewAdapter(
            onDefaultObjectClicked = vm::onObjectClicked
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
        vm.state.observe(viewLifecycleOwner) { observe(it) }
        clearSearchText = binding.searchView.root.findViewById(R.id.clearSearchText)
        filterInputField = binding.searchView.root.findViewById(R.id.filterInputField)
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
        vm.onStart(EventsDictionary.Routes.searchMenu, ignore)
        expand()
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        vm.onDialogCancelled()
    }

    private fun observe(state: ObjectSearchView) {
        when (state) {
            ObjectSearchView.Loading -> {
                with(binding) {
                    recyclerView.invisible()
                    tvScreenStateMessage.invisible()
                    tvScreenStateSubMessage.invisible()
                    showProgress()
                }
            }
            is ObjectSearchView.Success -> {
                with(binding) {
                    hideProgress()
                    tvScreenStateMessage.invisible()
                    tvScreenStateSubMessage.invisible()
                    recyclerView.visible()
                    moveToAdapter.submitList(state.objects)
                }
            }
            ObjectSearchView.EmptyPages -> {
                with(binding) {
                    hideProgress()
                    recyclerView.invisible()
                    tvScreenStateMessage.visible()
                    tvScreenStateMessage.text = getString(R.string.search_empty_pages)
                    tvScreenStateSubMessage.invisible()
                }
            }
            is ObjectSearchView.NoResults -> {
                with(binding) {
                    hideProgress()
                    recyclerView.invisible()
                    tvScreenStateMessage.visible()
                    tvScreenStateMessage.text =
                        getString(R.string.search_no_results, state.searchText)
                    tvScreenStateSubMessage.visible()
                }
            }
            is ObjectSearchView.Error -> {
                with(binding) {
                    hideProgress()
                    recyclerView.invisible()
                    tvScreenStateMessage.visible()
                    tvScreenStateMessage.text = state.error
                    tvScreenStateSubMessage.invisible()
                }
            }
            else -> Timber.d("Skipping state: $state")
        }
    }

    private fun execute(command: LinkToObjectViewModel.Command) {
        hideSoftInput()
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
                        target = target,
                        isBookmark = command.isBookmark
                    )
                }
                dismiss()
            }
        }
    }

    private fun initialize() {
        with(binding.tvScreenTitle) {
            text = getString(R.string.link_to)
            visible()
        }
        binding.recyclerView.invisible()
        binding.tvScreenStateMessage.invisible()
        binding.hideProgress()
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
        componentManager().linkToObjectComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().linkToObjectComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentObjectSearchBinding = FragmentObjectSearchBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val ARG_TARGET = "arg.link_to.target"
        const val ARG_POSITION = "arg.link_to.position"
        const val ARG_IGNORE = "arg.link_to.ignore"

        fun new(
            target: Id,
            position: Int?,
            ignore: Id
        ) = LinkToObjectFragment().apply {
            arguments = bundleOf(
                ARG_TARGET to target,
                ARG_POSITION to position,
                ARG_IGNORE to ignore
            )
        }
    }
}

interface OnLinkToAction {
    fun onLinkTo(
        link: Id,
        target: Id,
        isBookmark: Boolean
    )

    fun onLinkToClose(block: Id, position: Int?)
}
package com.anytypeio.anytype.ui.widgets

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
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.navigation.DefaultObjectViewAdapter
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.statusBarHeight
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetTextInputFragment
import com.anytypeio.anytype.databinding.FragmentObjectSearchBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.search.ObjectSearchView
import com.anytypeio.anytype.presentation.widgets.SelectWidgetSourceViewModel
import com.anytypeio.anytype.ui.moving.hideProgress
import com.anytypeio.anytype.ui.moving.showProgress
import com.anytypeio.anytype.ui.search.ObjectSearchFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject
import timber.log.Timber

class SelectWidgetSourceFragment : BaseBottomSheetTextInputFragment<FragmentObjectSearchBinding>() {

    private val vm by viewModels<SelectWidgetSourceViewModel> { factory }

    @Inject
    lateinit var factory: SelectWidgetSourceViewModel.Factory

    private lateinit var clearSearchText: View
    private lateinit var filterInputField: EditText

    override val textInput: EditText get() = binding.searchView.root.findViewById(R.id.filterInputField)

    private val selectWidgetSourceAdapter by lazy {
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
        super.onStart()
        vm.onStart()
        with(lifecycleScope) {
            jobs += subscribe(vm.isDismissed) { isDismissed ->
                if (isDismissed) dismiss()
            }
        }
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
                    selectWidgetSourceAdapter.submitList(state.objects)
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

    private fun initialize() {
        with(binding.tvScreenTitle) {
            text = getString(R.string.widget_source)
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
            adapter = selectWidgetSourceAdapter
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
        componentManager().selectWidgetSourceSubcomponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectWidgetSourceSubcomponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentObjectSearchBinding = FragmentObjectSearchBinding.inflate(
        inflater, container, false
    )

    companion object {

    }
}
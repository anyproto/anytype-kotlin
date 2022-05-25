package com.anytypeio.anytype.ui.search

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.features.navigation.DefaultObjectViewAdapter
import com.anytypeio.anytype.core_utils.ext.hideSoftInput
import com.anytypeio.anytype.core_utils.ext.imm
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.syncFocusWithImeVisibility
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.insets.RootViewDeferringInsetsCallback
import com.anytypeio.anytype.databinding.FragmentObjectSearchBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.search.ObjectSearchView
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModelFactory
import com.anytypeio.anytype.ui.base.ViewStateFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import timber.log.Timber
import javax.inject.Inject

class ObjectSearchFragment : ViewStateFragment<ObjectSearchView, FragmentObjectSearchBinding>(R.layout.fragment_object_search) {

    @Inject
    lateinit var factory: ObjectSearchViewModelFactory

    private lateinit var clearSearchText: View
    private lateinit var filterInputField: EditText

    private val vm by viewModels<ObjectSearchViewModel> { factory }

    private val searchAdapter by lazy {
        DefaultObjectViewAdapter(
            onClick = vm::onObjectClicked
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BottomSheetBehavior.from(binding.sheet).apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = true
            addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            hideSoftInput()
                            vm.onDialogCancelled()
                        }
                    }
                }
            )
        }
        vm.state.observe(viewLifecycleOwner, this)
        vm.navigation.observe(viewLifecycleOwner, navObserver)
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

    override fun onApplyWindowRootInsets() {
        if (BuildConfig.USE_NEW_WINDOW_INSET_API && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val deferringInsetsListener = RootViewDeferringInsetsCallback(
                persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                deferredInsetTypes = 0
            )
            ViewCompat.setWindowInsetsAnimationCallback(binding.root, deferringInsetsListener)
            ViewCompat.setOnApplyWindowInsetsListener(binding.root, deferringInsetsListener)

            binding.searchView.root
                .findViewById<EditText>(R.id.filterInputField)
                .syncFocusWithImeVisibility()
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(EventsDictionary.Routes.searchScreen)
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    private fun focusSearchInput() {
        filterInputField.apply {
            post {
                requestFocus()
                context.imm().showSoftInput(this, InputMethodManager.SHOW_FORCED)
            }
        }
    }

    override fun render(state: ObjectSearchView) {
        when (state) {
            ObjectSearchView.Loading -> {
                with(binding) {
                    recyclerView.invisible()
                    tvScreenStateMessage.invisible()
                    tvScreenStateSubMessage.invisible()
                    progressBar.visible()
                }
            }
            is ObjectSearchView.Success -> {
                with(binding) {
                    progressBar.invisible()
                    tvScreenStateMessage.invisible()
                    tvScreenStateSubMessage.invisible()
                    recyclerView.visible()
                }
                searchAdapter.submitList(state.objects)
            }
            ObjectSearchView.EmptyPages -> {
                with(binding) {
                    progressBar.invisible()
                    recyclerView.invisible()
                    tvScreenStateMessage.visible()
                    tvScreenStateMessage.text = getString(R.string.search_empty_pages)
                    tvScreenStateSubMessage.invisible()
                }
            }
            is ObjectSearchView.NoResults -> {
                with(binding) {
                    progressBar.invisible()
                    recyclerView.invisible()
                    tvScreenStateMessage.visible()
                    tvScreenStateMessage.text = getString(R.string.search_no_results, state.searchText)
                    tvScreenStateSubMessage.visible()
                }
            }
            is ObjectSearchView.Error -> {
                with(binding) {
                    progressBar.invisible()
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
        with(binding) {
            recyclerView.invisible()
            tvScreenStateMessage.invisible()
            progressBar.invisible()
            searchView.root.findViewById<View>(R.id.clearSearchText).setOnClickListener {

            }
        }
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
        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(context.drawable(R.drawable.divider_search))
                }
            )
        }
        focusSearchInput()
    }

    override fun injectDependencies() {
        componentManager().objectSearchComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().objectSearchComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentObjectSearchBinding = FragmentObjectSearchBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val EMPTY_FILTER_TEXT = ""
    }
}
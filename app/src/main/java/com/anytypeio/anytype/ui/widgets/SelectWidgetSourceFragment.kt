package com.anytypeio.anytype.ui.widgets

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.navigation.DefaultObjectViewAdapter
import com.anytypeio.anytype.core_ui.layout.SpacingItemDecoration
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.statusBarHeight
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetTextInputFragment
import com.anytypeio.anytype.core_utils.ui.TextInputDialogBottomBehaviorApplier
import com.anytypeio.anytype.databinding.FragmentObjectSearchBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.search.ObjectSearchView
import com.anytypeio.anytype.presentation.widgets.SelectWidgetSourceViewModel
import com.anytypeio.anytype.ui.moving.hideProgress
import com.anytypeio.anytype.ui.moving.showProgress
import com.anytypeio.anytype.ui.search.ObjectSearchFragment
import javax.inject.Inject
import timber.log.Timber

class SelectWidgetSourceFragment : BaseBottomSheetTextInputFragment<FragmentObjectSearchBinding>(),
    TextInputDialogBottomBehaviorApplier.OnDialogCancelListener {

    private val ctx: Id get() = arg(CTX_KEY)
    private val widget: Id get() = arg(WIDGET_ID_KEY)
    private val target: Id? get() = argOrNull(TARGET_KEY)
    private val source: Id get() = arg(WIDGET_SOURCE_KEY)
    private val type: Int get() = arg(WIDGET_TYPE_KEY)
    private val forExistingWidget: Boolean? get() = argOrNull(FLOW_EXISTING_WIDGET)
    private val isInEditMode: Boolean get() = arg(IS_IN_EDIT_MODE_KEY)

    private val vm by viewModels<SelectWidgetSourceViewModel> { factory }

    @Inject
    lateinit var factory: SelectWidgetSourceViewModel.Factory

    private lateinit var clearSearchText: View
    private lateinit var filterInputField: EditText

    override val textInput: EditText get() = binding.searchView.root.findViewById(R.id.filterInputField)

    private val selectWidgetSourceAdapter by lazy {
        DefaultObjectViewAdapter(
            onDefaultObjectClicked = vm::onObjectClicked,
            onBundledWidgetSourceClicked = vm::onBundledWidgetSourceClicked
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTransparent()

        TextInputDialogBottomBehaviorApplier(
            attachView = binding.sheet,
            textInput = textInput,
            dialogCancelListener = this
        ).apply()

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

    override fun onDialogCancelled() {
        findNavController().popBackStack()
    }

    override fun onStart() {
        super.onStart()
        if (forExistingWidget == true) {
            vm.onStartWithExistingWidget(
                ctx = ctx,
                source = source,
                widget = widget,
                type = type,
                isInEditMode = isInEditMode
            )
        } else {
            vm.onStartWithNewWidget(
                target = target,
                isInEditMode = isInEditMode
            )
        }
        with(lifecycleScope) {
            jobs += subscribe(vm.isDismissed) { isDismissed ->
                if (isDismissed) dismiss()
            }
        }
        skipCollapsed()
        expand()
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
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
            addItemDecoration(
                SpacingItemDecoration(
                    lastItemSpacingBottom = resources.getDimension(R.dimen.dp_120).toInt()
                )
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
        /**
         * Flow for selecting source for already existing widget.
         * If set to false, this screen is used for selecting source for new widget.
         */
        private const val FLOW_EXISTING_WIDGET = "arg.select-widget-source.flow-existing-widget"
        private const val CTX_KEY = "arg.select-widget-source.ctx"
        private const val WIDGET_ID_KEY = "arg.select-widget-source.widget-id"
        private const val WIDGET_TYPE_KEY = "arg.select-widget-source.widget-type"
        private const val WIDGET_SOURCE_KEY = "arg.select-widget-source.widget-source"
        private const val TARGET_KEY = "arg.select-widget-source.target"
        private const val IS_IN_EDIT_MODE_KEY = "arg.select-widget-source.is-in-edit-mode"
        fun args(
            ctx: Id,
            widget: Id,
            source: Id,
            type: Int,
            isInEditMode: Boolean
        ) = bundleOf(
            CTX_KEY to ctx,
            WIDGET_ID_KEY to widget,
            WIDGET_SOURCE_KEY to source,
            WIDGET_TYPE_KEY to type,
            TARGET_KEY to null,
            FLOW_EXISTING_WIDGET to true,
            IS_IN_EDIT_MODE_KEY to isInEditMode
        )

        /**
         * Flow for selecting source for new widget.
         */
        fun args(
            target: Id?,
            isInEditMode: Boolean
        ) = bundleOf(
            TARGET_KEY to target,
            IS_IN_EDIT_MODE_KEY to isInEditMode
        )
    }
}
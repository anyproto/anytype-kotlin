package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.sets.RelationValueAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.focusChanges
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseDialogFragment
import com.anytypeio.anytype.presentation.relations.AddObjectRelationValueViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.relation_option_value_add_fragment.*

abstract class RelationOptionValueBaseAddFragment : BaseDialogFragment() {

    private val behavior get() = BottomSheetBehavior.from(sheet)

    val ctx get() = argString(CTX_KEY)
    val relation get() = argString(RELATION_KEY)
    val target get() = argString(TARGET_KEY)
    val flow get() = arg<Int>(FLOW_KEY)
    val dataview get() = argString(DATAVIEW_KEY)
    val viewer get() = argString(VIEWER_KEY)

    private lateinit var searchRelationInput: EditText
    private lateinit var clearSearchText: View

    abstract val vm: AddObjectRelationValueViewModel

    private val editCellTagAdapter by lazy {
        RelationValueAdapter(
            onCreateOptionClicked = { name -> onCreateOptionClicked(name) },
            onTagClicked = { tag -> vm.onTagClicked(tag) },
            onStatusClicked = { status -> onStatusClicked(status) },
            onRemoveTagClicked = {},
            onRemoveStatusClicked = {},
            onObjectClicked = {},
            onRemoveObjectClicked = {},
            onFileClicked = {},
            onRemoveFileClicked = {}
        )
    }

    abstract fun onStatusClicked(status: RelationValueBaseViewModel.RelationValueView.Status)
    abstract fun onCreateOptionClicked(name: String)
    abstract fun onAddButtonClicked()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.relation_option_value_add_fragment, container, false).apply {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = editCellTagAdapter
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_relations))
                }
            )
        }
        searchRelationInput = searchBar.findViewById(R.id.filterInputField)
        clearSearchText = searchBar.findViewById(R.id.clearSearchText)
        clearSearchText.setOnClickListener {
            searchRelationInput.setText("")
            clearSearchText.invisible()
        }
        with(lifecycleScope) {
            subscribe(view.clicks()) { dismiss() }
            subscribe(btnAdd.clicks()) { onAddButtonClicked() }
            subscribe(searchRelationInput.textChanges()) {
                if (it.isEmpty()) clearSearchText.invisible() else clearSearchText.visible()
                vm.onFilterInputChanged(it.toString())
            }
            subscribe(searchRelationInput.focusChanges()) { hasFocus ->
                if (hasFocus) behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        behavior.apply {
            skipCollapsed = true
            addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) dismiss()
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        if (vm.isMultiple.value) {
                            if (slideOffset < 0)
                                btnAddContainer.gone()
                            else
                                btnAddContainer.visible()
                        }
                    }
                }
            )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(vm.ui) { editCellTagAdapter.update(it) }
            subscribe(vm.counter) { tvSelectionCounter.text = it.toString() }
            subscribe(vm.isAddButtonVisible) { isVisible ->
                if (!isVisible) btnAddContainer.gone() else btnAddContainer.visible()
            }
            subscribe(vm.isDismissed) { isDismissed ->
                if (isDismissed) {
                    proceedWithExiting(dismissParent = false)
                }
            }
            subscribe(vm.isParentDismissed) { isParentDismissed ->
                if (isParentDismissed) {
                    proceedWithExiting(dismissParent = true)
                }
            }
            subscribe(vm.isMultiple) { isMultiple ->
                if (isMultiple) {
                    recycler.updatePadding(
                        bottom = dimen(R.dimen.multiple_option_value_bottom_list_margin)
                    )
                    searchRelationInput.setHint(R.string.choose_options)
                } else {
                    recycler.updatePadding(
                        bottom = dimen(R.dimen.single_option_value_bottom_list_margin)
                    )
                    searchRelationInput.setHint(R.string.choose_option)
                }
            }
        }
    }

    open fun proceedWithExiting(dismissParent: Boolean = false) {
        searchRelationInput.apply {
            clearFocus()
            hideKeyboard()
        }
        if (dismissParent) {
            (parentFragment as? BottomSheetDialogFragment)?.dismiss()
        } else {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        setupAppearance()
        vm.onStart(
            target = target,
            relationId = relation
        )
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    private fun setupAppearance() {
        dialog?.window?.apply {
            setGravity(Gravity.BOTTOM)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.transparent)
            setWindowAnimations(R.style.DefaultBottomDialogAnimation)
        }
    }

    companion object {
        const val CTX_KEY = "arg.add-object-relation-value.ctx"
        const val RELATION_KEY = "arg.add-object-relation-value.relation"
        const val TARGET_KEY = "arg.add-object-relation-value.target"
        const val FLOW_KEY = "arg.add-object-relation-value.flow"
        const val DATAVIEW_KEY = "arg.add-object-relation-value.data-view"
        const val VIEWER_KEY = "arg.add-object-relation-value.viewer"
    }
}
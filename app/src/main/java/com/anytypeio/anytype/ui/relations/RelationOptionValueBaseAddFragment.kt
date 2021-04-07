package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.sets.RelationValueAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.presentation.relations.AddObjectRelationValueViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel
import kotlinx.android.synthetic.main.add_object_relation_value_fragment.*

abstract class RelationOptionValueBaseAddFragment : BaseBottomSheetFragment() {

    val ctx get() = argString(CTX_KEY)
    val relation get() = argString(RELATION_KEY)
    val target get() = argString(TARGET_KEY)
    val flow get() = arg<Int>(FLOW_KEY)
    val dataview get() = argString(DATAVIEW_KEY)
    val viewer get() = argString(VIEWER_KEY)

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
    ): View? = inflater.inflate(R.layout.add_object_relation_value_fragment, container, false)

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
        with(lifecycleScope) {
            subscribe(btnAdd.clicks()) {
                onAddButtonClicked()
            }
            subscribe(filterInput.textChanges()) { vm.onFilterInputChanged(it.toString()) }
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
            subscribe(vm.isDimissed) { isDismissed ->
                if (isDismissed) {
                    proceedWithExiting()
                }
            }
            subscribe(vm.isMultiple) { isMultiple ->
                if (isMultiple)
                    filterInput.setHint(R.string.choose_options)
                else
                    filterInput.setHint(R.string.choose_option)
            }
        }
    }

    open fun proceedWithExiting() {
        filterInput.apply {
            clearFocus()
            hideKeyboard()
        }
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(
            target = target,
            relationId = relation
        )
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
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
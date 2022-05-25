package com.anytypeio.anytype.ui.sets.modals

import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.features.sets.SortingRelationPickerAdapter
import com.anytypeio.anytype.core_utils.ext.argList
import com.anytypeio.anytype.core_utils.ext.argStringOrNull
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.SortingExpression
import com.anytypeio.anytype.ui.sets.ViewerSortByFragment

class PickSortingKeyFragment : BaseDialogListFragment() {

    private val selected get() = argStringOrNull(ARG_SELECTED)

    private val relations get() = argList<SimpleRelationView>(ARG_RELATIONS)
    private val sorts get() = argList<SortingExpression>(ARG_SORTS)

    override fun setAdapter(recyclerView: RecyclerView) {
        recyclerView.adapter = SortingRelationPickerAdapter(
            sorts = sorts,
            relations = relations,
            relationSelectedKey = selected,
            click = { keySelected, keyNew ->
                withParent<ViewerSortByFragment> {
                    if (keySelected == null) {
                        onAddSortKey(keyNew)
                    } else {
                        onReplaceSortKey(
                            keySelected = keySelected,
                            keyNew = keyNew
                        )
                    }
                }
                dismiss()
            }
        )
    }

    companion object {
        const val ARG_SELECTED = "arg.viewer.sorts.key.selected"
        const val ARG_RELATIONS = "arg.viewer.sorts.key.relations"
        const val ARG_SORTS = "arg.viewer.sorts.key.sorts"

        fun new(
            selected: String?,
            relations: List<SimpleRelationView>,
            sorts: ArrayList<SortingExpression>
        ) =
            PickSortingKeyFragment().apply {
                arguments = bundleOf(
                    ARG_SELECTED to selected,
                    ARG_RELATIONS to relations,
                    ARG_SORTS to sorts
                )
            }
    }
}
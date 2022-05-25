package com.anytypeio.anytype.ui.sets.modals

import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.features.sets.PickSortingTypeAdapter
import com.anytypeio.anytype.core_utils.ext.argInt
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.ui.sets.ViewerSortByFragment

class PickSortingTypeFragment : BaseDialogListFragment() {

    private val keySelected get() = argString(ARG_KEY_SELECTED)
    private val typeSelected get() = argInt(ARG_TYPE_SELECTED)
    private val types = listOf(Viewer.SortType.ASC, Viewer.SortType.DESC)

    override fun setAdapter(recyclerView: RecyclerView) {
        recyclerView.adapter = PickSortingTypeAdapter(
            items = types,
            typeSelected = typeSelected,
            keySelected = keySelected,
            click = { key, type ->
                withParent<ViewerSortByFragment> {
                    onPickSortType(key, type)
                }
                dismiss()
            }
        )
    }

    companion object {
        const val ARG_KEY_SELECTED = "arg.viewer.sorts.type.key"
        const val ARG_TYPE_SELECTED = "arg.viewer.sorts.type.selected"

        fun new(key: String, type: Int) = PickSortingTypeFragment().apply {
            arguments = bundleOf(
                ARG_KEY_SELECTED to key,
                ARG_TYPE_SELECTED to type
            )
        }
    }
}
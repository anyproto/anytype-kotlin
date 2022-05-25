package com.anytypeio.anytype.ui.sets.modals

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.tools.BottomSheetSharedTransition
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.sets.model.FilterExpression
import com.anytypeio.anytype.presentation.sets.model.SortingExpression
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.ui.sets.ViewerFilterFragment
import com.anytypeio.anytype.ui.sets.ViewerSortByFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ViewerBottomSheetRootFragment : BottomSheetDialogFragment() {

    private val ctx get() = argString(CONTEXT_ID_KEY)
    private val viewer get() = argString(VIEWER_ID_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_viewer_bottom_sheet_root, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager
            .beginTransaction()
            .add(R.id.container, ViewerCustomizeFragment.new(ctx, viewer))
            .addToBackStack(TAG_ROOT)
            .commit()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
            .also { dialog ->
                dialog.setOnShowListener {
                    val viewBehavior = BottomSheetBehavior.from(
                        dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
                    )
                    setupBehavior(viewBehavior)
                }
            }
    }

    fun transitToFilter() = {}

    fun transitToCustomize() = transitToFragment(
        ViewerCustomizeFragment.new(
            ctx = ctx,
            viewer = viewer
        )
    )

    fun transitToSorting() = transitToFragment(
        ViewerSortByFragment.new(
            ctx = ctx,
            viewer = viewer
        )
    )

    fun transitToRelations() {}

    fun dispatchResultSortsAndDismiss(sorts: List<SortingExpression>) {
        withParent<ObjectSetFragment> { onViewerNewSortsRequest(sorts) }
        dismiss()
    }

    fun dispatchResultFiltersAndDismiss(filters: List<FilterExpression>) {
        withParent<ObjectSetFragment> { onViewerNewFiltersRequest(filters) }
        dismiss()
    }

    private fun transitToFragment(newFragment: Fragment) {
        val currentFragmentRoot = childFragmentManager.fragments[0].requireView()

        childFragmentManager
            .beginTransaction()
            .apply {
                addSharedElement(currentFragmentRoot, currentFragmentRoot.transitionName)
                setReorderingAllowed(true)
                newFragment.sharedElementEnterTransition = BottomSheetSharedTransition()
            }
            .replace(R.id.container, newFragment)
            .addToBackStack(newFragment.javaClass.name)
            .commit()
    }

    private fun setupBehavior(bottomSheetBehavior: BottomSheetBehavior<View>) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.skipCollapsed = true
    }

    // INSTANTIATION

    companion object {

        fun new(ctx: Id, viewer: Id) = ViewerBottomSheetRootFragment().apply {
            arguments = bundleOf(
                CONTEXT_ID_KEY to ctx,
                VIEWER_ID_KEY to viewer
            )
        }

        const val VIEWER_ID_KEY = "arg.viewer.root.viewer_id"
        const val CONTEXT_ID_KEY = "arg.viewer.root.context_id"
        const val TAG_ROOT = "tag.root"
    }
}
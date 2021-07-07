package com.anytypeio.anytype.ui.sets.modals.sort

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.sets.ViewerSortAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.sort.ViewerSortViewModel
import com.anytypeio.anytype.presentation.sets.sort.ViewerSortViewModel.Mode
import kotlinx.android.synthetic.main.fragment_viewer_sort.*
import javax.inject.Inject

open class ViewerSortFragment : BaseBottomSheetFragment() {

    private val ctx: String get() = arg(CTX_KEY)

    @Inject
    lateinit var factory: ViewerSortViewModel.Factory

    private val vm: ViewerSortViewModel by viewModels { factory }

    private val viewerSortAdapter by lazy {
        ViewerSortAdapter(
            onAddViewerSortClicked = { navigateToSelectSort() },
            onViewerSortClicked = { view ->
                if (view.mode == Mode.READ) navigateToChangeSort(view.relation)
            },
            onRemoveViewerSortClicked = { vm.onRemoveViewerSortClicked(ctx, it) }
        )
    }

    private lateinit var dividerItem : DividerItemDecoration
    private lateinit var dividerItemEdit: DividerItemDecoration

    private fun navigateToSelectSort() {
        val fr = SelectSortRelationFragment.new(ctx)
        fr.show(parentFragmentManager, null)
    }

    private fun navigateToChangeSort(relation: Id) {
        val fr = ModifyViewerSortFragment.new(ctx = ctx, relation = relation)
        fr.show(parentFragmentManager, null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_viewer_sort, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dividerItem = DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
            setDrawable(drawable(R.drawable.decoration_viewer_sort))
        }
        dividerItemEdit = DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
            setDrawable(drawable(R.drawable.decoration_viewer_sort_edit))
        }
        viewerSortRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewerSortAdapter

        }
        with(lifecycleScope) {
            subscribe(btnResetSort.clicks()) { vm.onResetClicked(ctx) }
            subscribe(btnEditSortOrDone.clicks()) {
                if (btnEditSortOrDone.text == getString(R.string.edit))
                    vm.onEditClicked()
                else if (btnEditSortOrDone.text == getString(R.string.done))
                    vm.onDoneClicked()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(vm.mode) { mode ->
                when (mode) {
                    Mode.READ -> {
                        btnEditSortOrDone.setText(R.string.edit)
                        btnResetSort.visible()
                        viewerSortRecycler.apply {
                            removeItemDecoration(dividerItemEdit)
                            addItemDecoration(dividerItem)
                        }
                    }
                    Mode.EDIT -> {
                        btnEditSortOrDone.setText(R.string.done)
                        btnResetSort.invisible()
                        viewerSortRecycler.apply {
                            removeItemDecoration(dividerItem)
                            addItemDecoration(dividerItemEdit)
                        }
                    }
                }
            }
            subscribe(vm.views) { viewerSortAdapter.update(it) }
            subscribe(vm.isDismissed) { isDismissed -> if (isDismissed) dismiss() }
        }
    }

    override fun injectDependencies() {
        componentManager().viewerSortComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().viewerSortComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id): ViewerSortFragment = ViewerSortFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }

        const val CTX_KEY = "arg.viewer-sort.ctx"
    }
}
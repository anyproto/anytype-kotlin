package com.anytypeio.anytype.ui.sets.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.sets.ManageViewerAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.ManageViewerViewModel
import kotlinx.android.synthetic.main.fragment_manage_viewer.*
import javax.inject.Inject

class ManageViewerFragment : BaseBottomSheetFragment() {

    private val manageViewerAdapter by lazy {
        ManageViewerAdapter(
            onViewerClicked = { view -> vm.onViewerClicked(ctx = ctx, view = view) },
            onViewerActionClicked = { view ->
                val dialog = DataViewViewerActionFragment.new(
                    ctx = ctx,
                    viewer = view.id,
                    title = view.name
                )
                dialog.show(parentFragmentManager, null)
            }
        )
    }

    private val ctx: String get() = arg(CTX_KEY)
    private val dataview: String get() = arg(DATA_VIEW_KEY)

    @Inject
    lateinit var factory: ManageViewerViewModel.Factory

    private val vm: ManageViewerViewModel by viewModels { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_manage_viewer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataViewViewerRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = manageViewerAdapter
        }
        with(lifecycleScope) {
            subscribe(btnEditViewers.clicks()) { vm.onViewerEditClicked() }
            subscribe(btnAddNewViewer.clicks()) { navigateToCreateDataViewViewerScreen() }
        }
    }

    private fun navigateToCreateDataViewViewerScreen() {
        val dialog = CreateDataViewViewerFragment.new(
            ctx = ctx,
            target = dataview
        )
        dialog.show(parentFragmentManager, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(vm.toasts) { toast(it) }
            subscribe(vm.views) { manageViewerAdapter.update(it) }
            subscribe(vm.isDismissed) { isDismissed -> if (isDismissed) dismiss() }
            subscribe(vm.isEditEnabled) { isEditEnabled ->
                if (isEditEnabled) {
                    btnEditViewers.setText(R.string.done)
                    btnAddNewViewer.invisible()
                } else {
                    btnEditViewers.setText(R.string.edit)
                    btnAddNewViewer.visible()
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().manageViewerComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().manageViewerComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, dataview: Id): ManageViewerFragment = ManageViewerFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, DATA_VIEW_KEY to dataview)
        }

        const val CTX_KEY = "arg.manage-data-view-viewer.ctx"
        const val DATA_VIEW_KEY = "arg.manage-data-view-viewer.dataview"
    }
}
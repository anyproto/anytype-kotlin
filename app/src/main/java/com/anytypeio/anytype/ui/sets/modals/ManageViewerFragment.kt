package com.anytypeio.anytype.ui.sets.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.sets.ManageViewerDoneAdapter
import com.anytypeio.anytype.core_ui.features.sets.ManageViewerEditAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.tools.DefaultDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.OnStartDragListener
import com.anytypeio.anytype.databinding.FragmentManageViewerBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.ManageViewerViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ManageViewerFragment : BaseBottomSheetFragment<FragmentManageViewerBinding>(),
    OnStartDragListener {

    private val manageViewerAdapter by lazy {
        ManageViewerDoneAdapter(
            onViewerClicked = { view -> vm.onViewerClicked(ctx = ctx, view = view) }
        )
    }

    private val manageViewerEditAdapter by lazy {
        ManageViewerEditAdapter(
            onDragListener = this,
            onButtonMoreClicked = vm::onViewerActionClicked,
            onDeleteView = {
                vm.onDeleteView(
                    ctx = ctx,
                    dv = dv,
                    view = it
                )
            },
            onDeleteActiveView = { toast(R.string.toast_active_view_delete) }
        )
    }

    private val ctx: Id get() = arg(CTX_KEY)
    private val dv: Id get() = arg(DATA_VIEW_KEY)

    @Inject
    lateinit var factory: ManageViewerViewModel.Factory

    private val vm: ManageViewerViewModel by viewModels { factory }

    private val dndItemTouchHelper: ItemTouchHelper by lazy { ItemTouchHelper(dndBehavior) }
    private val dndBehavior by lazy {
        DefaultDragAndDropBehavior(
            onItemMoved = { from, to -> manageViewerEditAdapter.onItemMove(from, to) },
            onItemDropped = { newPosition ->
                vm.onOrderChanged(
                    ctx = ctx,
                    dv = dv,
                    newOrder = manageViewerEditAdapter.order,
                    newPosition = newPosition
                )
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dataViewViewerRecycler.apply {
            layoutManager = LinearLayoutManager(context)
        }
        with(lifecycleScope) {
            subscribe(binding.btnEditViewers.clicks()) { vm.onButtonEditClicked() }
            subscribe(binding.btnAddNewViewer.clicks()) { vm.onButtonAddClicked() }
        }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
    }

    override fun onStart() {
        lifecycleScope.launch {
            jobs += subscribe(vm.commands) { observe(it) }
            jobs += subscribe(vm.toasts) { toast(it) }
            jobs += subscribe(vm.views) {
                manageViewerAdapter.update(it)
                manageViewerEditAdapter.update(it)
            }
            jobs += subscribe(vm.isDismissed) { isDismissed ->
                if (isDismissed) dismiss()
            }
            jobs += subscribe(vm.isEditEnabled) { isEditEnabled ->
                if (isEditEnabled) {
                    with(binding) {
                        btnEditViewers.setText(R.string.done)
                        btnAddNewViewer.invisible()
                        dataViewViewerRecycler.apply {
                            adapter = manageViewerEditAdapter
                            dndItemTouchHelper.attachToRecyclerView(this)

                        }
                    }
                } else {
                    with(binding) {
                        btnEditViewers.setText(R.string.edit)
                        btnAddNewViewer.visible()
                        dataViewViewerRecycler.apply {
                            adapter = manageViewerAdapter
                            dndItemTouchHelper.attachToRecyclerView(null)
                        }
                    }
                }
            }
        }
        super.onStart()
    }

    private fun observe(command: ManageViewerViewModel.Command) {
        when (command) {
            is ManageViewerViewModel.Command.OpenEditScreen -> {
                val dialog = EditDataViewViewerFragment.new(
                    ctx = ctx,
                    viewer = command.id
                )
                dialog.show(parentFragmentManager, null)
            }
            ManageViewerViewModel.Command.OpenCreateScreen -> {
                val dialog = CreateDataViewViewerFragment.new(
                    ctx = ctx,
                    target = dv
                )
                dialog.show(parentFragmentManager, null)
            }
        }
    }

    override fun injectDependencies() {
        componentManager().manageViewerComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().manageViewerComponent.release(ctx)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentManageViewerBinding = FragmentManageViewerBinding.inflate(
        inflater, container, false
    )

    companion object {
        fun new(ctx: Id, dv: Id): ManageViewerFragment = ManageViewerFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, DATA_VIEW_KEY to dv)
        }

        const val CTX_KEY = "arg.manage-data-view-viewer.ctx"
        const val DATA_VIEW_KEY = "arg.manage-data-view-viewer.dataview"
    }
}
package com.anytypeio.anytype.ui.sets.modals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.features.dataview.ViewerModifyOrderAdapter
import com.anytypeio.anytype.core_ui.features.dataview.ViewerRelationsAdapter
import com.anytypeio.anytype.core_ui.layout.DividerVerticalItemDecoration
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.tools.DefaultDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.OnStartDragListener
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.ViewerRelationsViewModel
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.ui.relations.RelationAddToDataViewFragment
import com.anytypeio.anytype.ui.sets.modals.viewer.ViewerCardSizeSelectFragment
import com.anytypeio.anytype.ui.sets.modals.viewer.ViewerImagePreviewSelectFragment
import kotlinx.android.synthetic.main.fragment_viewer_relations_list.*
import javax.inject.Inject

class ViewerRelationsFragment : BaseBottomSheetFragment(), OnStartDragListener {

    @Inject
    lateinit var factory: ViewerRelationsViewModel.Factory
    private val vm: ViewerRelationsViewModel by viewModels { factory }

    private val ctx get() = arg<String>(CTX_KEY)
    private val viewer get() = arg<String>(VIEWER_KEY)
    private val dv get() = arg<String>(DV_KEY)

    private val listAdapter by lazy {
        ViewerRelationsAdapter(
            onSwitchClick = { vm.onSwitchClicked(ctx, it) },
            onSettingToggleChanged = { setting, isChecked ->
                vm.onSettingToggleChanged(
                    ctx = ctx,
                    toggle = setting,
                    isChecked = isChecked
                )
            },
            onViewerCardSettingClicked = {
                findNavController()
                    .navigate(
                        R.id.viewerCardSizeSelectFragment,
                        bundleOf(ViewerCardSizeSelectFragment.CTX_KEY to ctx)
                    )
            },
            onViewerImagePreviewSettingClicked = {
                findNavController()
                    .navigate(
                        R.id.viewerImagePreviewSelectFragment,
                        bundleOf(ViewerImagePreviewSelectFragment.CTX_KEY to ctx)
                    )
            }
        )
    }
    private val editAdapter by lazy {
        ViewerModifyOrderAdapter(
            dragListener = this,
            onItemClick = {
                toast("Not implemented yet")
            },
            onDeleteClick = { view ->
                vm.onDeleteClicked(
                    ctx = ctx,
                    item = view
                )
            },
            onSettingToggleChanged = { setting, isChecked ->
                vm.onSettingToggleChanged(
                    ctx = ctx,
                    toggle = setting,
                    isChecked = isChecked
                )
            }
        )
    }
    private val dndItemTouchHelper: ItemTouchHelper by lazy { ItemTouchHelper(dndBehavior) }
    private val dndBehavior by lazy {
        object : DefaultDragAndDropBehavior(
            onItemMoved = { from, to -> editAdapter.onItemMove(from, to) },
            onItemDropped = {
                vm.onOrderChanged(ctx, editAdapter.items)
            }
        ) {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return if (viewHolder is ViewerModifyOrderAdapter.RelationViewHolder) {
                    makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
                } else {
                    0
                }
            }
        }
    }

    private lateinit var itemDivider: DividerVerticalItemDecoration
    private lateinit var itemDividerEdit: DividerVerticalItemDecoration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_viewer_relations_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemDivider = DividerVerticalItemDecoration(
            divider = requireContext().drawable(R.drawable.divider_relation_layer),
            isShowInLastItem = false
        )
        itemDividerEdit = DividerVerticalItemDecoration(
            divider = requireContext().drawable(R.drawable.divider_relation_layer_edit),
            isShowInLastItem = false
        )
        recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
        with(lifecycleScope) {
            subscribe(editBtn.clicks()) { vm.onEditButtonClicked() }
            subscribe(doneBtn.clicks()) { vm.onDoneButtonClicked() }
            subscribe(iconAdd.clicks()) {
                RelationAddToDataViewFragment.new(
                    ctx = ctx,
                    dv = dv,
                    viewer = viewer
                ).show(childFragmentManager, null)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(vm.toasts) { toast(it) }
            subscribe(vm.views) {
                listAdapter.update(it)
                editAdapter.update(it)
            }
            subscribe(vm.screenState) { render(it) }
        }
    }

    private fun render(state: ViewerRelationsViewModel.ScreenState) {
        when (state) {
            ViewerRelationsViewModel.ScreenState.LIST -> {
                recycler.apply {
                    dndItemTouchHelper.attachToRecyclerView(null)
                }
                iconAdd.visible()
                editBtn.visible()
                doneBtn.invisible()
                recycler.apply {
                    adapter = listAdapter
                    removeItemDecoration(itemDividerEdit)
                    addItemDecoration(itemDivider)
                }
            }
            ViewerRelationsViewModel.ScreenState.EDIT -> {
                recycler.apply {
                    dndItemTouchHelper.attachToRecyclerView(this)
                }
                iconAdd.invisible()
                doneBtn.visible()
                editBtn.invisible()
                recycler.apply {
                    adapter = editAdapter
                    removeItemDecoration(itemDivider)
                    addItemDecoration(itemDividerEdit)
                }
            }
        }
    }

    private fun navigateToOptions(item: SimpleRelationView) {
        val dialog = ViewerRelationOptionFragment.new(
            ctx = ctx,
            relation = item.key,
            title = item.title,
            format = item.format
        )
        dialog.show(parentFragmentManager, null)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        dndItemTouchHelper.startDrag(viewHolder)
    }

    override fun injectDependencies() {
        componentManager().viewerRelationsComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().viewerRelationsComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, dv: Id, viewer: Id) = ViewerRelationsFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, DV_KEY to dv, VIEWER_KEY to viewer)
        }

        private const val CTX_KEY = "arg.viewer-relation-list.ctx"
        private const val DV_KEY = "arg.viewer-relation-list.dv"
        private const val VIEWER_KEY = "arg.viewer-relation-list.viewer"
    }
}
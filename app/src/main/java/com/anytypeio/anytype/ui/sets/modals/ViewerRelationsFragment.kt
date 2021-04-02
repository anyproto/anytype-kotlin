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
import kotlinx.android.synthetic.main.fragment_viewer_relations_list.*
import javax.inject.Inject

class ViewerRelationsFragment : BaseBottomSheetFragment(), OnStartDragListener {

    @Inject
    lateinit var factory: ViewerRelationsViewModel.Factory
    private val vm: ViewerRelationsViewModel by viewModels { factory }

    private val ctx get() = arg<String>(CONTEXT_KEY)

    private val listAdapter by lazy {
        ViewerRelationsAdapter(
            onSwitchClick = { vm.onSwitchClicked(ctx, it) }
        )
    }
    private val editAdapter by lazy {
        ViewerModifyOrderAdapter(
            dragListener = this,
            onMoreClick = { navigateToOptions(it) }
        )
    }
    private val dndItemTouchHelper: ItemTouchHelper by lazy { ItemTouchHelper(dndBehavior) }
    private val dndBehavior by lazy {
        DefaultDragAndDropBehavior(
            onItemMoved = { from, to -> editAdapter.onItemMove(from, to) },
            onItemDropped = { vm.onOrderChanged(ctx, editAdapter.order) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_viewer_relations_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                DividerVerticalItemDecoration(
                    divider = requireContext().drawable(R.drawable.divider_relation_layer),
                    isShowInLastItem = false
                )
            )
        }
        with(lifecycleScope) {
            subscribe(editBtn.clicks()) { vm.onEditButtonClicked() }
            subscribe(doneBtn.clicks()) { vm.onDoneButtonClicked() }
            subscribe(iconAdd.clicks()) { toast("Not implemented yet")}
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

        fun new(ctx: Id) = ViewerRelationsFragment().apply {
            arguments = bundleOf(
                CONTEXT_KEY to ctx
            )
        }

        private const val CONTEXT_KEY = "arg.viewer-relation-list.ctx"
    }
}
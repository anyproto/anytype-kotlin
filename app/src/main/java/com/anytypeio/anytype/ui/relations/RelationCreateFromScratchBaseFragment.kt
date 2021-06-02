package com.anytypeio.anytype.ui.relations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.relations.RelationFormatAdapter
import com.anytypeio.anytype.core_ui.features.relations.RelationNameInputAdapter
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.RelationCreateFromScratchBaseViewModel
import com.anytypeio.anytype.presentation.relations.RelationCreateFromScratchForDataViewViewModel
import com.anytypeio.anytype.presentation.relations.RelationCreateFromScratchForObjectViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_relation_create_from_scratch.*
import javax.inject.Inject

abstract class RelationCreateFromScratchBaseFragment : BaseBottomSheetFragment() {

    abstract val vm: RelationCreateFromScratchBaseViewModel

    protected val ctx get() = arg<Id>(CTX_KEY)

    private val nameInputAdapter = RelationNameInputAdapter {
        vm.onNameChanged(it)
    }

    private val relationAdapter = RelationFormatAdapter(
        onItemClick = { relation -> vm.onRelationFormatClicked(relation.format) }
    )

    private val concatAdapter = ConcatAdapter(nameInputAdapter, relationAdapter)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_relation_create_from_scratch, container, false).apply {
            dialog?.setOnShowListener { dg ->
                val bottomSheet = (dg as? BottomSheetDialog)?.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet
                )
                bottomSheet?.let {
                    val behavior = BottomSheetBehavior.from(it)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.skipCollapsed = true
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvCreateRelationFromScratch.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = concatAdapter
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_relations_with_padding))
                }
            )
        }
        btnAction.setOnClickListener { onCreateRelationClicked() }

        with(lifecycleScope) {
            subscribe(vm.views) { relationAdapter.submitList(it) }
            subscribe(vm.isActionButtonEnabled) { btnAction.isEnabled = it }
            subscribe(vm.isDismissed) { isDismissed ->
                if (isDismissed) {
                    // Refact parent dismissing
                    parentFragment?.let { parent ->
                        if (parent is RelationAddBaseFragment) parent.dismiss()
                    }
                    dismiss()
                }
            }
            subscribe(vm.toasts) { toast(it) }
        }
    }

    abstract fun onCreateRelationClicked()

    companion object {
        const val CTX_KEY = "arg.relation-create-from-scratch.ctx"
    }

}

class RelationCreateFromScratchForObjectFragment : RelationCreateFromScratchBaseFragment() {

    @Inject
    lateinit var factory: RelationCreateFromScratchForObjectViewModel.Factory
    override val vm: RelationCreateFromScratchForObjectViewModel by viewModels { factory }

    override fun onCreateRelationClicked() {
        vm.onCreateRelationClicked(ctx)
    }

    override fun injectDependencies() {
        componentManager().relationCreateFromScratchForObjectComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().relationCreateFromScratchForObjectComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id) = RelationCreateFromScratchForObjectFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }
    }
}

class RelationCreateFromScratchForDataViewFragment : RelationCreateFromScratchBaseFragment() {

    private val dv get() = arg<Id>(DV_KEY)

    @Inject
    lateinit var factory: RelationCreateFromScratchForDataViewViewModel.Factory
    override val vm: RelationCreateFromScratchForDataViewViewModel by viewModels { factory }

    override fun onCreateRelationClicked() {
        vm.onCreateRelationClicked(ctx = ctx, dv = dv)
    }

    override fun injectDependencies() {
        componentManager().relationCreateFromScratchForDataViewComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().relationCreateFromScratchForDataViewComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, dv: Id) = RelationCreateFromScratchForDataViewFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, DV_KEY to dv)
        }
        const val DV_KEY = "arg.relation-create-from-scratch-for-data-view.ctx"
    }
}
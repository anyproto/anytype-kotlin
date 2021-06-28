package com.anytypeio.anytype.ui.relations

import android.content.res.Resources
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
import com.anytypeio.anytype.core_ui.features.relations.RelationAddAdapter
import com.anytypeio.anytype.core_ui.features.relations.RelationAddHeaderAdapter
import com.anytypeio.anytype.core_ui.reactive.focusChanges
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.RelationAddBaseViewModel
import com.anytypeio.anytype.presentation.relations.RelationAddToDataViewViewModel
import com.anytypeio.anytype.presentation.relations.RelationAddToObjectViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_relation_add.*
import javax.inject.Inject


abstract class RelationAddBaseFragment : BaseBottomSheetFragment() {

    abstract val vm: RelationAddBaseViewModel

    protected val ctx get() = arg<Id>(CTX_KEY)

    protected val createFromScratchAdapter = RelationAddHeaderAdapter {
        onCreateFromScratchClicked()
    }

    private val relationAdapter = RelationAddAdapter { relation ->
        onRelationSelected(ctx = ctx, relation = relation.id)
    }

    private val concatAdapter = ConcatAdapter(createFromScratchAdapter, relationAdapter)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_relation_add, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFullHeight()
        relationAddRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = concatAdapter
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_relations_with_padding))
                }
            )
        }
        with(lifecycleScope) {
            subscribe(searchRelationInput.focusChanges()) { hasFocus -> if (hasFocus) expand(view) }
            subscribe(searchRelationInput.textChanges()) {
                createFromScratchAdapter.query = it.toString()
                vm.onQueryChanged(it.toString())
            }
            subscribe(vm.results) { relationAdapter.submitList(it) }
            subscribe(vm.isDismissed) { isDismissed -> if (isDismissed) dismiss() }
            subscribe(vm.toasts) { toast(it) }
        }
    }

    private fun setupFullHeight() {
        val lp = (root.layoutParams as FrameLayout.LayoutParams)
        lp.height = Resources.getSystem().displayMetrics.heightPixels - requireActivity().statusBarHeight
        root.layoutParams = lp
    }

    private fun expand(root: View) {
        BottomSheetBehavior.from(root.parent as View).state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(ctx)
    }

    abstract fun onRelationSelected(ctx: Id, relation: Id)
    abstract fun onCreateFromScratchClicked()

    companion object {
        const val CTX_KEY = "arg.relation-add.ctx"
    }
}

class RelationAddToObjectFragment : RelationAddBaseFragment() {

    @Inject
    lateinit var factory: RelationAddToObjectViewModel.Factory
    override val vm: RelationAddToObjectViewModel by viewModels { factory }

    override fun onRelationSelected(ctx: Id, relation: Id) {
        vm.onRelationSelected(ctx = ctx, relation = relation)
    }

    override fun onCreateFromScratchClicked() {
        RelationCreateFromScratchForObjectFragment
            .new(
                ctx = ctx,
                query = createFromScratchAdapter.query
            )
            .show(childFragmentManager, null)
    }

    override fun injectDependencies() {
        componentManager().relationAddToObjectComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().relationAddToObjectComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id): RelationAddToObjectFragment = RelationAddToObjectFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }
    }
}

class RelationAddToDataViewFragment : RelationAddBaseFragment() {

    private val dv get() = arg<Id>(DV_KEY)

    @Inject
    lateinit var factory: RelationAddToDataViewViewModel.Factory
    override val vm: RelationAddToDataViewViewModel by viewModels { factory }

    override fun onRelationSelected(ctx: Id, relation: Id) {
        vm.onRelationSelected(ctx = ctx, relation = relation, dv = dv)
    }

    override fun onCreateFromScratchClicked() {
        RelationCreateFromScratchForDataViewFragment
            .new(
                ctx = ctx,
                dv = dv,
                query = createFromScratchAdapter.query
            )
            .show(childFragmentManager, null)
    }

    override fun injectDependencies() {
        componentManager().relationAddToDataViewComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().relationAddToDataViewComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, dv: Id, viewer: Id): RelationAddToDataViewFragment =
            RelationAddToDataViewFragment().apply {
                arguments = bundleOf(
                    CTX_KEY to ctx,
                    DV_KEY to dv,
                    VIEWER_KEY to viewer
                )
            }

        private const val DV_KEY = "arg.relation-add-to-data-view.dv"
        private const val VIEWER_KEY = "arg.relation-add-to-data-view.viewer"
    }
}
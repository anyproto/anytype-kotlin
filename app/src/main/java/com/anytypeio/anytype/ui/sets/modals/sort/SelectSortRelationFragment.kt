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
import com.anytypeio.anytype.core_ui.features.sets.SearchRelationAdapter
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.drawable
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.SelectSortRelationViewModel
import kotlinx.android.synthetic.main.fragment_select_sort_or_filter_relation.*
import javax.inject.Inject

class SelectSortRelationFragment : BaseBottomSheetFragment() {

    private val ctx: String get() = arg(CTX_KEY)

    private val searchRelationAdapter by lazy {
        SearchRelationAdapter { relation ->
            vm.onRelationClicked(ctx = ctx, relation = relation)
        }
    }

    @Inject
    lateinit var factory: SelectSortRelationViewModel.Factory

    private val vm: SelectSortRelationViewModel by viewModels { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_select_sort_or_filter_relation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchRelationRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchRelationAdapter
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(drawable(R.drawable.divider_sort_or_filter_relation))
                }
            )
        }
        with(lifecycleScope) {
            subscribe(searchRelationInput.textChanges()) { vm.onSearchQueryChanged(it.toString()) }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(lifecycleScope) {
            subscribe(vm.views) { searchRelationAdapter.update(it) }
            subscribe(vm.isDismissed) { isDismissed -> if (isDismissed) dismiss() }
        }
    }

    override fun injectDependencies() {
        componentManager().selectSortRelationComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectSortRelationComponent.release(ctx)
    }

    companion object {

        fun new(ctx: Id): SelectSortRelationFragment = SelectSortRelationFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }

        const val CTX_KEY = "arg.select-sort-relation.ctx"
    }
}

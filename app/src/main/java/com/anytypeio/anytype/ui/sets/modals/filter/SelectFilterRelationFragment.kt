package com.anytypeio.anytype.ui.sets.modals.filter

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.withParent
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sets.SearchRelationViewModel
import com.anytypeio.anytype.presentation.sets.SelectFilterRelationViewModel
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.ui.sets.modals.search.SearchRelationFragment
import javax.inject.Inject

class SelectFilterRelationFragment : SearchRelationFragment() {

    override val ctx: String get() = arg(CTX_KEY)
    override val viewer: String get() = arg(VIEWER_ID_KEY)

    @Inject
    lateinit var factory: SelectFilterRelationViewModel.Factory

    override val vm: SearchRelationViewModel by viewModels { factory }

    override fun onRelationClicked(ctx: Id, relation: SimpleRelationView) {
        withParent<CreateFilterFlow> { onRelationSelected(ctx, relation) }
    }

    override fun injectDependencies() {
        componentManager().selectFilterRelationComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectFilterRelationComponent.release(ctx)
    }

    companion object {
        fun new(ctx: Id, viewerId: Id): SelectFilterRelationFragment = SelectFilterRelationFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx, VIEWER_ID_KEY to viewerId)
        }

        const val CTX_KEY = "arg.select-filter-relation.ctx"
        const val VIEWER_ID_KEY = "arg.select-filter-relation.viewer"
    }
}
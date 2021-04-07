package com.anytypeio.anytype.ui.relations

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.RelationOptionValueDVAddViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel
import javax.inject.Inject

open class RelationOptionValueDVAddFragment : RelationOptionValueBaseAddFragment() {

    @Inject
    lateinit var factory: RelationOptionValueDVAddViewModel.Factory
    override val vm: RelationOptionValueDVAddViewModel by viewModels { factory }

    override fun onStatusClicked(status: RelationValueBaseViewModel.RelationValueView.Status) {
        vm.onAddObjectSetStatusClicked(
            ctx = ctx,
            obj = target,
            dataview = dataview,
            viewer = viewer,
            relation = relation,
            status = status
        )
    }

    override fun onCreateOptionClicked(name: String) {
        vm.onCreateDataViewRelationOptionClicked(
            ctx = ctx,
            relation = relation,
            name = name,
            dataview = dataview,
            viewer = viewer,
            target = target
        )
    }

    override fun onAddButtonClicked() {
        vm.onAddSelectedValuesToDataViewClicked(
            ctx = ctx,
            viewer = viewer,
            target = target,
            relation = relation,
            dataview = dataview
        )
    }

    override fun injectDependencies() {
        componentManager().addObjectSetObjectRelationValueComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().addObjectSetObjectRelationValueComponent.release(ctx)
    }

    companion object {
        fun new(
            ctx: Id,
            target: Id,
            relation: Id,
            dataview: Id,
            viewer: Id
        ) = RelationOptionValueDVAddFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                TARGET_KEY to target,
                RELATION_KEY to relation,
                DATAVIEW_KEY to dataview,
                VIEWER_KEY to viewer
            )
        }
    }
}
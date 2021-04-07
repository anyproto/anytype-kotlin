package com.anytypeio.anytype.ui.relations

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.RelationOptionValueAddViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel
import javax.inject.Inject

class RelationOptionValueAddFragment : RelationOptionValueBaseAddFragment() {

    @Inject
    lateinit var factory: RelationOptionValueAddViewModel.Factory
    override val vm: RelationOptionValueAddViewModel by viewModels { factory }

    override fun onStatusClicked(status: RelationValueBaseViewModel.RelationValueView.Status) {
        vm.onAddObjectStatusClicked(
            ctx = ctx,
            relation = relation,
            status = status
        )
    }

    override fun onCreateOptionClicked(name: String) {
        vm.onCreateObjectRelationOptionClicked(
            ctx = ctx,
            relation = relation,
            obj = target,
            name = name
        )
    }

    override fun onAddButtonClicked() {
        vm.onAddSelectedValuesToObjectClicked(
            ctx = ctx,
            obj = target,
            relation = relation,
        )
    }

    override fun injectDependencies() {
        componentManager().addObjectObjectRelationValueComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().addObjectObjectRelationValueComponent.release(ctx)
    }

    companion object {
        fun new(
            ctx: Id,
            objectId: Id,
            relationId: Id
        ) = RelationOptionValueAddFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                TARGET_KEY to objectId,
                RELATION_KEY to relationId
            )
        }
    }
}
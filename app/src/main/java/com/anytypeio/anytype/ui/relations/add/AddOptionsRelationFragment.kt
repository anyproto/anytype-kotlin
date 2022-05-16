package com.anytypeio.anytype.ui.relations.add

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationViewModel
import javax.inject.Inject

class AddOptionsRelationFragment : BaseAddOptionsRelationFragment() {

    @Inject
    lateinit var factory: AddOptionsRelationViewModel.Factory
    override val vm: AddOptionsRelationViewModel by viewModels { factory }

    override fun onStatusClicked(status: RelationValueView.Option.Status) {
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
        ) = AddOptionsRelationFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                TARGET_KEY to objectId,
                RELATION_KEY to relationId
            )
        }
    }
}
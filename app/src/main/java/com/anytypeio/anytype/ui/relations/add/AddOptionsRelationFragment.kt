package com.anytypeio.anytype.ui.relations.add

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.di.feature.DefaultComponentParam
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
            relationKey = relationKey,
            status = status
        )
    }

    override fun onCreateOptionClicked(name: String) {
        vm.onCreateObjectRelationOptionClicked(
            ctx = ctx,
            relationKey = relationKey,
            obj = target,
            name = name
        )
    }

    override fun onAddButtonClicked() {
        vm.onAddSelectedValuesToObjectClicked(
            ctx = ctx,
            obj = target,
            relationKey = relationKey
        )
    }

    override fun injectDependencies() {
        componentManager()
            .addObjectObjectRelationValueComponent
            .get(
                params = DefaultComponentParam(
                    ctx = ctx,
                    space = SpaceId(space)
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().addObjectObjectRelationValueComponent.release()
    }

    companion object {
        fun new(
            ctx: Id,
            space: Id,
            objectId: Id,
            relationKey: Key
        ) = AddOptionsRelationFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                SPACE_ID_KEY to space,
                TARGET_KEY to objectId,
                RELATION_KEY to relationKey
            )
        }
    }
}
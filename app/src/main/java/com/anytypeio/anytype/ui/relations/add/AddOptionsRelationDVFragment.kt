package com.anytypeio.anytype.ui.relations.add

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationDVViewModel
import javax.inject.Inject

open class AddOptionsRelationDVFragment : BaseAddOptionsRelationFragment() {

    @Inject
    lateinit var factory: AddOptionsRelationDVViewModel.Factory
    override val vm: AddOptionsRelationDVViewModel by viewModels { factory }

    override fun onStatusClicked(status: RelationValueView.Option.Status) {
        vm.onAddObjectSetStatusClicked(
            obj = target,
            relationKey = relationKey,
            status = status
        )
    }

    override fun onCreateOptionClicked(name: String) {
        vm.onCreateDataViewRelationOptionClicked(
            relationKey = relationKey,
            name = name,
            target = target
        )
    }

    override fun onAddButtonClicked() {
        vm.onAddSelectedValuesToDataViewClicked(
            target = target,
            relationKey = relationKey
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
            relationId: Id,
            relationKey: Key,
            dataview: Id,
            viewer: Id
        ) = AddOptionsRelationDVFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                TARGET_KEY to target,
                RELATION_ID to relationId,
                RELATION_KEY to relationKey,
                DATAVIEW_KEY to dataview,
                VIEWER_KEY to viewer
            )
        }
    }
}
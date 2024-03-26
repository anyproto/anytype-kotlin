package com.anytypeio.anytype.ui.relations.add

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationDVViewModel
import javax.inject.Inject

open class AddOptionsRelationDVFragment : BaseAddOptionsRelationFragment() {

    @Inject
    lateinit var factory: AddOptionsRelationDVViewModel.Factory
    override val vm: AddOptionsRelationDVViewModel by viewModels { factory }

    private val isIntrinsic: Boolean get() = argOrNull<Boolean>(IS_INTRINSIC_KEY) ?: false

    override fun onStatusClicked(status: RelationValueView.Option.Status) {
        vm.onAddObjectSetStatusClicked(
            obj = target,
            relationKey = relationKey,
            status = status
        )
    }

    override fun onCreateOptionClicked(name: String) {
        vm.onCreateDataViewRelationOptionClicked(
            ctx = ctx,
            relationKey = relationKey,
            name = name,
            target = target
        )
    }

    override fun onAddButtonClicked() {
        vm.onAddSelectedValuesToDataViewClicked(
            ctx = ctx,
            target = target,
            relationKey = relationKey
        )
    }

    override fun injectDependencies() {
        if (isIntrinsic) {
            componentManager().addObjectSetObjectRelationValueComponent.get(ctx).inject(this)
        } else {
            componentManager().addDataViewObjectRelationValueComponent.get(ctx).inject(this)
        }
    }

    override fun releaseDependencies() {
        if (isIntrinsic) {
            componentManager().addObjectSetObjectRelationValueComponent.release()
        } else {
            componentManager().addDataViewObjectRelationValueComponent.release()
        }
    }

    companion object {
        fun new(
            ctx: Id,
            target: Id,
            relationKey: Key,
            isIntrinsic: Boolean
        ) = AddOptionsRelationDVFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                TARGET_KEY to target,
                RELATION_KEY to relationKey,
                IS_INTRINSIC_KEY to isIntrinsic
            )
        }

        private const val IS_INTRINSIC_KEY = "args.relations.edit-value.is-intrinsic"
    }
}
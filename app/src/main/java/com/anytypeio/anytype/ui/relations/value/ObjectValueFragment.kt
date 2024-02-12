package com.anytypeio.anytype.ui.relations.value

import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_utils.ext.argBoolean
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueViewModel
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueViewModelFactory
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import javax.inject.Inject

class ObjectValueFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: ObjectValueViewModelFactory
    private val vm by viewModels<ObjectValueViewModel> { factory }

    private val ctx get() = argString(CTX_KEY)
    private val relationKey get() = argString(RELATION_KEY)
    private val objectId get() = argString(OBJECT_ID_KEY)
    private val isLocked get() = argBoolean(IS_LOCKED_KEY)
    private val relationContext get() = requireArguments().getSerializable(RELATION_CONTEXT_KEY) as RelationContext

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    override fun injectDependencies() {
        val params = ObjectValueViewModel.ViewModelParams(
            ctx = ctx,
            objectId = objectId,
            relationKey = relationKey,
            isLocked = isLocked,
            relationContext = relationContext
        )
        inject(params)
    }

    private fun inject(params: ObjectValueViewModel.ViewModelParams) = when (relationContext) {
        RelationContext.OBJECT -> componentManager()
            .objectValueObjectComponent.get(params)
            .inject(this)
        RelationContext.OBJECT_SET -> componentManager()
            .objectValueSetComponent.get(params)
            .inject(this)
        RelationContext.DATA_VIEW -> componentManager()
            .objectValueDataViewComponent.get(params)
            .inject(this)
    }

    override fun releaseDependencies() = when (relationContext) {
        RelationContext.OBJECT -> componentManager().objectValueObjectComponent.release()
        RelationContext.OBJECT_SET -> componentManager().objectValueSetComponent.release()
        RelationContext.DATA_VIEW -> componentManager().objectValueDataViewComponent.release()
    }

    companion object {
        const val CTX_KEY = "arg.relation.object.ctx"
        const val RELATION_KEY = "arg.relation.object.relation.key"
        const val OBJECT_ID_KEY = "arg.relation.object.object"
        const val IS_LOCKED_KEY = "arg.relation.object.is-locked"
        const val RELATION_CONTEXT_KEY = "arg.relation.object.relation-context"
    }
}
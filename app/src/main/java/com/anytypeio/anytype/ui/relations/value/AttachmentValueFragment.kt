package com.anytypeio.anytype.ui.relations.value

import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_utils.ext.argBoolean
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.value.attachment.AttachmentValueViewModel
import com.anytypeio.anytype.presentation.relations.value.attachment.AttachmentValueViewModelFactory
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationContext
import javax.inject.Inject

class AttachmentValueFragment : BaseBottomSheetComposeFragment() {

    private val ctx get() = argString(CTX_KEY)
    private val relationKey get() = argString(RELATION_KEY)
    private val objectId get() = argString(OBJECT_ID_KEY)
    private val isLocked get() = argBoolean(IS_LOCKED_KEY)
    private val relationContext get() = requireArguments().getSerializable(RELATION_CONTEXT_KEY) as RelationContext

    @Inject
    lateinit var factory: AttachmentValueViewModelFactory
    private val vm by viewModels<AttachmentValueViewModel> { factory }

    override fun injectDependencies() {
        val params = AttachmentValueViewModel.ViewModelParams(
            ctx = ctx,
            objectId = objectId,
            relationKey = relationKey,
            isLocked = isLocked,
            relationContext = relationContext
        )
        inject(params)
    }

    private fun inject(params: AttachmentValueViewModel.ViewModelParams) = when (relationContext) {
        RelationContext.OBJECT -> componentManager()
            .attachmentObjectComponent.get(params)
            .inject(this)
        RelationContext.OBJECT_SET -> componentManager()
            .attachmentSetComponent.get(params)
            .inject(this)
        RelationContext.DATA_VIEW -> componentManager()
            .attachmentDataViewComponent.get(params)
            .inject(this)
    }

    override fun releaseDependencies() = when (relationContext) {
        RelationContext.OBJECT -> componentManager().attachmentObjectComponent.release()
        RelationContext.OBJECT_SET -> componentManager().attachmentSetComponent.release()
        RelationContext.DATA_VIEW -> componentManager().attachmentDataViewComponent.release()
    }

    companion object {
        const val CTX_KEY = "arg.relation.attachment.ctx"
        const val RELATION_KEY = "arg.relation.attachment.relation.key"
        const val OBJECT_ID_KEY = "arg.relation.attachment.object"
        const val IS_LOCKED_KEY = "arg.relation.attachment.is-locked"
        const val RELATION_CONTEXT_KEY = "arg.relation.attachment.relation-context"
    }
}
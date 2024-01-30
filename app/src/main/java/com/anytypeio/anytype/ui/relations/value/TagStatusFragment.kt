package com.anytypeio.anytype.ui.relations.value

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_ui.relations.RelationsValueScreen
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusViewModel
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusViewModelFactory
import javax.inject.Inject

class TagStatusFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: TagStatusViewModelFactory
    private val vm by viewModels<TagStatusViewModel> { factory }

    private val ctx get() = argString(CTX_KEY)
    private val relationKey get() = argString(RELATION_KEY)
    private val objectId get() = argString(OBJECT_ID_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            RelationsValueScreen(
                state = vm.viewState.collectAsStateWithLifecycle().value,
                action = vm::onAction
            )
        }
        vm.onStart()
    }

    override fun injectDependencies() {
        val params = TagStatusViewModel.Params(
            ctx = ctx,
            objectId = objectId,
            relationKey = relationKey
        )
        componentManager()
            .tagStatusObjectComponent.get(params)
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().tagStatusObjectComponent.release()
    }

    companion object {
        fun new(
            ctx: Id,
            objectId: Id,
            relationKey: Key
        ) = TagStatusFragment().apply {
            arguments = bundleOf(
                CTX_KEY to ctx,
                OBJECT_ID_KEY to objectId,
                RELATION_KEY to relationKey
            )
        }

        const val CTX_KEY = "arg.tag-status.ctx"
        const val RELATION_KEY = "arg.tag-status.relation.key"
        const val OBJECT_ID_KEY = "arg.tag-status.object"
    }
}
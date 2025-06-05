package com.anytypeio.anytype.ui.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_chats.presentation.SelectChatReactionViewModel
import com.anytypeio.anytype.feature_chats.ui.SelectChatReactionScreen
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class SelectChatReactionFragment : BaseBottomSheetComposeFragment() {

    private val chat: Id get() = arg<Id>(CHAT_ID_KEY)
    private val msg: Id get() = arg<Id>(MSG_ID_KEY)

    @Inject
    lateinit var factory: SelectChatReactionViewModel.Factory

    private val vm by viewModels<SelectChatReactionViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography
            ) {
                SelectChatReactionScreen(
                    views = vm.views.collectAsStateWithLifecycle(initialValue = emptyList()).value,
                    onEmojiClicked = vm::onEmojiClicked,
                    onQueryChanged = vm::onQueryChanged
                )
                LaunchedEffect(Unit) {
                    vm.isDismissed.collect { isDismissed ->
                        if (isDismissed) dismiss()
                    }
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().selectChatReactionComponent
            .get(
                key = getComponentKey(),
                param = SelectChatReactionViewModel.Params(
                    chat = chat,
                    msg = msg
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectChatReactionComponent.release(id = getComponentKey())
    }

    private fun getComponentKey(): String = "$COMPONENT_PREFIX-$chat"

    companion object {
        private const val COMPONENT_PREFIX = "select-chat-reaction"
        private const val SPACE_ID_KEY = "select-chat-reaction.space"
        private const val CHAT_ID_KEY = "select-chat-reaction.chat"
        private const val MSG_ID_KEY = "select-chat-reaction.msg"

        fun args(
            space: SpaceId,
            chat: String,
            msg: String
        ): Bundle = bundleOf(
            SPACE_ID_KEY to space.id,
            CHAT_ID_KEY to chat,
            MSG_ID_KEY to msg
        )
    }
}
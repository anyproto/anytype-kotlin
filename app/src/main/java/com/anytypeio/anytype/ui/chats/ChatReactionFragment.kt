package com.anytypeio.anytype.ui.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
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
import com.anytypeio.anytype.feature_chats.presentation.ChatReactionViewModel
import com.anytypeio.anytype.feature_chats.ui.ChatReactionScreen
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import kotlin.getValue

class ChatReactionFragment : BaseBottomSheetComposeFragment() {

    private val chat: Id get() = arg<Id>(CHAT_ID_KEY)
    private val msg: Id get() = arg<Id>(MSG_ID_KEY)
    private val emoji: String get() = arg<String>(EMOJI_KEY)

    @Inject
    lateinit var factory: ChatReactionViewModel.Factory

    private val vm by viewModels<ChatReactionViewModel> { factory }

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
                ChatReactionScreen(
                    viewState = vm.viewState.collectAsStateWithLifecycle().value
                )
            }
        }
    }

    override fun injectDependencies() {
        componentManager().chatReactionComponent
            .get(
                key = getComponentKey(),
                param = ChatReactionViewModel.Params(
                    chat = chat,
                    msg = msg,
                    emoji = emoji
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().chatReactionComponent.release(id = getComponentKey())
    }

    private fun getComponentKey(): String = "$COMPONENT_PREFIX-$chat"

    companion object {

        private const val COMPONENT_PREFIX = "chat-reaction"

        private const val SPACE_ID_KEY = "chat.reaction.space"
        private const val CHAT_ID_KEY = "chat.reaction.chat"
        private const val MSG_ID_KEY = "chat.reaction.msg"
        private const val EMOJI_KEY = "chat.reaction.emoji"

        fun args(
            space: SpaceId,
            chat: String,
            msg: String,
            emoji: String
        ): Bundle = bundleOf(
            SPACE_ID_KEY to space.id,
            CHAT_ID_KEY to chat,
            MSG_ID_KEY to msg,
            EMOJI_KEY to emoji
        )
    }
}
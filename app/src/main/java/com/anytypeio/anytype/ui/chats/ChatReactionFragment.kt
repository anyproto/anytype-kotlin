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
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_discussions.presentation.ChatReactionViewModel
import com.anytypeio.anytype.feature_discussions.ui.ChatReactionPicker
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import kotlin.getValue
import okhttp3.internal.notify

class ChatReactionFragment : BaseBottomSheetComposeFragment() {

    private val chat: Id get() = arg<Id>(CHAT_ID_KEY)
    private val msg: Id get() = arg<Id>(MSG_ID_KEY)

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
                ChatReactionPicker(
                    views = vm.default.collectAsStateWithLifecycle().value,
                    onEmojiClicked = vm::onEmojiClicked
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
        componentManager().chatReactionPickerComponent
            .get(
                key = chat,
                param = ChatReactionViewModel.Params(
                    chat = chat,
                    msg = msg
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        // TODO
    }

    companion object {

        private const val SPACE_ID_KEY = "chat.reaction-picker.space"
        private const val CHAT_ID_KEY = "chat.reaction-picker.chat"
        private const val MSG_ID_KEY = "chat.reaction-picker.msg"

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
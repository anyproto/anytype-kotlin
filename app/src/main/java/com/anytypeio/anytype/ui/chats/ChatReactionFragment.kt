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
import com.anytypeio.anytype.core_models.primitives.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_discussions.presentation.ChatReactionViewModel
import com.anytypeio.anytype.feature_discussions.ui.ChatReactionPicker
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import kotlin.getValue
import okhttp3.internal.notify

class ChatReactionFragment : BaseBottomSheetComposeFragment() {

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
                    vm.default.collectAsStateWithLifecycle().value
                )
            }
        }
    }

    override fun injectDependencies() {
        componentManager().chatReactionPickerComponent
            .get(
                key = "test",
                param = ChatReactionViewModel.Params(
                    chat = "",
                    msg = ""
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        // TODO
    }

    companion object {

        const val SPACE_ID_KEY = "chat.reaction-picker.space"
        const val CHAT_ID_KEY = "chat.reaction-picker.chat"
        const val MSG_ID_KEY = "chat.reaction-picker.msg"

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
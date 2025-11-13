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
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_chats.presentation.SelectChatIconViewModel
import com.anytypeio.anytype.feature_chats.ui.SelectChatIconScreen
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class SelectChatIconFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: SelectChatIconViewModel.Factory

    private val vm by viewModels<SelectChatIconViewModel> { factory }

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
                SelectChatIconScreen(
                    views = vm.views.collectAsStateWithLifecycle(initialValue = emptyList()).value,
                    onEmojiClicked = vm::onEmojiClicked,
                    onQueryChanged = vm::onQueryChanged
                )
                LaunchedEffect(Unit) {
                    vm.isDismissed.collect { isDismissed ->
                        if (isDismissed) dismiss()
                    }
                }
                LaunchedEffect(Unit) {
                    vm.emojiSelected.collect { emoji ->
                        parentFragmentManager.setFragmentResult(
                            REQUEST_KEY,
                            bundleOf(RESULT_EMOJI_KEY to emoji)
                        )
                    }
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().selectChatIconComponent
            .get()
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectChatIconComponent.release()
    }

    companion object {
        private const val COMPONENT_KEY = "select-chat-icon"
        const val REQUEST_KEY = "select-chat-icon-request"
        const val RESULT_EMOJI_KEY = "emoji"

        fun new(): SelectChatIconFragment = SelectChatIconFragment()
    }
}

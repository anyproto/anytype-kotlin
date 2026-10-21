package com.anytypeio.anytype.ui.search.v2

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ext.FragmentResultContract
import com.anytypeio.anytype.feature_search.presentation.SearchNavigation
import com.anytypeio.anytype.feature_search.presentation.SearchViewModel
import com.anytypeio.anytype.feature_search.ui.SearchScreen
import com.anytypeio.anytype.ui.chats.ChatFragment
import com.anytypeio.anytype.ui.date.DateObjectFragment
import com.anytypeio.anytype.ui.discussions.DiscussionFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.home.WidgetsScreenFragment
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.profile.ParticipantFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import timber.log.Timber

/**
 * The unified search surface (search v2): one screen, three scopes.
 * Opened from the vault (global mode) or from inside a space (that space's
 * scope token seeded); in-chat entry additionally seeds a chat filter token.
 */
class SearchV2Fragment : BaseComposeFragment() {

    private val space: Id? get() = argOrNull(ARG_SPACE)
    private val chat: Id? get() = argOrNull(ARG_CHAT)

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val vm by viewModels<SearchViewModel> { factory }

    /**
     * IDLE expiry, not wall-clock-since-open: the pause stamp is written when
     * the user leaves the screen (detour into a result, app backgrounded) and
     * checked on return, so time actively spent ON the screen never expires
     * it, and a configuration change (pause→resume within milliseconds)
     * never trips it. Survives process death via saved state.
     */
    private var lastPausedAt: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lastPausedAt = savedInstanceState?.getLong(PAUSED_AT_KEY) ?: 0L
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(PAUSED_AT_KEY, lastPausedAt)
    }

    override fun onPause() {
        super.onPause()
        lastPausedAt = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        // A search left idle for long (a long read of a result, the app
        // backgrounded) expires: coming back to a stale surface is worse
        // than none (iOS: 5-minute idle reset).
        if (lastPausedAt > 0L && System.currentTimeMillis() - lastPausedAt > EXPIRY_MS) {
            runCatching { findNavController().popBackStack() }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme(typography = typography) {
            SearchScreen(
                viewModel = vm,
                onBackClicked = {
                    runCatching { findNavController().popBackStack() }
                }
            )
            // STARTED-gated: a navigation command landing after
            // onSaveInstanceState must wait, not throw — the VM's channel
            // buffers it until the collector restarts.
            val lifecycleOwner = LocalLifecycleOwner.current
            LaunchedEffect(Unit) {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    vm.commands.collect { command -> proceed(command) }
                }
            }
        }
    }

    override fun onApplyWindowRootInsets(view: View) {
        // At or above EDGE_TO_EDGE_MIN_SDK the insets are handled in Compose —
        // the background must paint edge-to-edge behind the system bars.
        if (Build.VERSION.SDK_INT < EDGE_TO_EDGE_MIN_SDK) {
            super.onApplyWindowRootInsets(view)
        }
    }

    private fun proceed(command: SearchNavigation) {
        when (command) {
            is SearchNavigation.OpenObject -> proceedWithNavigation(command.navigation)
            is SearchNavigation.OpenChatAtMessage -> {
                runCatching {
                    findNavController().navigate(
                        R.id.chatScreen,
                        ChatFragment.args(
                            space = command.space,
                            ctx = command.chat,
                            startAtMessage = command.message,
                            popUpToVault = command.switchSpace
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error opening chat at message from search")
                }
            }
            is SearchNavigation.OpenDiscussionAtMessage -> {
                // iOS §11.2: land on the PARENT editor with the thread pushed
                // above it, scrolled to the message — not on a bare discussion.
                runCatching {
                    findNavController().navigate(
                        R.id.objectNavigation,
                        EditorFragment.args(ctx = command.parentObject, space = command.space)
                    )
                    findNavController().navigate(
                        R.id.discussionScreen,
                        DiscussionFragment.args(
                            ctx = command.discussion,
                            space = command.space,
                            startAtMessage = command.message
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error opening discussion at message from search")
                }
            }
            is SearchNavigation.OpenSpace -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionOpenSpaceFromVault,
                        WidgetsScreenFragment.args(
                            space = command.space,
                            deeplink = null
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error opening space from search")
                }
            }
            SearchNavigation.OpenCreateChannel -> {
                // Hand off to the vault's create-channel menu.
                runCatching {
                    findNavController().popBackStack(R.id.vaultScreen, false)
                    parentFragmentManager.setFragmentResult(
                        FragmentResultContract.OPEN_CREATE_CHANNEL_KEY,
                        Bundle()
                    )
                }.onFailure {
                    Timber.e(it, "Error opening create-channel from search")
                }
            }
            is SearchNavigation.Toast -> toast(getString(command.messageRes))
            is SearchNavigation.ObjectSelected -> {
                // Picker purpose only — never emitted for the navigation surface.
            }
        }
    }

    private fun proceedWithNavigation(nav: OpenObjectNavigation) {
        when (nav) {
            is OpenObjectNavigation.OpenEditor -> {
                runCatching {
                    findNavController().navigate(
                        R.id.objectNavigation,
                        EditorFragment.args(ctx = nav.target, space = nav.space)
                    )
                }.onFailure { Timber.e(it, "Error opening editor from search") }
            }
            is OpenObjectNavigation.OpenDataView -> {
                runCatching {
                    findNavController().navigate(
                        R.id.dataViewNavigation,
                        ObjectSetFragment.args(ctx = nav.target, space = nav.space)
                    )
                }.onFailure { Timber.e(it, "Error opening data view from search") }
            }
            is OpenObjectNavigation.OpenParticipant -> {
                runCatching {
                    findNavController().navigate(
                        R.id.participantScreen,
                        ParticipantFragment.args(objectId = nav.target, space = nav.space)
                    )
                }.onFailure { Timber.e(it, "Error opening participant from search") }
            }
            is OpenObjectNavigation.OpenChat -> {
                runCatching {
                    findNavController().navigate(
                        R.id.chatScreen,
                        ChatFragment.args(
                            ctx = nav.target,
                            space = nav.space,
                            popUpToVault = false
                        )
                    )
                }.onFailure { Timber.e(it, "Error opening chat from search") }
            }
            is OpenObjectNavigation.OpenDateObject -> {
                runCatching {
                    findNavController().navigate(
                        R.id.dateObjectScreen,
                        DateObjectFragment.args(objectId = nav.target, space = nav.space)
                    )
                }.onFailure { Timber.e(it, "Error opening date object from search") }
            }
            is OpenObjectNavigation.OpenType -> {
                runCatching {
                    navigation().openObjectType(objectId = nav.target, space = nav.space)
                }.onFailure { Timber.e(it, "Error opening type from search") }
            }
            is OpenObjectNavigation.OpenBookmarkUrl -> {
                runCatching {
                    ActivityCustomTabsHelper.openUrl(
                        activity = requireActivity(),
                        url = nav.url
                    )
                }.onFailure {
                    Timber.e(it, "Error opening bookmark URL from search")
                    toast(getString(R.string.error_unexpected_layout))
                }
            }
            OpenObjectNavigation.NonValidObject -> {
                toast(getString(R.string.error_non_valid_object))
            }
            is OpenObjectNavigation.UnexpectedLayoutError -> {
                toast(getString(R.string.error_unexpected_layout))
            }
        }
    }

    override fun injectDependencies() {
        val params = SearchViewModel.VmParams(
            entrySpace = space?.let { SpaceId(it) },
            chat = chat
        )
        // new(), never get(): the holder is shared with the attach picker and
        // other launches — a cached instance would carry the WRONG params
        // (entry space, purpose) into this screen.
        componentManager().searchV2Component.new(params).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().searchV2Component.release()
    }

    companion object {
        const val ARG_SPACE = "arg.search-v2.space"
        const val ARG_CHAT = "arg.search-v2.chat"
        private const val PAUSED_AT_KEY = "search-v2.paused-at"
        private const val EXPIRY_MS = 5 * 60_000L

        /**
         * [space] null = vault entry (global mode); set = that space's scope
         * is seeded. [chat] additionally seeds a chat filter token (in-chat entry).
         */
        fun args(space: Id?, chat: Id? = null): Bundle = bundleOf(
            ARG_SPACE to space,
            ARG_CHAT to chat
        )
    }
}

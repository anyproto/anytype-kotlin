package com.anytypeio.anytype.ui.vault

import android.os.Build.VERSION.SDK_INT
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavOptions.*
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.other.DefaultDeepLinkResolver
import com.anytypeio.anytype.presentation.vault.VaultCommand
import com.anytypeio.anytype.presentation.vault.VaultNavigation
import com.anytypeio.anytype.presentation.vault.VaultViewModel
import com.anytypeio.anytype.presentation.vault.VaultViewModelFactory
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.chats.ChatFragment
import com.anytypeio.anytype.ui.gallery.GalleryInstallationFragment
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import com.anytypeio.anytype.ui.multiplayer.RequestJoinSpaceFragment
import com.anytypeio.anytype.ui.payments.MembershipFragment
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.spaces.CreateSpaceFragment.Companion.ARG_SPACE_TYPE
import com.anytypeio.anytype.ui.spaces.CreateSpaceFragment.Companion.TYPE_CHAT
import com.anytypeio.anytype.ui.spaces.CreateSpaceFragment.Companion.TYPE_SPACE
import javax.inject.Inject
import timber.log.Timber

class VaultFragment : BaseComposeFragment() {

    private val deepLink: String? get() = argOrNull(DEEP_LINK_KEY)

    @Inject
    lateinit var factory: VaultViewModelFactory

    private val vm by viewModels<VaultViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(typography = typography) {
                Box(modifier = Modifier.fillMaxSize()) {
                    VaultScreenWithUnreadSection(
                        sections = vm.sections.collectAsStateWithLifecycle().value,
                        onSpaceClicked = vm::onSpaceClicked,
                        onCreateSpaceClicked = vm::onChooseSpaceTypeClicked,
                        onSettingsClicked = vm::onSettingsClicked,
                        onOrderChanged = vm::onOrderChanged,
                        onDragEnd = vm::onDragEnd,
                        profile = vm.profileView.collectAsStateWithLifecycle().value,
                        isLoading = vm.loadingState.collectAsStateWithLifecycle().value,
                    )

                    if (vm.showChooseSpaceType.collectAsStateWithLifecycle().value) {
                        ChooseSpaceTypeScreen(
                            onCreateChatClicked = {
                                vm.onCreateChatClicked()
                            },
                            onCreateSpaceClicked = {
                                vm.onCreateSpaceClicked()
                            },
                            onDismiss = {
                                vm.onChooseSpaceTypeDismissed()
                            }
                        )
                    }
                }
            }
            LaunchedEffect(Unit) {
                vm.commands.collect { command -> proceed(command) }
            }
            LaunchedEffect(Unit) {
                vm.navigations.collect { command -> proceed(command) }
            }
        }
    }

    private fun proceed(command: VaultCommand) {
        when (command) {
            is VaultCommand.EnterSpaceHomeScreen -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionOpenSpaceFromVault,
                        HomeScreenFragment.args(
                            space = command.space.id,
                            deeplink = null
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening space from vault")
                }
            }

            is VaultCommand.EnterSpaceLevelChat -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionOpenChatFromVault,
                        ChatFragment.args(
                            space = command.space.id,
                            ctx = command.chat
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening space-level chat from vault")
                }
            }

            is VaultCommand.CreateNewSpace -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionCreateSpaceFromVault,
                        bundleOf(ARG_SPACE_TYPE to TYPE_SPACE)
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening create space screen from vault")
                }
            }

            VaultCommand.CreateChat -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionCreateChatFromVault,
                        bundleOf(ARG_SPACE_TYPE to TYPE_CHAT)
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening create chat screen from vault")
                }
            }

            is VaultCommand.OpenProfileSettings -> {
                runCatching {
                    findNavController().navigate(
                        R.id.profileSettingsScreen,
                        null
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening profile settings from vault")
                }
            }

            is VaultCommand.Deeplink.Invite -> {
                findNavController().navigate(
                    R.id.requestJoinSpaceScreen,
                    RequestJoinSpaceFragment.args(link = command.link)
                )
            }

            is VaultCommand.Deeplink.GalleryInstallation -> {
                findNavController().navigate(
                    R.id.galleryInstallationScreen,
                    GalleryInstallationFragment.args(
                        deepLinkType = command.deepLinkType,
                        deepLinkSource = command.deepLinkSource
                    )
                )
            }

            is VaultCommand.Deeplink.MembershipScreen -> {
                findNavController().navigate(
                    R.id.paymentsScreen,
                    MembershipFragment.args(command.tierId),
                    Builder().setLaunchSingleTop(true).build()
                )
            }

            is VaultCommand.Deeplink.DeepLinkToObjectNotWorking -> {
                toast(
                    getString(R.string.multiplayer_deeplink_to_your_object_error)
                )
            }
        }
    }

    private fun proceed(destination: VaultNavigation) {
        when (destination) {
            is VaultNavigation.OpenObject -> runCatching {
                findNavController().navigate(
                    R.id.actionOpenSpaceFromVault,
                    HomeScreenFragment.args(
                        space = destination.space,
                        deeplink = null
                    )
                )
                navigation().openDocument(
                    target = destination.ctx,
                    space = destination.space
                )
            }.onFailure {
                Timber.e(it, "Error while opening object from vault")
            }

            is VaultNavigation.OpenSet -> runCatching {
                findNavController().navigate(
                    R.id.actionOpenSpaceFromVault,
                    HomeScreenFragment.args(
                        space = destination.space,
                        deeplink = null
                    )
                )
                navigation().openObjectSet(
                    target = destination.ctx,
                    space = destination.space,
                    view = destination.view
                )
            }.onFailure {
                Timber.e(it, "Error while opening set or collection from vault")
            }

            is VaultNavigation.OpenChat -> {
                findNavController().navigate(
                    R.id.actionOpenSpaceFromVault,
                    HomeScreenFragment.args(
                        space = destination.space,
                        deeplink = null
                    )
                )
                navigation().openChat(
                    target = destination.ctx,
                    space = destination.space
                )
            }

            is VaultNavigation.OpenDateObject -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionOpenSpaceFromVault,
                        HomeScreenFragment.args(
                            space = destination.space,
                            deeplink = null
                        )
                    )
                    navigation().openDateObject(
                        objectId = destination.ctx,
                        space = destination.space
                    )
                }.onFailure { e ->
                    Timber.e(e, "Error while opening date object from widgets")
                }
            }

            is VaultNavigation.OpenParticipant -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionOpenSpaceFromVault,
                        HomeScreenFragment.args(
                            space = destination.space,
                            deeplink = null
                        )
                    )
                    navigation().openParticipantObject(
                        objectId = destination.ctx,
                        space = destination.space
                    )
                }.onFailure { e ->
                    Timber.e(e, "Error while opening participant object from widgets")
                }
            }

            is VaultNavigation.OpenType -> {
                Timber.e("Illegal command: type cannot be opened from vault")
            }

            is VaultNavigation.OpenUrl -> {
                try {
                    ActivityCustomTabsHelper.openUrl(
                        activity = requireActivity(),
                        url = destination.url
                    )
                } catch (e: Throwable) {
                    Timber.e(e, "Error opening bookmark URL: ${destination.url}")
                    toast("Failed to open URL")
                }
            }

            is VaultNavigation.ShowError -> {
                toast(destination.message)
            }
        }
    }

    override fun onApplyWindowRootInsets(view: View) {
        if (SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
            // Do nothing.
        } else {
            super.onApplyWindowRootInsets(view)
        }
    }

    override fun onResume() {
        super.onResume()
        proceedWithDeepLinks()
        vm.processPendingDeeplink()
    }

    private fun proceedWithDeepLinks() {
        val deepLinkFromFragmentArgs = deepLink
        if (deepLinkFromFragmentArgs != null) {
            Timber.d("Deeplink  from fragment args")
            vm.onResume(DefaultDeepLinkResolver.resolve(deepLinkFromFragmentArgs))
            arguments?.putString(DEEP_LINK_KEY, null)
        } else {
            vm.onResume(null)
        }
    }

    override fun injectDependencies() {
        componentManager().vaultComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().vaultComponent.release()
    }

    companion object {
        private const val SHOW_MNEMONIC_KEY = "arg.vault-screen.show-mnemonic"
        private const val DEEP_LINK_KEY = "arg.vault-screen.deep-link"
        fun args(deeplink: String?): Bundle = bundleOf(
            DEEP_LINK_KEY to deeplink
        )
    }
}
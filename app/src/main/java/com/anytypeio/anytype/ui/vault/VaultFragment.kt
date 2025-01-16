package com.anytypeio.anytype.ui.vault

import android.os.Build.VERSION.SDK_INT
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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.BuildConfig.USE_EDGE_TO_EDGE
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.other.DefaultDeepLinkResolver
import com.anytypeio.anytype.presentation.vault.VaultViewModel
import com.anytypeio.anytype.presentation.vault.VaultViewModel.Navigation
import com.anytypeio.anytype.presentation.vault.VaultViewModel.Command
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.gallery.GalleryInstallationFragment
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import com.anytypeio.anytype.ui.multiplayer.RequestJoinSpaceFragment
import com.anytypeio.anytype.ui.payments.MembershipFragment
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import timber.log.Timber

class VaultFragment : BaseComposeFragment() {

    private val deepLink: String? get() = argOrNull(DEEP_LINK_KEY)

    @Inject
    lateinit var factory: VaultViewModel.Factory

    private val vm by viewModels<VaultViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(typography = typography) {
                VaultScreen(
                    spaces = vm.spaces.collectAsStateWithLifecycle().value,
                    onSpaceClicked = vm::onSpaceClicked,
                    onCreateSpaceClicked = vm::onCreateSpaceClicked,
                    onSettingsClicked = vm::onSettingsClicked,
                    onOrderChanged = vm::onOrderChanged
                )
            }
            LaunchedEffect(Unit) {
                vm.commands.collect { command -> proceed(command) }
            }
            LaunchedEffect(Unit) {
                vm.navigation.collect { command -> proceed(command) }
            }
        }
    }

    private fun proceed(command: Command) {
        when (command) {
            is Command.EnterSpaceHomeScreen -> {
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
            is Command.CreateNewSpace -> {
                runCatching {
                    findNavController().navigate(
                        R.id.actionCreateSpaceFromVault
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening create-space screen from vault")
                }
            }
            is Command.OpenProfileSettings -> {
                runCatching {
                    findNavController().navigate(
                        R.id.profileSettingsScreen,
                        null
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening profile settings from vault")
                }
            }
            is Command.ShowIntroduceVault -> {
                runCatching {
                    findNavController().navigate(R.id.actionShowIntroduceVaultScreen)
                }.onFailure {
                    Timber.e(it, "Error while opening introduce-vault-screen from vault")
                }
            }
            is Command.Deeplink.Invite -> {
                findNavController().navigate(
                    R.id.requestJoinSpaceScreen,
                    RequestJoinSpaceFragment.args(link = command.link)
                )
            }
            is Command.Deeplink.GalleryInstallation -> {
                findNavController().navigate(
                    R.id.galleryInstallationScreen,
                    GalleryInstallationFragment.args(
                        deepLinkType = command.deepLinkType,
                        deepLinkSource = command.deepLinkSource
                    )
                )
            }
            is Command.Deeplink.MembershipScreen -> {
                findNavController().navigate(
                    R.id.paymentsScreen,
                    MembershipFragment.args(command.tierId),
                    NavOptions.Builder().setLaunchSingleTop(true).build()
                )
            }
            is Command.Deeplink.DeepLinkToObjectNotWorking -> {
                toast(
                    getString(R.string.multiplayer_deeplink_to_your_object_error)
                )
            }
        }
    }

    private fun proceed(destination: Navigation) {
        when (destination) {
            is Navigation.OpenObject -> runCatching {
                findNavController().navigate(R.id.actionOpenSpaceFromVault)
                navigation().openDocument(
                    target = destination.ctx,
                    space = destination.space
                )
            }.onFailure {
                Timber.e(it, "Error while opening object from vault")
            }
            is Navigation.OpenSet -> runCatching {
                findNavController().navigate(R.id.actionOpenSpaceFromVault)
                navigation().openObjectSet(
                    target = destination.ctx,
                    space = destination.space,
                    view = destination.view
                )
            }.onFailure {
                Timber.e(it, "Error while opening set or collection from vault")
            }
            is Navigation.OpenChat -> {
                findNavController().navigate(R.id.actionOpenSpaceFromVault)
                navigation().openChat(
                    target = destination.ctx,
                    space = destination.space
                )
            }
            is Navigation.OpenDateObject -> {
                runCatching {
                    findNavController().navigate(R.id.actionOpenSpaceFromVault)
                    navigation().openDateObject(
                        objectId = destination.ctx,
                        space = destination.space
                    )
                }.onFailure { e ->
                    Timber.e(e, "Error while opening date object from widgets")
                }
            }
        }
    }

    override fun onApplyWindowRootInsets(view: View) {
        if (USE_EDGE_TO_EDGE && SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
            // Do nothing.
        } else {
            super.onApplyWindowRootInsets(view)
        }
    }

    override fun onResume() {
        super.onResume()
        proceedWithDeepLinks()
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
        fun args(deeplink: String?) : Bundle = bundleOf(
            DEEP_LINK_KEY to deeplink
        )
    }
}
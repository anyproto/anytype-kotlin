package com.anytypeio.anytype.ui.vault

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
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.vault.VaultViewModel
import com.anytypeio.anytype.presentation.vault.VaultViewModel.Command
import com.anytypeio.anytype.ui.settings.ProfileSettingsFragment
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import timber.log.Timber

class VaultFragment : BaseComposeFragment() {

    // TODO handle deeplink

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
                vm.commands.collect { command ->
                    proceedWithCommand(command)
                }
            }
        }
    }

    private fun proceedWithCommand(command: Command) {
        when (command) {
            is Command.EnterSpaceHomeScreen -> {
                runCatching {
                    findNavController().navigate(R.id.actionOpenSpaceFromVault)
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
                        R.id.profileScreen,
                        bundleOf(ProfileSettingsFragment.SPACE_ID_KEY to command.space.id)
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
        }
    }

    override fun onApplyWindowRootInsets(view: View) {
        // TODO Do nothing ?
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun injectDependencies() {
        componentManager().vaultComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().vaultComponent.release()
    }

    companion object {
        const val SHOW_MNEMONIC_KEY = "arg.vault-screen.show-mnemonic"
        const val DEEP_LINK_KEY = "arg.vault-screen.deep-link"
        fun args(deeplink: String?) : Bundle = bundleOf(
            DEEP_LINK_KEY to deeplink
        )
    }
}
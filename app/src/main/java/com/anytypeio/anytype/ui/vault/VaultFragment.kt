package com.anytypeio.anytype.ui.vault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.vault.VaultViewModel
import com.anytypeio.anytype.presentation.vault.VaultViewModel.Command
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class VaultFragment : BaseComposeFragment() {

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
                    onCreateSpaceClicked = {
                        // TODO
                    }
                )
            }
            LaunchedEffect(Unit) {
                vm.commands.collect { command ->
                    when(command) {
                        Command.EnterSpaceHomeScreen -> {
                            // Temporary implementation for dev builds
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    override fun onApplyWindowRootInsets(view: View) {
        // Do nothing
    }

    override fun injectDependencies() {
        componentManager().vaultComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().vaultComponent.release()
    }
}
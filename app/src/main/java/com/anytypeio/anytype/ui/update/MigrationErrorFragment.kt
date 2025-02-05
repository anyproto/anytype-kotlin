package com.anytypeio.anytype.ui.update

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.update.MigrationErrorViewModel
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.vault.VaultFragment
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class MigrationErrorFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: MigrationErrorViewModel.Factory

    private val vm by viewModels<MigrationErrorViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(
        context = requireContext()
    ).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(typography = typography) {
                MigrationInProgressScreen()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.commands.collect { command ->
                    when(command) {
                        is MigrationErrorViewModel.Command.Restart -> {
                            runCatching {
                                findNavController().navigate(R.id.actionOpenVault)
                            }.onFailure {
                                Timber.e(it, "Error while trying to open vault screen from onboarding")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onApplyWindowRootInsets(view: View) {
        // Do nothing.
    }

    override fun injectDependencies() {
        componentManager().migrationErrorComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().migrationErrorComponent.release()
    }
}
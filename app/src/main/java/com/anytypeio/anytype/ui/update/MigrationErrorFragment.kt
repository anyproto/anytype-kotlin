package com.anytypeio.anytype.ui.update

import android.content.Intent
import android.net.Uri
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
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.update.MigrationErrorViewModel
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.settings.typography
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
                MigrationErrorScreen(vm::onAction)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.commands.collect { command ->
                    when(command) {
                        is MigrationErrorViewModel.Command.Browse -> {
                            browseUrl(command)
                        }
                        is MigrationErrorViewModel.Command.Exit -> {
                            navigation().exitFromMigrationScreen()
                        }
                    }
                }
            }
        }
    }

    private fun browseUrl(command: MigrationErrorViewModel.Command.Browse) {
        try {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(command.url)
            }.let(::startActivity)
        } catch (e: Exception) {
            Timber.e(e, "Error while browsing url")
        }
    }

    override fun injectDependencies() {
        componentManager().migrationErrorComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().migrationErrorComponent.release()
    }
}
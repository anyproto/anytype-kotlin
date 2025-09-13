package com.anytypeio.anytype.ui.publishtoweb

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
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.publishtoweb.MySitesViewModel
import com.anytypeio.anytype.ui.settings.typography
import timber.log.Timber
import javax.inject.Inject

class MySitesFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: MySitesViewModel.Factory

    private val vm by viewModels<MySitesViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    MySitesScreen(
                        viewState = vm.viewState.collectAsStateWithLifecycle().value
                    )
                    LaunchedEffect(Unit) {
                        vm.commands.collect { command ->
                            Timber.d("MySites New command: $command")
                            // TODO: Handle commands when needed
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skipCollapsed()
        expand()
    }

    override fun injectDependencies() {
        componentManager()
            .mySitesComponent
            .get(params = MySitesViewModel.VmParams)
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().mySitesComponent.release()
    }
}
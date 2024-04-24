package com.anytypeio.anytype.ui.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_ui.features.multiplayer.SpaceListScreen
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.spaces.SpaceListViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class SpaceListFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: SpaceListViewModel.Factory

    private val vm by viewModels<SpaceListViewModel> { factory }

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
                SpaceListScreen(state = vm.state.collectAsStateWithLifecycle().value)
            }
        }
    }


    override fun injectDependencies() {
        componentManager().spaceListComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().spaceListComponent.release()
    }
}
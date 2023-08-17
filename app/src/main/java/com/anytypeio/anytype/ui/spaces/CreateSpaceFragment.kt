package com.anytypeio.anytype.ui.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.spaces.CreateSpaceViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class CreateSpaceFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: CreateSpaceViewModel.Factory

    private val vm by viewModels<CreateSpaceViewModel> { factory }

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
                // TODO
            }
        }
    }

    override fun injectDependencies() {
        componentManager().createSpaceComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createSpaceComponent.release()
    }
}
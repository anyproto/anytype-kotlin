package com.anytypeio.anytype.ui.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.spaces.SelectSpaceViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class SelectSpaceFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: SelectSpaceViewModel.Factory

    private val vm by viewModels<SelectSpaceViewModel> { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.SelectSpaceDialogTheme
        )
    }

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
                SelectSpaceScreen(
                    spaces = vm.views.collectAsState().value,
                    onSpaceClicked = vm::onSpaceClicked,
                    onAddClicked = {
                        findNavController().navigate(
                            R.id.createSpaceScreen
                        )
                    },
                    onSpaceSettingsClicked = {
                        findNavController().navigate(
                            R.id.settingsScreen
                        )
                    },
                    onProfileClicked = {
                        findNavController().navigate(
                            R.id.profileScreen
                        )
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun injectDependencies() {
        componentManager().selectSpaceComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectSpaceComponent.release()
    }
}
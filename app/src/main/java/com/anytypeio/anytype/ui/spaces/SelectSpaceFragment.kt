package com.anytypeio.anytype.ui.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.spaces.Command
import com.anytypeio.anytype.presentation.spaces.SelectSpaceViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class SelectSpaceFragment : BaseBottomSheetComposeFragment() {

    private val exitHomeWhenSpaceIsSelected get() = argOrNull<Boolean>(EXIT_HOME_WHEN_SPACE_IS_SELECTED_KEY)

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
                    onAddClicked = throttledClick(
                        onClick = { vm.onCreateSpaceClicked() }
                    ),
                    onSettingsClicked = throttledClick(
                        onClick = { findNavController().navigate(R.id.profileScreen) }
                    ),
                    onProfileClicked = throttledClick(
                        onClick = { findNavController().navigate(R.id.profileScreen) }
                    )
                )
            }
            LaunchedEffect(Unit) {
                vm.commands.collect { command -> proceedWithCommand(command) }
            }
            LaunchedEffect(Unit) {
                vm.toasts.collect { toast(it) }
            }
        }
    }

    private fun proceedWithCommand(command: Command) {
        when (command) {
            is Command.CreateSpace -> {
                findNavController().navigate(
                    R.id.createSpaceScreen
                )
            }
            is Command.Dismiss -> {
                findNavController().popBackStack()
            }
            is Command.SwitchToNewSpace -> {
                if (exitHomeWhenSpaceIsSelected == true) {
                    findNavController().navigate(R.id.switchSpaceAction)
                } else {
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skipCollapsed()
        expand()
    }

    override fun onStart() {
        vm.onStart()
        super.onStart()
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    override fun injectDependencies() {
        componentManager().selectSpaceComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectSpaceComponent.release()
    }

    companion object {
        const val EXIT_HOME_WHEN_SPACE_IS_SELECTED_KEY = "select.space.screen.arg.exit-to-desktop"
        fun args(exitHomeWhenSpaceIsSelected: Boolean) = bundleOf(
            EXIT_HOME_WHEN_SPACE_IS_SELECTED_KEY to exitHomeWhenSpaceIsSelected
        )
    }
}
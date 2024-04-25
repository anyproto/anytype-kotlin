package com.anytypeio.anytype.ui.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.multiplayer.SpaceListScreen
import com.anytypeio.anytype.core_ui.foundation.Warning
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.spaces.SpaceListViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class SpaceListFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: SpaceListViewModel.Factory

    private val vm by viewModels<SpaceListViewModel> { factory }

    @OptIn(ExperimentalMaterial3Api::class)
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
                SpaceListScreen(
                    state = vm.state.collectAsStateWithLifecycle().value,
                    onDeleteSpaceClicked = vm::onDeleteSpaceClicked,
                    onLeaveSpaceClicked = vm::onLeaveSpaceClicked,
                    onCancelJoinRequestClicked = vm::onCancelJoinSpaceClicked
                )
            }
            when(val warning = vm.warning.collectAsStateWithLifecycle().value) {
                is SpaceListViewModel.Warning.CancelSpaceJoinRequest -> {
                    ModalBottomSheet(
                        onDismissRequest = { vm.onWarningDismissed() },
                        dragHandle = {},
                        containerColor = colorResource(id = R.color.background_secondary)
                    ) {
                        Warning(
                            actionButtonText = stringResource(R.string.delete),
                            cancelButtonText = stringResource(R.string.back),
                            title = stringResource(R.string.delete_space_title),
                            subtitle = stringResource(R.string.delete_space_subtitle),
                            onNegativeClick = {
                                vm.onWarningDismissed()
                            },
                            onPositiveClick = {
                                vm.onCancelJoinRequestAccepted(warning.space)
                            },
                            isInProgress = false
                        )
                    }
                }
                is SpaceListViewModel.Warning.DeleteSpace -> {
                    ModalBottomSheet(
                        onDismissRequest = { vm.onWarningDismissed() },
                        dragHandle = {},
                        containerColor = colorResource(id = R.color.background_secondary)
                    ) {
                        Warning(
                            actionButtonText = stringResource(R.string.delete),
                            cancelButtonText = stringResource(R.string.back),
                            title = stringResource(R.string.delete_space_title),
                            subtitle = stringResource(R.string.delete_space_subtitle),
                            onNegativeClick = {
                                vm.onWarningDismissed()
                            },
                            onPositiveClick = {
                                vm.onDeleteSpaceAccepted(warning.space)
                            },
                            isInProgress = false
                        )
                    }
                }
                is SpaceListViewModel.Warning.LeaveSpace -> {
                    ModalBottomSheet(
                        onDismissRequest = { vm.onWarningDismissed() },
                        dragHandle = {},
                        containerColor = colorResource(id = R.color.background_secondary)
                    ) {
                        Warning(
                            actionButtonText = stringResource(R.string.multiplayer_leave_space),
                            cancelButtonText = stringResource(R.string.cancel),
                            title = stringResource(R.string.multiplayer_leave_space),
                            subtitle = stringResource(R.string.multiplayer_leave_space_warning_subtitle),
                            onNegativeClick = {
                                vm.onWarningDismissed()
                            },
                            onPositiveClick = {
                                vm.onLeaveSpaceAccepted(warning.space)
                            },
                            isInProgress = false
                        )
                    }
                }
                is SpaceListViewModel.Warning.None -> {
                    // Do nothing
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior(DEFAULT_PADDING_TOP)
    }

    override fun injectDependencies() {
        componentManager().spaceListComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().spaceListComponent.release()
    }
}
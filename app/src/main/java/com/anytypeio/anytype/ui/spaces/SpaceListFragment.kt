package com.anytypeio.anytype.ui.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.multiplayer.SpaceListScreen
import com.anytypeio.anytype.core_ui.foundation.Warning
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.spaces.SpaceListViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import kotlinx.coroutines.launch

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
                typography = typography,
                shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(16.dp))
            ) {
                SpaceListScreen(
                    state = vm.state.collectAsStateWithLifecycle().value,
                    onDeleteSpaceClicked = vm::onDeleteSpaceClicked,
                    onLeaveSpaceClicked = vm::onLeaveSpaceClicked,
                    onCancelJoinRequestClicked = vm::onCancelJoinSpaceClicked
                )
            }

            val bottomSheetState = rememberModalBottomSheetState()
            val scope = rememberCoroutineScope()

            when(val warning = vm.warning.collectAsStateWithLifecycle().value) {
                is SpaceListViewModel.Warning.CancelSpaceJoinRequest -> {
                    ModalBottomSheet(
                        onDismissRequest = { vm.onWarningDismissed() },
                        dragHandle = {},
                        containerColor = colorResource(id = R.color.background_secondary),
                        sheetState = bottomSheetState
                    ) {
                        Warning(
                            actionButtonText = stringResource(R.string.cancel),
                            cancelButtonText = stringResource(R.string.multiplayer_do_not_cancel_join_request),
                            title = stringResource(R.string.multiplayer_cancel_join_request),
                            subtitle = stringResource(R.string.multiplayer_cancel_join_request_msg),
                            onNegativeClick = {
                                scope.launch {
                                    bottomSheetState.hide()
                                }.invokeOnCompletion {
                                    vm.onWarningDismissed()
                                }
                            },
                            onPositiveClick = {
                                scope.launch {
                                    bottomSheetState.hide()
                                }.invokeOnCompletion {
                                    vm.onCancelJoinRequestAccepted(warning.space)
                                }
                            },
                            isInProgress = false,
                            footerHeight = 24.dp
                        )
                    }
                }
                is SpaceListViewModel.Warning.DeleteSpace -> {
                    ModalBottomSheet(
                        onDismissRequest = { vm.onWarningDismissed() },
                        dragHandle = {},
                        containerColor = colorResource(id = R.color.background_secondary),
                        sheetState = bottomSheetState
                    ) {
                        Warning(
                            actionButtonText = stringResource(R.string.delete),
                            cancelButtonText = stringResource(R.string.back),
                            title = stringResource(R.string.delete_space_title),
                            subtitle = stringResource(R.string.delete_space_subtitle),
                            onNegativeClick = {
                                scope.launch {
                                    bottomSheetState.hide()
                                }.invokeOnCompletion {
                                    vm.onWarningDismissed()
                                }
                            },
                            onPositiveClick = {
                                scope.launch {
                                    bottomSheetState.hide()
                                }.invokeOnCompletion {
                                    vm.onDeleteSpaceAccepted(warning.space)
                                }
                            },
                            isInProgress = false,
                            footerHeight = 24.dp
                        )
                    }
                }
                is SpaceListViewModel.Warning.LeaveSpace -> {
                    ModalBottomSheet(
                        onDismissRequest = { vm.onWarningDismissed() },
                        dragHandle = {},
                        containerColor = colorResource(id = R.color.background_secondary),
                        sheetState = bottomSheetState
                    ) {
                        Warning(
                            actionButtonText = stringResource(R.string.multiplayer_leave_space),
                            cancelButtonText = stringResource(R.string.cancel),
                            title = stringResource(R.string.multiplayer_leave_space),
                            subtitle = stringResource(R.string.multiplayer_leave_space_warning_subtitle),
                            onNegativeClick = {
                                scope.launch {
                                    bottomSheetState.hide()
                                }.invokeOnCompletion {
                                    vm.onWarningDismissed()
                                }
                            },
                            onPositiveClick = {
                                scope.launch {
                                    bottomSheetState.hide()
                                }.invokeOnCompletion {
                                    vm.onLeaveSpaceAccepted(warning.space)
                                }
                            },
                            isInProgress = false,
                            footerHeight = 24.dp
                        )
                    }
                }
                is SpaceListViewModel.Warning.None -> {
                   // Do nothing.
                }
            }
            LaunchedEffect(Unit) {
                vm.toasts.collect { msg ->
                    toast(msg)
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
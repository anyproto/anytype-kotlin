package com.anytypeio.anytype.ui.settings.space

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_utils.clipboard.copyPlainTextToClipboard
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.core_utils.ext.shareFile
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel.Command
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import com.anytypeio.anytype.ui.multiplayer.LeaveSpaceWarning
import com.anytypeio.anytype.ui.multiplayer.ShareSpaceFragment
import com.anytypeio.anytype.ui.settings.SpacesStorageFragment
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.spaces.DeleteSpaceWarning
import com.anytypeio.anytype.ui_settings.space.SpaceSettingsScreen
import java.io.File
import javax.inject.Inject
import timber.log.Timber

class SpaceSettingsFragment : BaseBottomSheetComposeFragment() {

    private val space get() = arg<Id>(ARG_SPACE_ID_KEY)

    @Inject
    lateinit var factory: SpaceSettingsViewModel.Factory

    @Inject
    lateinit var uriFileProvider: UriFileProvider

    private val vm by viewModels<SpaceSettingsViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = ComposeDialogView(
        context = requireContext(), dialog = requireDialog()
    ).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography,
                colors = MaterialTheme.colors.copy(
                    surface = colorResource(id = R.color.context_menu_background)
                )
            ) {
                SpaceSettingsScreen(
                    onNameSet = vm::onNameSet,
                    state = vm.spaceViewState.collectAsStateWithLifecycle().value,
                    onDeleteSpaceClicked = throttledClick(
                        onClick = { vm.onDeleteSpaceClicked() }
                    ),
                    onFileStorageClick = throttledClick(
                        onClick = {
                            findNavController().navigate(
                                resId = R.id.spacesStorageScreen,
                                args = SpacesStorageFragment.args(space = space)
                            )
                        }
                    ),
                    onPersonalizationClicked = throttledClick(
                        onClick = {
                            findNavController().navigate(R.id.personalizationScreen)
                        }
                    ),
                    onSpaceIdClicked = {
                        context.copyPlainTextToClipboard(
                            plainText = it,
                            label = "Space ID",
                            successToast = context.getString(R.string.space_id_copied_toast_msg)
                        )
                    },
                    onNetworkIdClicked = {
                        context.copyPlainTextToClipboard(
                            plainText = it,
                            label = "Network ID",
                            successToast = context.getString(R.string.network_id_copied_toast_msg)
                        )
                    },
                    onCreatedByClicked = {
                        context.copyPlainTextToClipboard(
                            plainText = it,
                            label = "Created-by ID",
                            successToast = context.getString(R.string.created_by_id_copied_toast_msg)
                        )
                    },
                    onDebugClicked = vm::onSpaceDebugClicked,
                    onRandomGradientClicked = vm::onRandomSpaceGradientClicked,
                    onManageSharedSpaceClicked = vm::onManageSharedSpaceClicked,
                    onSharePrivateSpaceClicked = vm::onSharePrivateSpaceClicked,
                    onAddMoreSpacesClicked = vm::onAddMoreSpacesClicked,
                    onSpaceImagePicked = { uri ->
                        runCatching {
                            vm.onSpaceImagePicked(
                                path = uri.parseImagePath(requireContext())
                            )
                        }.onFailure {
                            toast(getString(R.string.error_while_loading_picture))
                        }
                    }
                )
                LaunchedEffect(Unit) { vm.toasts.collect { toast(it) } }
                LaunchedEffect(Unit) {
                    vm.isDismissed.collect { isDismissed ->
                        if (isDismissed) dismiss()
                    }
                }
                LaunchedEffect(Unit) {
                    observeCommands()
                }
            }
        }
    }

    private suspend fun observeCommands() {
        vm.commands.collect { command ->
            when (command) {
                is Command.ShareSpaceDebug -> {
                    try {
                        shareFile(
                            uriFileProvider.getUriForFile(File(command.filepath))
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error while sharing space debug").also {
                            toast("Error while sharing space debug. Please try again later.")
                        }
                    }
                }
                is Command.ManageSharedSpace -> {
                    runCatching {
                        findNavController()
                            .navigate(
                                R.id.shareSpaceScreen,
                                ShareSpaceFragment.args(command.space)
                            )
                    }.onFailure {
                        Timber.e(it, "Error while opening share-space screen")
                    }
                }
                is Command.SharePrivateSpace -> {
                    runCatching {
                        findNavController()
                            .navigate(
                                R.id.shareSpaceScreen,
                                ShareSpaceFragment.args(command.space)
                            )
                    }.onFailure {
                        Timber.e(it, "Error while opening share-space screen")
                    }
                }
                is Command.ShowDeleteSpaceWarning -> {
                    val dialog = DeleteSpaceWarning.new()
                    dialog.onDeletionAccepted = {
                        dialog.dismiss()
                        vm.onDeleteSpaceAcceptedClicked()
                    }
                    dialog.onDeletionCancelled = {
                        vm.onDeleteSpaceWarningCancelled()
                    }
                    dialog.show(childFragmentManager, null)
                }
                is Command.ShowLeaveSpaceWarning -> {
                    val dialog = LeaveSpaceWarning.new()
                    dialog.onLeaveSpaceAccepted = {
                        dialog.dismiss()
                        vm.onLeaveSpaceAcceptedClicked()
                    }
                    dialog.onLeaveSpaceCancelled = {
                        vm.onDeleteSpaceWarningCancelled()
                    }
                    dialog.show(childFragmentManager, null)
                }
                is Command.ShowShareLimitReachedError -> {
                    toast(getString(R.string.multiplayer_toast_share_limit_reached))
                }
                Command.NavigateToMembership -> {
                    findNavController().navigate(R.id.paymentsScreen)
                }
                Command.NavigateToMembershipUpdate -> {
                    findNavController().navigate(R.id.membershipUpdateScreen)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expand()
        skipCollapsed()
    }

    override fun injectDependencies() {
        val vmParams = SpaceSettingsViewModel.VmParams(space = SpaceId(space))
        componentManager().spaceSettingsComponent.get(params = vmParams).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().spaceSettingsComponent.release()
    }

    companion object {
        const val ARG_SPACE_ID_KEY = "arg.space-settings.space-id"
        fun args(space: SpaceId) = bundleOf(ARG_SPACE_ID_KEY to space.id)
    }
}
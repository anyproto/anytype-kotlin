package com.anytypeio.anytype.ui.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.multiplayer.ChannelCreationType
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.spaces.CreateSpaceViewModel
import com.anytypeio.anytype.ui.home.WidgetsScreenFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject
import timber.log.Timber

class CreateSpaceFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: CreateSpaceViewModel.Factory

    private val vm by viewModels<CreateSpaceViewModel> { factory }

    private val args by navArgs<CreateSpaceFragmentArgs>()
    val channelType: ChannelCreationType get() = args.channelType
    val selectedMemberIdentities: List<String>
        get() = args.selectedMemberIdentities?.toList() ?: emptyList()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                if (uri != null) {
                    val path = uri.parseImagePath(inflater.context)
                    if (path != null) {
                        vm.onImageSelected(url = path)
                    }
                }
            }
        )

        CreateSpaceScreen(
            spaceIconView = vm.spaceIconView.collectAsState().value,
            selectedMembers = vm.selectedMembersView.collectAsStateWithLifecycle().value,
            onCreate = { name ->
                vm.onCreateSpace(
                    name = name
                )
            },
            onBackClicked = {
                findNavController().popBackStack()
            },
            onSpaceIconUploadClicked = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onSpaceIconRemoveClicked = {
                vm.onSpaceIconRemovedClicked()
            },
            isLoading = vm.isInProgress.collectAsState()
        )

        // Error handling with BaseAlertDialog
        val createSpaceError = vm.createSpaceError.collectAsStateWithLifecycle().value
        createSpaceError?.let { error ->
            BaseAlertDialog(
                dialogText = error.msg,
                buttonText = getString(R.string.button_ok),
                onButtonClick = { vm.clearCreateSpaceError() },
                onDismissRequest = { vm.clearCreateSpaceError() }
            )
        }
        LaunchedEffect(Unit) { vm.toasts.collect { toast(it) } }
        LaunchedEffect(Unit) {
            vm.isDismissed.collect { isDismissed ->
                if (isDismissed) findNavController().popBackStack()
            }
        }
        LaunchedEffect(Unit) {
            vm.commands.collect { command ->
                Timber.d("Received command: $command")
                when (command) {
                    is CreateSpaceViewModel.Command.SwitchSpaceWithHomepagePicker -> {
                        runCatching {
                            findNavController().navigate(R.id.exitToVaultAction)
                            findNavController().navigate(
                                R.id.actionOpenSpaceFromVault,
                                args = WidgetsScreenFragment.args(
                                    space = command.space.id,
                                    deeplink = null,
                                    showHomepagePicker = true
                                )
                            )
                        }.onFailure {
                            Timber.e(
                                it,
                                "Error while exiting to vault or opening homepage picker"
                            )
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
        (dialog as? BottomSheetDialog)?.findViewById<FrameLayout>(
            com.google.android.material.R.id.design_bottom_sheet
        )?.setBackgroundColor(requireContext().color(android.R.color.transparent))
    }

    override fun injectDependencies() {
        val vmParams = CreateSpaceViewModel.VmParams(
            channelType = channelType,
            selectedMemberIdentities = selectedMemberIdentities
        )
        componentManager().createSpaceComponent.get(vmParams).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createSpaceComponent.release()
    }
}
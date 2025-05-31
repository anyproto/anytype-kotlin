package com.anytypeio.anytype.ui.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.spaces.CreateSpaceViewModel
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import timber.log.Timber

class CreateSpaceFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: CreateSpaceViewModel.Factory

    private val vm by viewModels<CreateSpaceViewModel> { factory }

    private val spaceType get() = argString(ARG_SPACE_TYPE)

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
                val imagePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = { uri ->
                        if (uri != null) {
                            vm.onImageSelected(url = uri.parseImagePath(context))
                        }
                    }
                )

                CreateSpaceScreen(
                    spaceIconView = vm.spaceIconView.collectAsState().value,
                    onCreate = { name, isSpaceLevelChatSwitchChecked ->
                        vm.onCreateSpace(
                            name = name,
                            withChat = spaceType == TYPE_CHAT
                        )
                    },
                    onSpaceIconUploadClicked = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onSpaceIconRemoveClicked = {
                        vm.onSpaceIconRemovedClicked()
                    },
                    isLoading = vm.isInProgress.collectAsState(),
                    isChatSpace = spaceType == TYPE_CHAT
                )
                LaunchedEffect(Unit) { vm.toasts.collect { toast(it) } }
                LaunchedEffect(Unit) {
                    vm.isDismissed.collect { isDismissed ->
                        if (isDismissed) findNavController().popBackStack()
                    }
                }
                LaunchedEffect(Unit) {
                    vm.commands.collect { command ->
                        when(command) {
                            is CreateSpaceViewModel.Command.SwitchSpace -> {
                                runCatching {
                                    findNavController().navigate(R.id.exitToVaultAction)
                                    findNavController().navigate(
                                        R.id.actionOpenSpaceFromVault,
                                        args = HomeScreenFragment.args(
                                            space = command.space.id,
                                            deeplink = null
                                        )
                                    )
                                    command.startingObject
                                        ?.takeIf { it.isNotEmpty() }
                                        ?.let { startingObject ->
                                            findNavController().navigate(
                                                R.id.objectNavigation,
                                                EditorFragment.args(
                                                    ctx = startingObject,
                                                    space = command.space.id
                                                )
                                            )
                                        }
                                }.onFailure {
                                    Timber.e(it, "Error while exiting to vault or opening created space")
                                }
                            }
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
        componentManager().createSpaceComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createSpaceComponent.release()
    }

    companion object {
        const val ARG_SPACE_TYPE = "arg.space_type"

        const val TYPE_SPACE = "space"
        const val TYPE_CHAT = "chat"
    }
}
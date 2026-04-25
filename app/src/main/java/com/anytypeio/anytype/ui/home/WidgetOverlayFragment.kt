package com.anytypeio.anytype.ui.home

import android.Manifest
import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.ui.WallpaperResult
import com.anytypeio.anytype.core_utils.ext.isVideo
import com.anytypeio.anytype.core_models.ui.WallpaperView
import com.anytypeio.anytype.core_ui.widgets.SpaceBackground
import com.anytypeio.anytype.core_ui.widgets.toSpaceBackground
import com.anytypeio.anytype.core_utils.ext.argString
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectAction
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectViewModelFactory
import com.anytypeio.anytype.feature_create_object.presentation.NewCreateObjectViewModel
import com.anytypeio.anytype.feature_create_object.ui.CreateObjectPopup
import com.anytypeio.anytype.presentation.home.Command
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel
import com.anytypeio.anytype.presentation.home.HomeScreenVmParams
import com.anytypeio.anytype.presentation.main.MainViewModel
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.settings.space.SpaceSettingsFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

private const val WIDGET_OVERLAY_FAB_SCALE = 0.85f

class WidgetOverlayFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var factory: HomeScreenViewModel.Factory

    private val vm: HomeScreenViewModel by viewModels { factory }

    private lateinit var createObjectFactory: CreateObjectViewModelFactory

    private val createObjectVm by viewModels<NewCreateObjectViewModel> { createObjectFactory }

    private val mainVm: MainViewModel by activityViewModels()

    private val space: String get() = argString(ARG_SPACE_ID)

    override fun getTheme(): Int = R.style.WidgetOverlayBottomSheetTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        val vmParams = HomeScreenVmParams(
            spaceId = SpaceId(space),
            showHomepagePicker = false
        )
        componentManager().widgetOverlayComponent.get(vmParams).inject(this)
        val createObjectVmParams = NewCreateObjectViewModel.VmParams(
            spaceId = SpaceId(space),
            showAttachObject = false,
            showMediaSection = true
        )
        createObjectFactory = componentManager()
            .createObjectFeatureComponent
            .get(key = createObjectComponentKey(), param = createObjectVmParams)
            .viewModelFactory()
        super.onCreate(savedInstanceState)
    }

    private fun createObjectComponentKey(): String = "overlay-create-object:$space"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        dialog.behavior.isFitToContents = false
        dialog.behavior.isDraggable = true
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        WidgetOverlayContent(
            vm = vm,
            createObjectVm = createObjectVm,
            wallpaperState = mainVm.wallpaperState,
            onBackClicked = { dismiss() },
            onSpaceSettingsClicked = {
                vm.onSpaceSettingsClicked(space = SpaceId(space))
            },
            onCreateObjectAction = { action -> handleCreateObjectAction(action) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.commands.collect { command ->
                        // Most HomeScreenViewModel commands are widget-host flows
                        // owned by WidgetsScreenFragment. The overlay handles the
                        // two that originate from its own bottom buttons.
                        when (command) {
                            is Command.OpenGlobalSearchScreen -> {
                                runCatching {
                                    navigation().openGlobalSearch(
                                        space = command.space
                                    )
                                }.onFailure {
                                    Timber.e(it, "Error opening global search from overlay")
                                }
                                dismissAfterGesture()
                            }
                            else -> {
                                Timber.d("WidgetOverlay vm command (ignored): $command")
                            }
                        }
                    }
                }
                launch {
                    vm.navigation.collect { destination ->
                        proceed(destination)
                    }
                }
                launch { vm.toasts.collect { toast(it) } }
                launch {
                    vm.uploadSnackbar.collect { variant ->
                        mainVm.showSnackbarWithOk(uploadSnackbarMessage(variant))
                    }
                }
            }
        }
    }

    /**
     * Posts dismiss to the next frame. Dismissing synchronously during a
     * Compose long-press crashes the pointer-input continuation ("Already
     * resumed" — the BottomSheetDialog tear-down dispatches ACTION_CANCEL
     * back into a coroutine that has already resumed). Yielding to the
     * main-thread queue lets the gesture finish cleanly first.
     */
    private fun dismissAfterGesture() {
        val v = view ?: run {
            if (isAdded) dismiss()
            return
        }
        v.post { if (isAdded) dismiss() }
    }

    private fun proceed(destination: HomeScreenViewModel.Navigation) {
        Timber.d("WidgetOverlay destination: $destination")
        // Always dismiss the overlay first so the back-stack does not contain the sheet.
        dismiss()
        when (destination) {
            is HomeScreenViewModel.Navigation.OpenObject -> runCatching {
                navigation().openDocument(
                    target = destination.ctx,
                    space = destination.space
                )
            }.onFailure { Timber.e(it, "Error opening document from overlay") }

            is HomeScreenViewModel.Navigation.OpenSet -> runCatching {
                navigation().openObjectSet(
                    target = destination.ctx,
                    space = destination.space,
                    view = destination.view
                )
            }.onFailure { Timber.e(it, "Error opening object set from overlay") }

            is HomeScreenViewModel.Navigation.OpenChat -> runCatching {
                navigation().openChat(
                    target = destination.ctx,
                    space = destination.space,
                    popUpToVault = false
                )
            }.onFailure { Timber.e(it, "Error opening chat from overlay") }

            is HomeScreenViewModel.Navigation.ExpandWidget -> runCatching {
                navigation().launchCollections(
                    subscription = destination.subscription,
                    space = destination.space
                )
            }.onFailure { Timber.e(it, "Error expanding widget from overlay") }

            is HomeScreenViewModel.Navigation.OpenAllContent -> runCatching {
                navigation().openAllContent(space = destination.space)
            }.onFailure { Timber.e(it, "Error opening all content from overlay") }

            is HomeScreenViewModel.Navigation.OpenDateObject -> runCatching {
                navigation().openDateObject(
                    objectId = destination.ctx,
                    space = destination.space
                )
            }.onFailure { Timber.e(it, "Error opening date object from overlay") }

            is HomeScreenViewModel.Navigation.OpenParticipant -> runCatching {
                navigation().openParticipantObject(
                    objectId = destination.objectId,
                    space = destination.space
                )
            }.onFailure { Timber.e(it, "Error opening participant from overlay") }

            is HomeScreenViewModel.Navigation.OpenType -> runCatching {
                navigation().openObjectType(
                    objectId = destination.target,
                    space = destination.space,
                    view = destination.view
                )
            }.onFailure { Timber.e(it, "Error opening object type from overlay") }

            is HomeScreenViewModel.Navigation.OpenOwnerOrEditorSpaceSettings -> runCatching {
                // The widgets-scoped action ids are not reachable from the overlay's
                // host destination (chat/editor/object set), so navigate directly to
                // the global spaceSettingsScreen destination with the space arg.
                findNavController().navigate(
                    R.id.spaceSettingsScreen,
                    SpaceSettingsFragment.args(space = SpaceId(destination.space))
                )
            }.onFailure { Timber.e(it, "Error opening space settings from overlay") }

            is HomeScreenViewModel.Navigation.OpenBookmarkUrl -> {
                try {
                    ActivityCustomTabsHelper.openUrl(
                        activity = requireActivity(),
                        url = destination.url
                    )
                } catch (e: Throwable) {
                    Timber.e(e, "Error opening bookmark URL from overlay: ${destination.url}")
                    toast("Failed to open URL")
                }
            }

            is HomeScreenViewModel.Navigation.OpenTemplate -> runCatching {
                navigation().openModalTemplateEdit(
                    template = destination.template,
                    templateTypeId = destination.templateTypeId,
                    templateTypeKey = destination.templateTypeKey,
                    space = destination.space
                )
            }.onFailure { Timber.e(it, "Error opening template from overlay") }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        vm.onStart()
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        componentManager().createObjectFeatureComponent.release(createObjectComponentKey())
        componentManager().widgetOverlayComponent.release()
        super.onDestroy()
    }

    private fun handleCreateObjectAction(action: CreateObjectAction) {
        when (action) {
            is CreateObjectAction.CreateObjectOfType -> {
                vm.onCreateNewObjectOfTypeKey(typeKey = TypeKey(action.typeKey))
            }
            is CreateObjectAction.UpdateSearch,
            is CreateObjectAction.Retry -> {
                createObjectVm.onAction(action)
            }
            CreateObjectAction.AttachExistingObject -> {
                Timber.d("CreateObjectPopup attach action received unexpectedly")
                vm.hideCreateObjectSheet()
            }
            CreateObjectAction.SelectPhotos,
            CreateObjectAction.TakePhoto,
            CreateObjectAction.SelectFiles -> {
                // Media actions are handled inside WidgetOverlayContent where the
                // Activity-result launchers live. This branch is unreachable.
            }
        }
    }

    companion object {
        private const val ARG_SPACE_ID = "space_id"
        private const val TAG = "WidgetOverlayFragment"

        fun show(fm: FragmentManager, spaceId: String) {
            WidgetOverlayFragment().apply {
                arguments = bundleOf(ARG_SPACE_ID to spaceId)
            }.show(fm, TAG)
        }
    }
}

@Composable
private fun WidgetOverlayContent(
    vm: HomeScreenViewModel,
    createObjectVm: NewCreateObjectViewModel,
    wallpaperState: StateFlow<WallpaperResult>,
    onBackClicked: () -> Unit,
    onSpaceSettingsClicked: () -> Unit,
    onCreateObjectAction: (CreateObjectAction) -> Unit,
) {
    val wallpaper by wallpaperState.collectAsStateWithLifecycle()
    val spaceBackground = wallpaper.toSpaceBackground()
    val wallpaperAlpha = WallpaperView.WALLPAPER_DEFAULT_ALPHA / 255f
    val scaledFabSize = dimensionResource(
        com.anytypeio.anytype.core_ui.R.dimen.nav_fab_button_size
    ) * WIDGET_OVERLAY_FAB_SCALE

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 8.dp, bottom = 16.dp)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colorResource(R.color.background_primary))
                .then(
                    when (spaceBackground) {
                        is SpaceBackground.SolidColor -> Modifier.background(
                            color = spaceBackground.color.copy(alpha = wallpaperAlpha)
                        )
                        is SpaceBackground.Gradient -> Modifier.background(
                            brush = spaceBackground.brush,
                            alpha = wallpaperAlpha
                        )
                        SpaceBackground.None -> Modifier
                    }
                )
                .nestedScroll(rememberNestedScrollInteropConnection())
        ) {
            WidgetsScreen(
                viewModel = vm,
                fabSize = scaledFabSize,
                topContentPadding = 52.dp,
                bottomContentPadding = 16.dp,
            )
        }
        HomeScreenToolbar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 12.dp)
                .padding(horizontal = 8.dp),
            onBackButtonClicked = onBackClicked,
            onSpaceSettingsClicked = onSpaceSettingsClicked,
            buttonSize = scaledFabSize,
        )

        val createObjectSheetVisible by vm.createObjectSheetVisible.collectAsStateWithLifecycle()
        val createObjectState by createObjectVm.state.collectAsStateWithLifecycle()
        LaunchedEffect(createObjectSheetVisible) {
            if (createObjectSheetVisible) createObjectVm.onOpen()
        }

        val uploadContext = LocalContext.current
        val uploadMediaLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia()
        ) { uris ->
            vm.onUploadFilesToSpace(
                uris.map { uri ->
                    val type = if (isVideo(uri, uploadContext))
                        Block.Content.File.Type.VIDEO
                    else
                        Block.Content.File.Type.IMAGE
                    HomeScreenViewModel.UploadToSpaceTarget(uri.toString(), type)
                }
            )
        }
        val uploadFileLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->
            vm.onUploadFilesToSpace(
                uris.map { uri ->
                    HomeScreenViewModel.UploadToSpaceTarget(
                        uri.toString(),
                        Block.Content.File.Type.NONE
                    )
                }
            )
        }
        var capturedPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }
        var capturedPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }
        val takePhotoLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { isSuccess ->
            val uri = capturedPhotoUri
            val sourcePath = capturedPhotoPath
            if (isSuccess && uri != null) {
                vm.onUploadFilesToSpace(
                    listOf(
                        HomeScreenViewModel.UploadToSpaceTarget(
                            uri = uri,
                            type = Block.Content.File.Type.IMAGE,
                            sourceFilePath = sourcePath
                        )
                    )
                )
            } else if (sourcePath != null) {
                runCatching { java.io.File(sourcePath).delete() }
            }
            capturedPhotoUri = null
            capturedPhotoPath = null
        }
        val takePhotoPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                launchCameraForHomeUpload(
                    context = uploadContext,
                    launcher = takePhotoLauncher,
                    onPhotoReady = { uri, file ->
                        capturedPhotoUri = uri.toString()
                        capturedPhotoPath = file.absolutePath
                    }
                )
            } else {
                Timber.w("Camera permission denied for overlay upload")
            }
        }

        CreateObjectPopup(
            expanded = createObjectSheetVisible,
            onDismissRequest = { vm.hideCreateObjectSheet() },
            state = createObjectState,
            onAction = { action ->
                when (action) {
                    CreateObjectAction.SelectPhotos -> {
                        uploadMediaLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                            )
                        )
                        vm.hideCreateObjectSheet()
                    }
                    CreateObjectAction.TakePhoto -> {
                        takePhotoPermissionLauncher.launch(Manifest.permission.CAMERA)
                        vm.hideCreateObjectSheet()
                    }
                    CreateObjectAction.SelectFiles -> {
                        uploadFileLauncher.launch(arrayOf("*/*"))
                        vm.hideCreateObjectSheet()
                    }
                    else -> onCreateObjectAction(action)
                }
            }
        )
    }
}

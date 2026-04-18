package com.anytypeio.anytype.ui.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions.Builder
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_utils.ext.isVideo
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectAction
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectViewModelFactory
import com.anytypeio.anytype.feature_create_object.presentation.NewCreateObjectViewModel
import com.anytypeio.anytype.feature_create_object.ui.CreateObjectPopup
import java.io.File
import com.anytypeio.anytype.core_ui.features.multiplayer.QrCodeScreen
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.intents.ActivityCustomTabsHelper
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.other.DefaultDeepLinkResolver
import com.anytypeio.anytype.presentation.home.Command
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel
import com.anytypeio.anytype.presentation.main.MainViewModel
import com.anytypeio.anytype.presentation.notifications.UploadSuccessSnackbar
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.Navigation
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.ViewerSpaceSettingsState
import com.anytypeio.anytype.presentation.home.HomeScreenVmParams
import com.anytypeio.anytype.presentation.spaces.UiEvent
import com.anytypeio.anytype.presentation.spaces.UiSpaceQrCodeState
import com.anytypeio.anytype.ui.base.navigation
import com.anytypeio.anytype.ui.gallery.GalleryInstallationFragment
import com.anytypeio.anytype.ui.multiplayer.LeaveSpaceWarning
import com.anytypeio.anytype.ui.multiplayer.RequestJoinSpaceFragment
import com.anytypeio.anytype.ui.multiplayer.ShareSpaceFragment
import com.anytypeio.anytype.ui.objects.creation.WidgetSourceTypeFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeSelectionListener
import com.anytypeio.anytype.ui.objects.types.pickers.WidgetSourceTypeListener
import com.anytypeio.anytype.ui.payments.MembershipFragment
import com.anytypeio.anytype.ui.settings.space.SpaceSettingsFragment
import com.anytypeio.anytype.ui.widgets.CreateChatObjectFragment
import com.anytypeio.anytype.ui.widgets.CreateChatObjectListener
import com.anytypeio.anytype.ui.widgets.SelectWidgetSourceFragment
import com.anytypeio.anytype.ui.widgets.SelectWidgetTypeFragment
import com.anytypeio.anytype.ui_settings.space.new_settings.ViewerSpaceSettings
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class WidgetsScreenFragment : Fragment(),
    ObjectTypeSelectionListener,
    WidgetSourceTypeListener,
    CreateChatObjectListener {

    private val deepLink: String? get() = argOrNull(DEEP_LINK_KEY)

    private val space: Id get() = arg<Id>(SPACE_ID_KEY)

    private var isMnemonicReminderDialogNeeded: Boolean
        get() = argOrNull<Boolean>(SHOW_MNEMONIC_KEY) == true
        set(value) {
            arguments?.putBoolean(SHOW_MNEMONIC_KEY, value)
        }

    @Inject
    lateinit var factory: HomeScreenViewModel.Factory

    private lateinit var createObjectFactory: CreateObjectViewModelFactory

    private val vm by viewModels<HomeScreenViewModel> { factory }

    private val createObjectVm by viewModels<NewCreateObjectViewModel> { createObjectFactory }

    private val mainVm: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val vmParams = HomeScreenVmParams(
            spaceId = SpaceId(space),
            showHomepagePicker = argOrNull<Boolean>(SHOW_HOMEPAGE_PICKER_KEY) ?: false
        )
        componentManager().homeScreenComponent.get(vmParams).inject(this)
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

    override fun onDestroy() {
        componentManager().createObjectFeatureComponent.release(createObjectComponentKey())
        componentManager().homeScreenComponent.release()
        super.onDestroy()
    }

    private fun createObjectComponentKey(): String = "widgets-create-object:$space"

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = content {
        Box(modifier = Modifier.fillMaxSize()) {
            WidgetsScreen(
                viewModel = vm
            )
            HomeScreenToolbar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding(),
                onBackButtonClicked = vm::onBackClicked,
                onSpaceSettingsClicked = { vm.onSpaceSettingsClicked(space = SpaceId(space)) }
            )
        }

        val spaceSettingsState = vm.viewerSpaceSettingsState.collectAsStateWithLifecycle().value

        if (spaceSettingsState is ViewerSpaceSettingsState.Visible) {
            ModalBottomSheet(
                containerColor = Color.Transparent,
                onDismissRequest = vm::onDismissViewerSpaceSettings,
                dragHandle = null,
                content = {
                    ViewerSpaceSettings(
                        title = spaceSettingsState.name,
                        icon = spaceSettingsState.icon,
                        description = spaceSettingsState.description,
                        info = spaceSettingsState.techInfo,
                        inviteLink = spaceSettingsState.inviteLink,
                        uiEvent = {
                            vm.onViewerSpaceSettingsUiEvent(
                                space = SpaceId(space),
                                uiEvent = it
                            )
                        }
                    )
                }
            )
        }
        // QR Code Modal
        when (val qrCodeState = vm.uiQrCodeState.collectAsStateWithLifecycle().value) {
            is UiSpaceQrCodeState.SpaceInvite -> {
                QrCodeScreen(
                    spaceName = qrCodeState.spaceName,
                    link = qrCodeState.link,
                    icon = qrCodeState.icon,
                    onShare = { link ->
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, link)
                            type = "text/plain"
                        }
                        startActivity(Intent.createChooser(intent, null))
                    },
                    onDismiss = { vm.onHideQrCodeScreen() }
                )
            }

            else -> {}
        }

        val pendingBundledWidgetId =
            vm.pendingBundledWidgetDeletion.collectAsStateWithLifecycle().value
        if (pendingBundledWidgetId != null) {
            UnpinWidgetScreen(
                onPinCancelled = { vm.onBundledWidgetDeletionCanceled() },
                onPinAccepted = {
                    vm.onBundledWidgetDeletionConfirmed()
                }
            )
        }

        val createObjectSheetVisible =
            vm.createObjectSheetVisible.collectAsStateWithLifecycle().value
        val createObjectState =
            createObjectVm.state.collectAsStateWithLifecycle().value
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
                // Capture cancelled/failed — still clean up the empty temp file.
                runCatching { File(sourcePath).delete() }
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
                Timber.w("Camera permission denied for home upload")
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
                    else -> handleCreateObjectAction(action)
                }
            }
        )

        // Homepage Picker - shown as bottom sheet after channel creation or from Create Home widget
        val showHomepagePicker = vm.showHomepagePicker.collectAsStateWithLifecycle().value
        if (showHomepagePicker) {
            HomepagePickerBottomSheet(
                onHomepageSelected = vm::onHomepageSelected,
                onLaterClicked = vm::onHomepagePickerDismissed,
                onDismiss = vm::onHomepagePickerDismissed
            )
        }

        BackHandler {
            vm.onBackClicked()
        }
    }

    override fun onStart() {
        Timber.d("onStart")
        super.onStart()
        vm.onStart()
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.commands.collect { command -> proceed(command) } }
                launch { vm.navigation.collect { command -> proceed(command) } }
                launch { vm.toasts.collect { toast(it) } }
                launch {
                    vm.uploadSnackbar.collect { variant ->
                        mainVm.showSnackbarWithOk(uploadSnackbarMessage(variant))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isMnemonicReminderDialogNeeded)
            showMnemonicReminderAlert()
        proceedWithDeepLinks()
    }

    private fun proceedWithDeepLinks() {
        val deepLinkFromFragmentArgs = deepLink
        if (deepLinkFromFragmentArgs != null) {
            Timber.d("Deeplink  from fragment args")
            vm.onResume(DefaultDeepLinkResolver.resolve(deepLinkFromFragmentArgs))
            arguments?.putString(DEEP_LINK_KEY, null)
        } else {
            vm.onResume(null)
        }
    }

    private fun proceed(command: Command) {
        when (command) {
            is Command.ChangeWidgetSource -> {
                runCatching {
                    findNavController().navigate(
                        R.id.selectWidgetSourceScreen,
                        args = SelectWidgetSourceFragment.args(
                            ctx = command.ctx,
                            widget = command.widget,
                            source = command.source,
                            type = command.type,
                            isInEditMode = command.isInEditMode,
                            spaceId = command.space
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while navigation")
                }
            }

            is Command.SelectWidgetSource -> {
                runCatching {
                    findNavController().navigate(
                        R.id.selectWidgetSourceScreen,
                        args = SelectWidgetSourceFragment.args(
                            ctx = command.ctx,
                            target = command.target,
                            isInEditMode = command.isInEditMode,
                            spaceId = command.space
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while navigation")
                }
            }

            is Command.ChangeWidgetType -> {
                runCatching {
                    findNavController().navigate(
                        R.id.selectWidgetTypeScreen,
                        args = SelectWidgetTypeFragment.args(
                            ctx = command.ctx,
                            widget = command.widget,
                            source = command.source,
                            type = command.type,
                            layout = command.layout,
                            isInEditMode = command.isInEditMode
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while navigation")
                }
            }

            is Command.SelectWidgetType -> {
                runCatching {
                    findNavController().navigate(
                        R.id.selectWidgetTypeScreen,
                        args = SelectWidgetTypeFragment.args(
                            ctx = command.ctx,
                            source = command.source,
                            layout = command.layout,
                            target = command.target,
                            isInEditMode = command.isInEditMode
                        )
                    )
                }.onFailure {
                    Timber.e(it, "Error while navigation")
                }
            }

            is Command.Deeplink.Invite -> {
                findNavController().navigate(
                    R.id.requestJoinSpaceScreen,
                    RequestJoinSpaceFragment.args(link = command.link)
                )
            }

            is Command.Deeplink.GalleryInstallation -> {
                findNavController().navigate(
                    R.id.galleryInstallationScreen,
                    GalleryInstallationFragment.args(
                        deepLinkType = command.deepLinkType,
                        deepLinkSource = command.deepLinkSource
                    )
                )
            }

            is Command.Deeplink.MembershipScreen -> {
                runCatching {
                    findNavController().navigate(
                        R.id.paymentsScreen,
                        MembershipFragment.args(command.tierId),
                        Builder().setLaunchSingleTop(true).build()
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening membership screen")
                }
            }

            is Command.Deeplink.DeepLinkToObjectNotWorking -> {
                toast(
                    getString(R.string.multiplayer_deeplink_to_your_object_error)
                )
            }

            is Command.ShareSpace -> {
                runCatching {
                    findNavController().navigate(
                        R.id.shareSpaceScreen,
                        args = ShareSpaceFragment.args(command.space)
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening share screen")
                }
            }

            is Command.ShowLeaveSpaceWarning -> {
                val dialog = LeaveSpaceWarning.new()
                dialog.onLeaveSpaceAccepted = {
                    dialog.dismiss()
                    vm.onLeaveSpaceAcceptedClicked(SpaceId(space))
                }
                dialog.show(childFragmentManager, null)
            }

            is Command.CreateSourceForNewWidget -> {
                val dialog = WidgetSourceTypeFragment.new(
                    space = command.space.id,
                    widgetId = command.widgets
                )
                dialog.show(childFragmentManager, null)
            }

            is Command.OpenSpaceSettings -> {
                runCatching {
                    findNavController().navigate(
                        R.id.action_open_space_settings,
                        SpaceSettingsFragment.args(space = command.spaceId)
                    )
                }.onFailure { e ->
                    Timber.e(e, "Error while opening space settings")
                }
            }

            is Command.OpenGlobalSearchScreen -> {
                runCatching {
                    navigation().openGlobalSearch(
                        space = command.space
                    )
                }.onFailure {
                    Timber.e(it, "Error while opening global search screen")
                }
            }

            is Command.OpenVault -> {
                runCatching {
                    findNavController().navigate(R.id.action_open_vault)
                }.onFailure {
                    Timber.e(it, "Error while opening vault from home screen")
                }
            }

            is Command.HandleChatSpaceBackNavigation -> {
                runCatching {
                    // Deterministic navigation based on space kind, not back stack
                    val chatId = command.spaceChatId
                    if (command.isOneToOneSpace && !chatId.isNullOrEmpty()) {
                        // 1-1 (DM) space: navigate to chat object
                        navigation().openChat(
                            target = chatId,
                            space = space
                        )
                    } else {
                        // Regular data space: always navigate to vault
                        vm.proceedWithExitingToVault()
                        findNavController().navigate(R.id.action_back_on_vault)
                    }
                }.onFailure {
                    Timber.e(it, "Error while handling home screen back navigation")
                }
            }

            is Command.ShareInviteLink -> {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, command.link)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(intent, null))
            }

            is Command.CreateNewType -> {
                runCatching {
                    navigation().openCreateObjectTypeScreen(spaceId = command.space)
                }.onFailure { e ->
                    Timber.e(e, "Error while opening create new type screen")
                }
            }

            is Command.CreateChatObject -> {
                val dialog = CreateChatObjectFragment.new(space = command.space.id)
                dialog.show(childFragmentManager, "create-chat-object-dialog")
            }

            is Command.OpenManageSections -> {
                runCatching {
                    findNavController().navigate(R.id.action_open_manage_sections)
                }.onFailure { e ->
                    Timber.e(e, "Error while opening manage sections screen")
                }
            }
            
            is Command.Toast.SpaceMuted -> {
                toast(getString(com.anytypeio.anytype.localization.R.string.multiplayer_space_muted))
            }
            
            is Command.Toast.SpaceUnmuted -> {
                toast(getString(com.anytypeio.anytype.localization.R.string.multiplayer_space_unmuted))
            }
            
            is Command.Toast.UnableToChangeNotificationSettings -> {
                toast(getString(com.anytypeio.anytype.localization.R.string.multiplayer_unable_to_change_notification_settings))
            }
            
            is Command.Toast.FailedToChangeNotificationSettings -> {
                toast(getString(com.anytypeio.anytype.localization.R.string.multiplayer_failed_to_change_notification_settings))
            }
        }
    }

    private fun proceed(destination: Navigation) {
        Timber.d("New destination: $destination")
        when (destination) {
            is Navigation.OpenObject -> runCatching {
                navigation().openDocument(
                    target = destination.ctx,
                    space = destination.space
                )
            }

            is Navigation.OpenSet -> runCatching {
                navigation().openObjectSet(
                    target = destination.ctx,
                    space = destination.space,
                    view = destination.view
                )
            }

            is Navigation.OpenChat -> runCatching {
                navigation().openChat(
                    target = destination.ctx,
                    space = destination.space,
                    popUpToVault = false
                )
            }

            is Navigation.ExpandWidget -> runCatching {
                navigation().launchCollections(
                    subscription = destination.subscription,
                    space = destination.space
                )
            }

            is Navigation.OpenAllContent -> {
                runCatching {
                    navigation().openAllContent(space = destination.space)
                }.onFailure { e ->
                    Timber.e(e, "Error while opening all content from widgets")
                }
            }

            is Navigation.OpenDateObject -> {
                runCatching {
                    navigation().openDateObject(
                        objectId = destination.ctx,
                        space = destination.space
                    )
                }.onFailure { e ->
                    Timber.e(e, "Error while opening date object from widgets")
                }
            }

            is Navigation.OpenParticipant -> {
                runCatching {
                    navigation().openParticipantObject(
                        objectId = destination.objectId,
                        space = destination.space
                    )
                }.onFailure { e ->
                    Timber.e(e, "Error while opening participant from widgets")
                }
            }

            is Navigation.OpenType -> {
                runCatching {
                    navigation().openObjectType(
                        objectId = destination.target,
                        space = destination.space,
                        view = destination.view
                    )
                }.onFailure { e ->
                    Timber.e(e, "Error while opening participant from widgets")
                }
            }

            is Navigation.OpenOwnerOrEditorSpaceSettings -> {
                runCatching {
                    findNavController()
                        .navigate(
                            R.id.action_open_space_settings_from_widgets,
                            SpaceSettingsFragment.args(
                                space = SpaceId(space)
                            )
                        )
                }.onFailure {
                    Timber.e(it, "Error while opening space settings")
                }
            }

            is Navigation.OpenBookmarkUrl -> {
                try {
                    ActivityCustomTabsHelper.openUrl(
                        activity = requireActivity(),
                        url = destination.url
                    )
                } catch (e: Throwable) {
                    Timber.e(e, "Error opening bookmark URL: ${destination.url}")
                    toast("Failed to open URL")
                }
            }

            is Navigation.OpenTemplate -> {
                runCatching {
                    navigation().openModalTemplateEdit(
                        template = destination.template,
                        templateTypeId = destination.templateTypeId,
                        templateTypeKey = destination.templateTypeKey,
                        space = destination.space
                    )
                }.onFailure { e ->
                    Timber.e(e, "Error while opening template from widgets")
                }
            }
        }
    }

    private fun showMnemonicReminderAlert() {
        isMnemonicReminderDialogNeeded = false
        findNavController().navigate(R.id.dashboardKeychainDialog)
    }

    override fun onSetNewWidgetSource(objType: ObjectWrapper.Type, widgetId: Id) {
        vm.onNewWidgetSourceTypeSelected(type = objType, widgets = widgetId)
    }

    override fun onSelectObjectType(objType: ObjectWrapper.Type) {
        vm.onCreateNewObjectClicked(objType = objType)
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
                // Media actions are handled in the popup's onAction lambda where
                // the Activity-result launchers live. This branch is unreachable.
            }
        }
    }

    override fun onChatObjectCreated(objectId: Id) {
        Timber.d("Chat object created from widget: $objectId")
        navigation().openChat(
            target = objectId,
            space = space,
            popUpToVault = false
        )
    }

    companion object {
        const val SHOW_MNEMONIC_KEY = "arg.home-screen.show-mnemonic"
        const val DEEP_LINK_KEY = "arg.home-screen.deep-link"
        const val SPACE_ID_KEY = "arg.home-screen.space-id"
        const val SHOW_HOMEPAGE_PICKER_KEY = "arg.home-screen.show-homepage-picker"

        fun args(
            space: Id,
            deeplink: String? = null,
            showHomepagePicker: Boolean = false
        ): Bundle = bundleOf(
            DEEP_LINK_KEY to deeplink,
            SPACE_ID_KEY to space,
            SHOW_HOMEPAGE_PICKER_KEY to showHomepagePicker
        )
    }
}

/**
 * Writes a temp JPEG into the app cache, gives the launcher the FileProvider URI,
 * and reports the URI back via [onUriReceived] so callers can upload on capture.
 * Mirrors `feature-chats/tools/launchCamera` but lives here so Home/Widgets can
 * invoke it without depending on the chats module.
 */
internal fun launchCameraForHomeUpload(
    context: android.content.Context,
    launcher: androidx.activity.compose.ManagedActivityResultLauncher<Uri, Boolean>,
    onPhotoReady: (uri: Uri, file: File) -> Unit
) {
    val tempDir = File(context.cacheDir, HOME_UPLOAD_TEMP_FOLDER)
    if (!tempDir.exists()) tempDir.mkdirs()
    val photoFile = File.createTempFile("IMG_", ".jpg", tempDir).apply {
        createNewFile()
        deleteOnExit()
    }
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        photoFile
    )
    onPhotoReady(uri, photoFile)
    launcher.launch(uri)
}

private const val HOME_UPLOAD_TEMP_FOLDER = "home_upload_temp_folder"

/**
 * Resolve the user-facing snackbar message for an [UploadSuccessSnackbar]
 * variant. Shared by fragments that host upload entry points (Widgets,
 * Widget Overlay, Chat).
 */
internal fun Fragment.uploadSnackbarMessage(variant: UploadSuccessSnackbar): String =
    when (variant) {
        UploadSuccessSnackbar.Image -> getString(R.string.upload_success_snackbar_image)
        UploadSuccessSnackbar.Video -> getString(R.string.upload_success_snackbar_video)
        UploadSuccessSnackbar.File -> getString(R.string.upload_success_snackbar_file)
        UploadSuccessSnackbar.Mixed -> getString(R.string.upload_success_snackbar_mixed)
    }
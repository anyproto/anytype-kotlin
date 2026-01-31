package com.anytypeio.anytype.ui.chats

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.features.multiplayer.ShareSpaceQrCodeScreen
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.BUTTON_WARNING
import com.anytypeio.anytype.core_ui.foundation.GenericAlert
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.openAppSettings
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.core_utils.ext.safeNavigate
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.intents.SystemAction
import com.anytypeio.anytype.core_utils.intents.SystemAction.OpenUrl
import com.anytypeio.anytype.core_utils.intents.proceedWithAction
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ext.FragmentResultContract
import com.anytypeio.anytype.ext.daggerViewModel
import com.anytypeio.anytype.feature_chats.presentation.ChatObjectIcon
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModelFactory
import com.anytypeio.anytype.feature_chats.tools.LinkDetector.ANYTYPE_PREFIX
import com.anytypeio.anytype.feature_chats.tools.LinkDetector.FILE_PREFIX
import com.anytypeio.anytype.feature_chats.tools.LinkDetector.MAILTO_PREFIX
import com.anytypeio.anytype.feature_chats.tools.LinkDetector.TEL_PREFIX
import com.anytypeio.anytype.feature_chats.ui.ChatInfoScreenState
import com.anytypeio.anytype.feature_chats.ui.ChatScreenWrapper
import com.anytypeio.anytype.feature_chats.ui.ChatTopToolbar
import com.anytypeio.anytype.feature_chats.ui.EditChatInfoScreen
import com.anytypeio.anytype.feature_chats.ui.NotificationPermissionContent
import com.anytypeio.anytype.feature_vault.ui.AlertScreenModals
import com.anytypeio.anytype.presentation.search.GlobalSearchViewModel
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.home.WidgetsScreenFragment
import com.anytypeio.anytype.ui.media.MediaActivity
import com.anytypeio.anytype.ui.multiplayer.ShareSpaceFragment
import com.anytypeio.anytype.ui.primitives.ObjectTypeFragment
import com.anytypeio.anytype.ui.profile.ParticipantFragment
import com.anytypeio.anytype.ui.search.GlobalSearchScreen
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import timber.log.Timber

class ChatFragment : Fragment() {

    @Inject
    lateinit var factory: ChatViewModelFactory

    private val vm by viewModels<ChatViewModel> { factory }

    val ctx get() = arg<Id>(CTX_KEY)
    private val space get() = arg<Id>(SPACE_KEY)

    private val triggeredByPush get() = arg<Boolean>(TRIGGERED_BY_PUSH_KEY)
    private val popUpToVault get() = arg<Boolean>(POP_UP_TO_VAULT_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        releaseDependencies()
        super.onDestroy()
    }

    // Rendering
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = content {
        MaterialTheme(typography = typography) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val notificationsSheetState =
                rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val moveToBinSheetState =
                rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val chatInfoSheetState =
                rememberModalBottomSheetState(skipPartiallyExpanded = true)
            var showGlobalSearchBottomSheet by remember { mutableStateOf(false) }
            var showChatInfoScreen by remember { mutableStateOf(false) }
            var chatInfoData by remember { mutableStateOf<Pair<String, ObjectIcon>?>(null) }
            val showNotificationPermissionDialog =
                vm.showNotificationPermissionDialog.collectAsStateWithLifecycle().value
            val showMoveToBinDialog =
                vm.showMoveToBinDialog.collectAsStateWithLifecycle().value

            ErrorScreen()

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0.dp),
                topBar = {
                    ChatTopToolbar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding(),
                        header = vm.header.collectAsStateWithLifecycle().value,
                        onBackButtonClicked = {
                            vm.onBackButtonPressed(isExitingVault = popUpToVault)
                        },
                        onSpaceNameClicked = vm::onSpaceIconClicked,
                        onSpaceIconClicked = vm::onSpaceIconClicked,
                        onInviteMembersClicked = vm::onInviteMembersClicked,
                        onEditInfo = vm::onEditInfo,
                        onPin = vm::onPinChatAsWidget,
                        onCopyLink = vm::onCopyChatLink,
                        onMoveToBin = vm::onMoveToBin,
                        onNotificationSettingChanged = vm::onNotificationSettingChanged
                    )
                }
            ) { paddingValues ->
                ChatScreenWrapper(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .navigationBarsPadding(),
                    vm = vm,
                    onAttachObjectClicked = { showGlobalSearchBottomSheet = true },
                    onMarkupLinkClicked = { url ->
                        val handled = when {
                            url.startsWith(MAILTO_PREFIX) -> {
                                val action = SystemAction.MailTo(url.removePrefix(MAILTO_PREFIX))
                                proceedWithAction(action)
                            }

                            url.startsWith(TEL_PREFIX) -> {
                                val action = SystemAction.Dial(url.removePrefix(TEL_PREFIX))
                                proceedWithAction(action)
                            }

                            url.startsWith(ANYTYPE_PREFIX) || url.startsWith(FILE_PREFIX) -> {
                                // Unsupported schemes - copy to clipboard directly
                                proceedWithAction(
                                    SystemAction.CopyToClipboard(
                                        plain = url,
                                        label = "link"
                                    )
                                )
                            }

                            else -> {
                                val action = OpenUrl(url)
                                proceedWithAction(action)
                            }
                        }

                        // If the primary action failed, fallback to copying to clipboard
                        if (!handled && !url.startsWith(ANYTYPE_PREFIX)
                            && !url.startsWith(FILE_PREFIX)
                        ) {
                            proceedWithAction(
                                SystemAction.CopyToClipboard(
                                    plain = url,
                                    label = "link"
                                )
                            )
                        }
                    },
                    onRequestOpenFullScreenImageGallery = { objects, index ->
                        vm.onMediaPreview(objects, index)
                                                          },
                    onSelectChatReaction = vm::onSelectChatReaction,
                    onViewChatReaction = { msg, emoji ->
                        vm.onViewChatReaction(msg = msg, emoji = emoji)
                    },
                    onRequestVideoPlayer = { attachment ->
                        MediaActivity.start(
                            context = requireContext(),
                            mediaType = MediaActivity.TYPE_VIDEO,
                            obj = attachment.obj,
                            space = space
                        )
                    }
                )
            }

            if (showNotificationPermissionDialog) {
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    Timber.d("Permission granted: $isGranted")
                    if (isGranted) {
                        vm.onNotificationPermissionGranted()
                    } else {
                        vm.onNotificationPermissionDenied()
                    }
                }
                ModalBottomSheet(
                    onDismissRequest = { vm.onNotificationPermissionDismissed() },
                    sheetState = notificationsSheetState,
                    containerColor = colorResource(id = R.color.background_secondary),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    dragHandle = null
                ) {
                    NotificationPermissionContent(
                        onCancelClicked = { vm.onNotificationPermissionDismissed() },
                        onEnableNotifications = {
                            vm.onNotificationPermissionRequested()
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    )
                }
            }

            if (showMoveToBinDialog) {
                ModalBottomSheet(
                    onDismissRequest = { vm.onMoveToBinCancelled() },
                    sheetState = moveToBinSheetState,
                    containerColor = colorResource(id = R.color.background_secondary),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    dragHandle = null
                ) {
                    GenericAlert(
                        config = AlertConfig.WithTwoButtons(
                            title = stringResource(R.string.move_chat_to_bin_warning_title),
                            description = stringResource(R.string.chat_move_to_bin_warning_description),
                            firstButtonText = stringResource(R.string.cancel),
                            secondButtonText = stringResource(R.string.chat_move_to_bin),
                            firstButtonType = BUTTON_SECONDARY,
                            secondButtonType = BUTTON_WARNING,
                            icon = R.drawable.ic_popup_question_56
                        ),
                        onFirstButtonClicked = { vm.onMoveToBinCancelled() },
                        onSecondButtonClicked = { vm.onMoveToBinConfirmed() }
                    )
                }
            }

            if (showGlobalSearchBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showGlobalSearchBottomSheet = false
                    },
                    sheetState = sheetState,
                    containerColor = colorResource(id = R.color.background_secondary),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    dragHandle = null
                ) {
                    val component = componentManager().globalSearchComponent
                    val searchViewModel = daggerViewModel {
                        component.get(
                            params = GlobalSearchViewModel.VmParams(
                                space = SpaceId(space)
                            )
                        ).getViewModel()
                    }
                    GlobalSearchScreen(
                        modifier = Modifier.padding(top = 12.dp),
                        state = searchViewModel.state
                            .collectAsStateWithLifecycle()
                            .value,
                        onQueryChanged = searchViewModel::onQueryChanged,
                        onObjectClicked = {
                            vm.onAttachObject(it)
                            showGlobalSearchBottomSheet = false
                        },
                        focusOnStart = false
                    )
                }
            } else {
                componentManager().globalSearchComponent.release()
            }

            if (showChatInfoScreen && chatInfoData != null) {
                val (name, icon) = chatInfoData!!

                // Track selected icon state for preview and save
                var selectedIcon by remember { mutableStateOf<ChatObjectIcon>(ChatObjectIcon.None) }
                var showEmojiPicker by remember { mutableStateOf(false) }

                val imagePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = { uri ->
                        if (uri != null) {
                            context?.let {
                                val path = uri.parseImagePath(it)
                                if (!path.isNullOrEmpty()) {
                                    selectedIcon = ChatObjectIcon.Image(uri = path)
                                }
                            }
                        }
                    }
                )
                
                ModalBottomSheet(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars),
                    onDismissRequest = { 
                        showChatInfoScreen = false
                        chatInfoData = null
                    },
                    sheetState = chatInfoSheetState,
                    containerColor = colorResource(id = R.color.background_primary),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    dragHandle = null
                ) {
                    EditChatInfoScreen(
                        state = ChatInfoScreenState.Edit(
                            currentName = name,
                            currentIcon = icon
                        ),
                        icon = icon,
                        selectedIcon = selectedIcon,
                        onSave = { update ->
                            vm.onUpdateChatObjectInfoRequested(
                                originalName = name,
                                originalIcon = icon,
                                update = update
                            )
                            showChatInfoScreen = false
                            chatInfoData = null
                        },
                        onCreate = { _ ->
                            // Not used in edit mode
                        },
                        onIconUploadClicked = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onIconRemoveClicked = {
                            // Mark as removed (explicit empty state)
                            selectedIcon = ChatObjectIcon.Removed
                        },
                        onEmojiIconClicked = {
                            showEmojiPicker = true
                        },
                        isLoading = false
                    )
                }
                
                if (showEmojiPicker) {
                    val emojiPickerFragment = SelectChatIconFragment.new()
                    emojiPickerFragment.show(childFragmentManager, "emoji_picker")
                    
                    // Listen for emoji selection
                    LaunchedEffect(Unit) {
                        childFragmentManager.setFragmentResultListener(
                            SelectChatIconFragment.REQUEST_KEY,
                            viewLifecycleOwner
                        ) { _, bundle ->
                            val emoji = bundle.getString(SelectChatIconFragment.RESULT_EMOJI_KEY)
                            if (emoji != null) {
                                selectedIcon = ChatObjectIcon.Emoji(unicode = emoji)
                                showEmojiPicker = false
                            }
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                vm.commands.collect { command ->
                    Timber.d("Command: $command")
                    when (command) {
                        is ChatViewModel.ViewModelCommand.Exit -> {
                            runCatching {
                                if (popUpToVault) {
                                    findNavController().navigate(R.id.action_back_on_vault)
                                } else {
                                    findNavController().popBackStack()
                                }
                            }.onFailure {
                                Timber.e(it, "Error while back on vault from chat screen")
                            }
                        }

                        is ChatViewModel.ViewModelCommand.OpenWidgets -> {
                            runCatching {
                                findNavController().navigate(
                                    R.id.actionOpenWidgetsFromChat,
                                    args = WidgetsScreenFragment.args(
                                        space = space,
                                        deeplink = null
                                    )
                                )
                            }.onFailure {
                                Timber.e(it, "Error while opening widgets from chats")
                            }
                        }

                        is ChatViewModel.ViewModelCommand.MediaPreview -> {
                            runCatching {
                                MediaActivity.start(
                                    context = requireContext(),
                                    mediaType = MediaActivity.TYPE_IMAGE,
                                    objects = command.objects,
                                    index = command.index,
                                    space = space
                                )
                            }.onFailure {
                                Timber.e(it, "Error while launching media image viewer")
                            }
                        }

                        is ChatViewModel.ViewModelCommand.SelectChatReaction -> {
                            runCatching {
                                findNavController().navigate(
                                    R.id.selectChatReactionScreen,
                                    SelectChatReactionFragment.args(
                                        space = Space(space),
                                        chat = ctx,
                                        msg = command.msg
                                    )
                                )
                            }.onFailure {
                                Timber.e(it, "Error while opening chat-reaction picker")
                            }
                        }

                        is ChatViewModel.ViewModelCommand.ViewChatReaction -> {
                            runCatching {
                                findNavController().navigate(
                                    R.id.chatReactionScreen,
                                    ChatReactionFragment.args(
                                        space = Space(space),
                                        chat = ctx,
                                        msg = command.msg,
                                        emoji = command.emoji
                                    )
                                )
                            }.onFailure {
                                Timber.e(it, "Error while opening a chat reaction")
                            }
                        }

                        is ChatViewModel.ViewModelCommand.ViewMemberCard -> {
                            runCatching {
                                findNavController().navigate(
                                    R.id.participantScreen,
                                    ParticipantFragment.args(
                                        space = command.space.id,
                                        objectId = command.member
                                    )
                                )
                            }.onFailure {
                                Timber.e(it, "Error while opening space member card")
                            }
                        }

                        is ChatViewModel.ViewModelCommand.Browse -> {
                            runCatching {
                                proceedWithAction(
                                    OpenUrl(
                                        command.url
                                    )
                                )
                            }.onFailure {
                                Timber.e(it, "Error while opening bookmark from chat")
                            }
                        }

                        is ChatViewModel.ViewModelCommand.PlayAudio -> {
                            runCatching {
                                MediaActivity.start(
                                    context = requireContext(),
                                    mediaType = MediaActivity.TYPE_AUDIO,
                                    obj = command.obj,
                                    name = command.name,
                                    space = space
                                )
                            }.onFailure {
                                Timber.e(it, "Error while launching audio player")
                            }
                        }

                        is ChatViewModel.ViewModelCommand.ShareInviteLink -> {
                            runCatching {
                                val intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, command.link)
                                    type = "text/plain"
                                }
                                startActivity(Intent.createChooser(intent, null))
                            }.onFailure {
                                Timber.e(it, "Error while sharing invite link")
                            }
                        }

                        is ChatViewModel.ViewModelCommand.ShareQrCode -> {
                            runCatching {
                                Timber.d("ShareQrCode command received with link: ${command.link}")
                                toast("QR Code sharing - to be implemented")
                            }.onFailure {
                                Timber.e(it, "Error while opening QR code")
                            }
                        }

                        is ChatViewModel.ViewModelCommand.OpenSpaceMembers -> {
                            findNavController().safeNavigate(
                                currentDestinationId = R.id.chatScreen,
                                id = R.id.shareSpaceScreen,
                                args = ShareSpaceFragment.args(command.space),
                                errorMessage = "Error while opening share screen"
                            )
                        }

                        is ChatViewModel.ViewModelCommand.OpenChatInfo -> {
                            chatInfoData = command.name to command.icon
                            showChatInfoScreen = true
                        }
                        is ChatViewModel.ViewModelCommand.Toast.PinnedChatAsWidget -> {
                            toast(
                                getString(R.string.chat_pinned_as_widget_success)
                            )
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            vm.navigation.collect { nav ->
                when (nav) {
                    is OpenObjectNavigation.OpenEditor -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.objectNavigation,
                                EditorFragment.args(
                                    ctx = nav.target,
                                    space = nav.space,
                                    effect = nav.effect
                                )
                            )
                        }.onFailure {
                            Timber.w("Error while opening editor from chat.")
                        }
                    }

                    is OpenObjectNavigation.OpenDataView -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.dataViewNavigation,
                                ObjectSetFragment.args(
                                    ctx = nav.target,
                                    space = nav.space
                                )
                            )
                        }.onFailure {
                            Timber.w("Error while opening set from chat.")
                        }
                    }

                    is OpenObjectNavigation.OpenType -> {
                        runCatching {
                            findNavController().navigate(
                                resId = R.id.objectTypeNavigation,
                                args = ObjectTypeFragment.args(
                                    objectId = nav.target,
                                    space = nav.space
                                )
                            )
                        }.onFailure {
                            Timber.w("Error while opening type from chat: ${it.message}")
                        }
                    }
                    is OpenObjectNavigation.OpenChat -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.chatScreen,
                                ChatFragment.args(
                                    space = nav.space,
                                    ctx = nav.target
                                )
                            )
                        }.onFailure {
                            Timber.w("Error while opening chat from chat.")
                        }
                    }

                    is OpenObjectNavigation.OpenParticipant -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.participantScreen,
                                ParticipantFragment.args(
                                    space = nav.space,
                                    objectId = nav.target
                                )
                            )
                        }.onFailure {
                            Timber.w("Error while opening participant from chat.")
                        }
                    }

                    is OpenObjectNavigation.OpenBookmarkUrl -> {
                        runCatching {
                            proceedWithAction(OpenUrl(nav.url))
                        }.onFailure {
                            Timber.w("Error while opening bookmark URL from chat.")
                        }
                    }

                    is OpenObjectNavigation.OpenDateObject -> {
                        runCatching {
                            findNavController().navigate(
                                R.id.objectNavigation,
                                EditorFragment.args(
                                    ctx = nav.target,
                                    space = nav.space
                                )
                            )
                        }.onFailure {
                            Timber.w("Error while opening date object from chat.")
                        }
                    }

                    is OpenObjectNavigation.UnexpectedLayoutError -> {
                        toast("Cannot open object: unexpected layout")
                        Timber.w("Unexpected layout error: ${nav.layout}")
                    }

                    OpenObjectNavigation.NonValidObject -> {
                        toast("Cannot open invalid object")
                        Timber.w("Attempted to open non-valid object")
                    }
                }
            }
        }
        ShareSpaceQrCodeScreen(
            qrCodeState = vm.uiQrCodeState.collectAsStateWithLifecycle().value,
            onShareLinkClicked = vm::onShareInviteLink,
            onHideQrCodeScreen = vm::onHideQRCodeScreen
        )
        BackHandler {
            vm.onBackButtonPressed(isExitingVault = popUpToVault)
        }
        //DROID-3943 Temporarily disabled
//        LaunchedEffect(Unit) {
//            vm.checkNotificationPermissionDialogState()
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentFragmentManager.setFragmentResultListener(
            FragmentResultContract.ATTACH_TO_CHAT_CONTRACT_KEY,
            viewLifecycleOwner
        ) { key, bundle ->
            val target = bundle.getString(FragmentResultContract.ATTACH_TO_CHAT_TARGET_ID_KEY)
            val space = bundle.getString(FragmentResultContract.ATTACH_TO_CHAT_SPACE_ID_KEY)
            val chat = bundle.getString(FragmentResultContract.ATTACH_TO_CHAT_CHAT_ID_KEY)
            if (target != null && space != null && chat != null) {
                if (this.space == space && this.ctx == chat) {
                    vm.onAttachObject(target)
                } else {
                    Timber.w("DROID-2966 Skipping attach to chat command")
                }
            } else {
                Timber.w("DROID-2966 Attach-to-chat-data is not complete")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    // DI

    private fun injectDependencies() {
        componentManager()
            .chatComponent
            .get(
                key = ctx,
                param = ChatViewModel.Params.Default(
                    ctx = ctx,
                    space = SpaceId(space),
                    triggeredByPush = triggeredByPush
                )
            )
            .inject(this)
    }

    private fun releaseDependencies() {
        componentManager().chatComponent.release(ctx)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ErrorScreen() {
        val errorStateScreen = vm.errorState.collectAsStateWithLifecycle()
        when (val state = errorStateScreen.value) {
            ChatViewModel.UiErrorState.Hidden -> {
                // Do nothing
            }

            is ChatViewModel.UiErrorState.Show -> {
                BaseAlertDialog(
                    dialogText = state.msg,
                    buttonText = stringResource(id = R.string.membership_error_button_text_dismiss),
                    onButtonClick = vm::hideError,
                    onDismissRequest = vm::hideError
                )
            }

            ChatViewModel.UiErrorState.CameraPermissionDenied -> {
                AlertScreenModals(
                    title = getString(R.string.camera_permission_required_title),
                    description = getString(R.string.camera_permission_settings_message),
                    firstButtonText = getString(R.string.open_settings),
                    secondButtonText = getString(R.string.cancel),
                    onAction = {
                        requireContext().openAppSettings()
                        vm.hideError()
                    },
                    onDismiss = vm::hideError
                )
            }
        }
    }

    companion object {
        private const val CTX_KEY = "arg.discussion.ctx"
        private const val SPACE_KEY = "arg.discussion.space"
        private const val TRIGGERED_BY_PUSH_KEY = "arg.discussion.triggered-by-push"
        private const val POP_UP_TO_VAULT_KEY = "arg.discussion.pop-up-to-vault"
        const val PERMISSIONS_REQUEST_CODE = 100
        fun args(
            space: Id,
            ctx: Id,
            triggeredByPush: Boolean = false,
            popUpToVault: Boolean = true
        ) = bundleOf(
            CTX_KEY to ctx,
            SPACE_KEY to space,
            TRIGGERED_BY_PUSH_KEY to triggeredByPush,
            POP_UP_TO_VAULT_KEY to popUpToVault
        )
    }
}
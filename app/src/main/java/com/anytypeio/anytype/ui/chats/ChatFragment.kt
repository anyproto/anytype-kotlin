package com.anytypeio.anytype.ui.chats

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.features.multiplayer.GenerateInviteLinkCard
import com.anytypeio.anytype.core_ui.features.multiplayer.ShareInviteLinkCard
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.intents.SystemAction.OpenUrl
import com.anytypeio.anytype.core_utils.intents.proceedWithAction
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ext.daggerViewModel
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModelFactory
import com.anytypeio.anytype.feature_chats.ui.ChatScreenWrapper
import com.anytypeio.anytype.feature_chats.ui.ChatTopToolbar
import com.anytypeio.anytype.feature_chats.ui.NotificationPermissionContent
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.search.GlobalSearchViewModel
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.editor.gallery.FullScreenPictureFragment
import com.anytypeio.anytype.ui.home.HomeScreenFragment
import com.anytypeio.anytype.ui.home.isSpaceRootScreen
import com.anytypeio.anytype.ui.media.MediaActivity
import com.anytypeio.anytype.ui.multiplayer.DeleteSpaceInviteLinkWarning
import com.anytypeio.anytype.ui.profile.ParticipantFragment
import com.anytypeio.anytype.ui.search.GlobalSearchScreen
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import timber.log.Timber

class ChatFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: ChatViewModelFactory

    private val vm by viewModels<ChatViewModel> { factory }

    val ctx get() = arg<Id>(CTX_KEY)
    private val space get() = arg<Id>(SPACE_KEY)

    // Rendering

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    val notificationsSheetState =
                        rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    var showGlobalSearchBottomSheet by remember { mutableStateOf(false) }
                    val inviteModalState = vm.inviteModalState.collectAsStateWithLifecycle().value
                    val showNotificationPermissionDialog =
                        vm.showNotificationPermissionDialog.collectAsStateWithLifecycle().value
                    val canCreateInviteLink = vm.canCreateInviteLink.collectAsStateWithLifecycle().value
                    val isGeneratingInviteLink = vm.isGeneratingInviteLink.collectAsStateWithLifecycle().value

                    ErrorScreen()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        ChatTopToolbar(
                            header = vm.header.collectAsStateWithLifecycle().value,
                            onBackButtonClicked = {
                                vm.onBackButtonPressed(isSpaceRootScreen())
                            },
                            onSpaceNameClicked = {
                                vm.onSpaceNameClicked(isSpaceRoot = isSpaceRootScreen())
                            },
                            onSpaceIconClicked = vm::onSpaceIconClicked
                        )
                        ChatScreenWrapper(
                            modifier = Modifier.weight(1f),
                            vm = vm,
                            onAttachObjectClicked = { showGlobalSearchBottomSheet = true },
                            onMarkupLinkClicked = { proceedWithAction(OpenUrl(it)) },
                            onRequestOpenFullScreenImage = { url -> vm.onMediaPreview(url) },
                            onSelectChatReaction = vm::onSelectChatReaction,
                            onViewChatReaction = { msg, emoji ->
                                vm.onViewChatReaction(msg = msg, emoji = emoji)
                            },
                            onRequestVideoPlayer = { attachment ->
                                MediaActivity.start(
                                    context = requireContext(),
                                    mediaType = MediaActivity.TYPE_VIDEO,
                                    url = attachment.url
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

                    when (inviteModalState) {
                        is ChatViewModel.InviteModalState.ShowShareCard -> {
                            ModalBottomSheet(
                                onDismissRequest = {
                                    vm.onInviteModalDismissed()
                                },
                                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                                containerColor = Color.Transparent,
                                contentColor = Color.Transparent,
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                                dragHandle = null
                            ) {
                                ShareInviteLinkCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                        .background(
                                            shape = RoundedCornerShape(16.dp),
                                            color = colorResource(id = R.color.widget_background)
                                        ),
                                    link = inviteModalState.link,
                                    isCurrentUserOwner = canCreateInviteLink,
                                    onShareInviteClicked = { vm.onShareInviteLinkFromCardClicked() },
                                    onDeleteLinkClicked = { vm.onDeleteLinkClicked() },
                                    onShowQrCodeClicked = { vm.onShareQrCodeClicked() }
                                )
                            }
                        }
                        is ChatViewModel.InviteModalState.ShowGenerateCard -> {
                            ModalBottomSheet(
                                onDismissRequest = {
                                    vm.onInviteModalDismissed()
                                },
                                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                                containerColor = Color.Transparent,
                                contentColor = Color.Transparent,
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                                dragHandle = null
                            ) {
                                GenerateInviteLinkCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                        .background(
                                            shape = RoundedCornerShape(16.dp),
                                            color = colorResource(id = R.color.widget_background)
                                        ),
                                    onGenerateInviteLinkClicked = {
                                        vm.onGenerateInviteLinkClicked()
                                    },
                                    isLoading = isGeneratingInviteLink
                                )
                            }
                        }
                        ChatViewModel.InviteModalState.Hidden -> {
                            // No modal shown
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
                                            space = nav.space
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
                            else -> toast("TODO")
                        }
                    }
                }
                LaunchedEffect(Unit) {
                    vm.commands.collect { command ->
                        Timber.d("Command: $command")
                        when (command) {
                            is ChatViewModel.ViewModelCommand.Exit -> {
                                runCatching {
                                    findNavController().popBackStack()
                                }.onFailure {
                                    Timber.e(it, "Error while exiting chat")
                                }
                            }
                            is ChatViewModel.ViewModelCommand.OpenWidgets -> {
                                runCatching {
                                    findNavController().navigate(
                                        R.id.actionOpenWidgetsFromChat,
                                        args = HomeScreenFragment.args(
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
                                    findNavController().navigate(
                                        R.id.fullScreenImageFragment,
                                        FullScreenPictureFragment.args(
                                            url = command.url,
                                            ignoreRootWindowInsets = true
                                        )
                                    )
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
                            is ChatViewModel.ViewModelCommand.ShowDeleteLinkWarning -> {
                                runCatching {
                                    val dialog = DeleteSpaceInviteLinkWarning()
                                    dialog.onAccepted = {
                                        vm.onDeleteLinkAccepted().also {
                                            dialog.dismiss()
                                        }
                                    }
                                    dialog.onCancelled = {
                                        // Do nothing.
                                    }
                                    dialog.show(childFragmentManager, null)
                                }.onFailure {
                                    Timber.e(it, "Error while showing delete link warning")
                                }
                            }
                        }
                    }
                }
                BackHandler {
                    vm.onBackButtonPressed(
                        isSpaceRoot = isSpaceRootScreen()
                    )
                }
                LaunchedEffect(Unit) {
                    vm.checkNotificationPermissionDialogState()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    // DI

    override fun injectDependencies() {
        componentManager()
            .chatComponent
            .get(
                key = ctx,
                param = ChatViewModel.Params.Default(
                    ctx = ctx,
                    space = SpaceId(space)
                )
            )
            .inject(this)
    }

    override fun releaseDependencies() {
        componentManager().chatComponent.release(ctx)
    }

    override fun onApplyWindowRootInsets(view: View) {
        // Do not apply.
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
        }
    }

    companion object {
        private const val CTX_KEY = "arg.discussion.ctx"
        private const val SPACE_KEY = "arg.discussion.space"
        const val PERMISSIONS_REQUEST_CODE = 100
        fun args(
            space: Id,
            ctx: Id
        ) = bundleOf(
            CTX_KEY to ctx,
            SPACE_KEY to space
        )
    }
}
package com.anytypeio.anytype.feature_chats.presentation

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.LinkPreview
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.StubAccount
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.StubObjectView
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_utils.common.DefaultFileInfo
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.chats.DeleteChatMessage
import com.anytypeio.anytype.domain.chats.EditChatMessage
import com.anytypeio.anytype.domain.chats.ToggleChatMessageReaction
import com.anytypeio.anytype.domain.invite.GetCurrentInviteAccessLevel
import com.anytypeio.anytype.domain.invite.SpaceInviteLinkStore
import com.anytypeio.anytype.domain.media.DiscardPreloadedFile
import com.anytypeio.anytype.domain.media.PreloadFile
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.misc.GetLinkPreview
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.notifications.NotificationBuilder
import com.anytypeio.anytype.domain.notifications.SetChatNotificationMode
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.CreateObjectFromUrl
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.feature_chats.DefaultCoroutineTestRule
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel.UXCommand
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel.ViewModelCommand
import com.anytypeio.anytype.feature_chats.tools.ClearChatsTempFolder
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView
import com.anytypeio.anytype.presentation.spaces.UiSpaceQrCodeState
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.vault.ExitToVaultDelegate
import com.anytypeio.anytype.presentation.widgets.PinObjectAsWidgetDelegate
import com.anytypeio.anytype.test_utils.MockDataFactory
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever


@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()


    @Mock lateinit var chatContainer: ChatContainer
    @Mock lateinit var addChatMessage: AddChatMessage
    @Mock lateinit var editChatMessage: EditChatMessage
    @Mock lateinit var deleteChatMessage: DeleteChatMessage
    @Mock lateinit var toggleChatMessageReaction: ToggleChatMessageReaction
    @Mock lateinit var members: ActiveSpaceMemberSubscriptionContainer
    @Mock lateinit var getAccount: GetAccount
    @Mock lateinit var urlBuilder: UrlBuilder
    @Mock lateinit var spaceViews: SpaceViewSubscriptionContainer
    private val dispatchers: AppCoroutineDispatchers = AppCoroutineDispatchers(
        io = coroutineTestRule.dispatcher,
        computation = coroutineTestRule.dispatcher,
        main = coroutineTestRule.dispatcher
    )
    @Mock lateinit var uploadFile: UploadFile
    @Mock lateinit var preloadFile: PreloadFile
    @Mock lateinit var discardPreloadedFile: DiscardPreloadedFile
    @Mock lateinit var storeOfObjectTypes: StoreOfObjectTypes
    @Mock lateinit var copyFileToCacheDirectory: CopyFileToCacheDirectory
    @Mock lateinit var exitToVaultDelegate: ExitToVaultDelegate
    @Mock lateinit var getLinkPreview: GetLinkPreview
    @Mock lateinit var createObjectFromUrl: CreateObjectFromUrl
    @Mock lateinit var notificationPermissionManager: NotificationPermissionManager
    @Mock lateinit var spacePermissionProvider: UserPermissionProvider
    @Mock lateinit var notificationBuilder: NotificationBuilder
    @Mock lateinit var clearChatsTempFolder: ClearChatsTempFolder
    @Mock lateinit var objectWatcher: ObjectWatcher
    @Mock lateinit var createObject: CreateObject
    @Mock lateinit var getObject: GetObject
    @Mock lateinit var analytics: Analytics
    @Mock lateinit var spaceInviteLinkStore: SpaceInviteLinkStore
    @Mock lateinit var getCurrentInviteAccessLevel: GetCurrentInviteAccessLevel
    @Mock lateinit var pinObjectAsWidgetDelegate: PinObjectAsWidgetDelegate
    @Mock lateinit var setObjectListIsArchived: SetObjectListIsArchived
    @Mock lateinit var setObjectDetails: SetObjectDetails
    @Mock lateinit var setSpaceDetails: SetSpaceDetails
    @Mock lateinit var setChatNotificationMode: SetChatNotificationMode

    private val space = Space("test-space-Id")
    private val ctx = "test-ctx"


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should trigger mention and make it visible on chat input change`() = runTest {
        val stubConfig = StubConfig()
        mockInit()

        whenever(members.get()).thenReturn(
            ActiveSpaceMemberSubscriptionContainer.Store.Data(
                config = stubConfig,
                members = listOf(
                    ObjectWrapper.SpaceMember(
                        mapOf(
                            Relations.ID to RandomString.make(),
                            Relations.NAME to "test-text",
                            Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble()
                        )
                    )
                )
            )
        )

        val vm = createViewModel()

        vm.onChatBoxInputChanged(1..10,"@test-text")

        advanceUntilIdle()

        vm.mentionPanelState.test {
            val item = awaitItem()
            assert(item is ChatViewModel.MentionPanelState.Visible)
        }

        verifyBlocking(members, times(1)) { get() }
    }


    @Test
    fun `should change mention visibility on chat input change`() = runTest {
        val stubConfig = StubConfig()
        mockInit()

        whenever(members.get()).thenReturn(
            ActiveSpaceMemberSubscriptionContainer.Store.Data(
                config = stubConfig,
                members = listOf(
                    ObjectWrapper.SpaceMember(
                        mapOf(
                            Relations.ID to RandomString.make(),
                            Relations.NAME to "test-text",
                            Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble()
                        )
                    )
                )
            )
        )

        val vm = createViewModel()

        vm.onChatBoxInputChanged(5..5,"@test")

        vm.mentionPanelState.test {
            val item = awaitItem()
            assert(item is ChatViewModel.MentionPanelState.Visible)
        }


        whenever(members.get()).thenReturn(ActiveSpaceMemberSubscriptionContainer.Store.Empty)

        vm.onChatBoxInputChanged(5..5,"@test")

        vm.mentionPanelState.test {
            val item = awaitItem()
            assert(item is ChatViewModel.MentionPanelState.Hidden)
        }

    }

    @Test
    fun `change things on message change`() = runTest {
        val markup = mockMarkups()
        val attachments = mockAttachments()
        mockInit()

        whenever(uploadFile.async(any())).thenReturn(Resultat.success(ObjectWrapper.File(mapOf("id" to "id","name" to "Name"))))
        whenever(createObjectFromUrl.async(any())).thenReturn(Resultat.success(dummyObject()))
        whenever(addChatMessage.async(any())).thenReturn(Resultat.success(Pair("test-id",emptyList())))
        whenever(copyFileToCacheDirectory.copy(any())).thenReturn("/cache/preloaded_image.jpg")

        val vm = createViewModel()

        vm.chatBoxAttachments.value = attachments

        vm.onMessageSent("This is a test Message", markup)

        vm.mentionPanelState.test {
            val item = awaitItem()
            assertIs<ChatViewModel.MentionPanelState.Hidden>(item)
        }

        advanceUntilIdle()

        verifyBlocking(uploadFile,times(4)) { async(any()) }
        verifyBlocking(createObjectFromUrl, times(1)) { async(any()) }
        verifyBlocking(addChatMessage, times(1)) { async(any()) }
        verifyBlocking(copyFileToCacheDirectory, times(2)) { copy(any()) }


        whenever(editChatMessage.async(any())).thenReturn(Resultat.success(Unit))
        vm.chatBoxMode.value = ChatViewModel.ChatBoxMode.EditMessage(msg = "test-msg-Id")

        vm.onMessageSent("This is a test Message", markup)
        vm.uXCommands.test {
            val item = awaitItem()
            assert(item is UXCommand.JumpToBottom)
        }
        vm.chatBoxMode.test {
            assertIs<ChatViewModel.ChatBoxMode.Default>(awaitItem())
        }
        advanceUntilIdle()
        verifyBlocking(editChatMessage, times(1)) { async(any()) }


        clearInvocations(addChatMessage)
        vm.chatBoxMode.value = ChatViewModel.ChatBoxMode.Reply(
            msg = "test-msg-Id",
            text = "test-text",
            author = "test-user"
        )

        vm.onMessageSent("This is a test Message", markup)
        vm.uXCommands.test {
            val item = awaitItem()
            assert(item is UXCommand.JumpToBottom)
        }
        vm.chatBoxMode.test {
            assertIs<ChatViewModel.ChatBoxMode.Default>(awaitItem())
        }
        advanceUntilIdle()
        verifyBlocking(addChatMessage, times(1)) { async(any()) }
    }


    @Test
    fun `execute things on request edit clicked`() = runTest {
        val attachments = mockAttachments()
        val msg = dummyMessage()
        mockInit()
        val vm = createViewModel()

        vm.chatBoxAttachments.value = attachments
        vm.onRequestEditMessageClicked(msg = msg)
        advanceUntilIdle()
        vm.chatBoxMode.test {
            assertIs<ChatViewModel.ChatBoxMode.EditMessage>(awaitItem())
        }
    }


    @Test
    fun `clear attachments on click`() = runTest {

        val attachments = mockAttachments()
        val attachmentToBeRemoved = attachments[4]

        mockInit()
        whenever(discardPreloadedFile.async(any())).thenReturn(Resultat.success(Unit))
        whenever(copyFileToCacheDirectory.delete(any())).thenReturn(true)

        val vm = createViewModel()
        vm.chatBoxAttachments.value = attachments

        vm.onClearAttachmentClicked(attachmentToBeRemoved)
        vm.chatBoxAttachments.test {
            val items = awaitItem()
            assert(items.none { it == attachmentToBeRemoved })
        }

        advanceUntilIdle()

        verifyBlocking(discardPreloadedFile, times(1)) { async(any()) }
        verifyBlocking(copyFileToCacheDirectory, times(1)) { delete(any()) }

    }

    @Test
    fun `should clear reply`() = runTest {

        mockInit()

        val vm = createViewModel()

        vm.onClearReplyClicked()

        vm.chatBoxMode.test {
            assertIs<ChatViewModel.ChatBoxMode.Default>(awaitItem())
        }
    }


    @Test
    fun `toggle chat reaction on reacted`() = runTest {

        mockInit()
        whenever(toggleChatMessageReaction.async(any())).thenReturn(Resultat.success(Unit))

        val vm = createViewModel()

        vm.uiState.value  = ChatViewState(
            messages = listOf(
                dummyMessage()
            )
        )

        vm.onReacted(dummyMessage().id,"Emoji")

        advanceUntilIdle()
        verifyBlocking(toggleChatMessageReaction, times(1)) { async(any()) }

    }


    @Test
    fun `execute reply actions on reply message`() = runTest {
        mockInit()

        val vm = createViewModel()
        vm.onReplyMessage(dummyMessage())

        advanceUntilIdle()

        vm.chatBoxMode.test {
            assertIs<ChatViewModel.ChatBoxMode.Reply>(awaitItem())
        }
    }

    @Test
    fun `delete message on delete`() = runTest {

        mockInit()
        whenever(deleteChatMessage.async(any())).thenReturn(Resultat.success(Unit))
        val vm = createViewModel()

        vm.onDeleteMessage(dummyMessage())

        advanceUntilIdle()

        verifyBlocking(deleteChatMessage, times(1)) { async(any()) }
    }

    @Test
    fun `perform necessary actions on attachment clicked`() = runTest {
        val messageImageAttachment = ChatView.Message.Attachment.Image(
            obj = "test-obj-Id",
            url = "https://example.com/image.jpg",
            name = "image.jpg",
            ext = "jpg"
        )
        val messageBookmarkAttachment = ChatView.Message.Attachment.Bookmark(
            id = "test-Id",
            url = "https://example.com/impinfo",
            title = "some-bookmark",
            description = "some-description",
            imageUrl = "https://example.com/img"
        )
        val linkWrapper = ObjectWrapper.Basic(
            map = mapOf(
                Relations.LAYOUT to ObjectType.Layout.BOOKMARK.code.toDouble(),
                Relations.SOURCE to "https://example.com",
                Relations.NAME to "Example Bookmark"
            )
        )
        val messageLinkAttachment = ChatView.Message.Attachment.Link(
            obj = "test-object-link",
            wrapper = linkWrapper,
            typeName = "bookmark"
        )
        val linkWrapperNoSource = ObjectWrapper.Basic(
            map = mapOf(
                Relations.LAYOUT to ObjectType.Layout.BOOKMARK.code.toDouble(),
                Relations.NAME to "Example Bookmark"
            )
        )
        val messageLinkAttachmentNoSource = ChatView.Message.Attachment.Link(
            obj = "test-object-link-ns",
            wrapper = linkWrapperNoSource,
            typeName = "bookmark-ns"
        )
        val audioWrapper = ObjectWrapper.Basic(
            map = mapOf(
                    Relations.ID to "basic-audio-id",
                    Relations.LAYOUT to ObjectType.Layout.AUDIO.code.toDouble(),
                    Relations.NAME to "Sample Audio"
                )
        )
        val messageLinkAttachmentAudio = ChatView.Message.Attachment.Link(
            obj = "test-object-link-audio",
            wrapper = audioWrapper,
            typeName = "audio"
        )
        val basicWrapper = ObjectWrapper.Basic(
            map = mapOf(
                    Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                    Relations.NAME to "Regular Page"
                )
        )
        val messageLinkAttachmentBasic = ChatView.Message.Attachment.Link(
            obj = "test-object-link-basic",
            wrapper = basicWrapper,
            typeName = "Basic"
        )


        val dummyMessage = dummyMessage(listOf(
            messageImageAttachment,
            messageBookmarkAttachment,
            messageLinkAttachment,
            messageLinkAttachmentNoSource,
            messageLinkAttachmentAudio,
            messageLinkAttachmentBasic
        ))
        mockInit()

        val vm = createViewModel()

        vm.onAttachmentClicked(dummyMessage, dummyMessage.attachments[0])

        vm.uXCommands.test {
            val item = awaitItem()
            assertIs<UXCommand.OpenFullScreenImage>(item)
        }

        vm.onAttachmentClicked(dummyMessage, dummyMessage.attachments[1])
        vm.commands.test {
            val item = awaitItem()
            assertIs<ChatViewModel.ViewModelCommand.Browse>(item)
        }


        vm.onAttachmentClicked(dummyMessage, dummyMessage.attachments[2])
        vm.commands.test {
            val item = awaitItem()
            assertIs<ChatViewModel.ViewModelCommand.Browse>(item)
        }

        vm.onAttachmentClicked(dummyMessage, dummyMessage.attachments[3])
        vm.navigation.test {
            val item = awaitItem()
            assertIs<OpenObjectNavigation>(item)
        }

        vm.onAttachmentClicked(dummyMessage, dummyMessage.attachments[4])
        vm.commands.test {
            val item = awaitItem()
            assertIs<ChatViewModel.ViewModelCommand.PlayAudio>(item)
        }

        vm.onAttachmentClicked(dummyMessage, dummyMessage.attachments[5])
        vm.navigation.test {
            val item = awaitItem()
            assertIs<OpenObjectNavigation>(item)
        }

    }

    @Test
    fun `should perform preload and other actions on chat media picked`() = runTest {

        val mockedMediaUris = mockMediaUris()
        val uri = MockDataFactory.randomUuid()
        val id = "test-id"
        mockInit()
        whenever(copyFileToCacheDirectory.copy(any())).thenReturn(uri)
        whenever(preloadFile.async(any())).thenReturn(Resultat.success(id))

        val vm = createViewModel()
        vm.onChatBoxMediaPicked(mockedMediaUris)
        advanceUntilIdle()
        vm.chatBoxAttachments.test {
            val items = awaitItem()
            val filtered = items.filter {
                it is ChatView.Message.ChatBoxAttachment.Media &&
                        it.state is ChatView.Message.ChatBoxAttachment.State.Preloaded
            }
            assert(filtered.isNotEmpty())
        }


        verifyBlocking(preloadFile, atLeast(1)) { async(any()) }

    }

    @Test
    fun `should perform preload and other actions on chat media file picked`() = runTest {

        val mockedFileInfos = mockFileInfos()
        val uri = MockDataFactory.randomUuid()
        val id = "test-id"
        mockInit()
        whenever(copyFileToCacheDirectory.copy(any())).thenReturn(uri)
        whenever(preloadFile.async(any())).thenReturn(Resultat.success(id))

        val vm = createViewModel()
        vm.onChatBoxFilePicked(mockedFileInfos)
        advanceUntilIdle()
        vm.chatBoxAttachments.test {
            val items = awaitItem()
            val filtered = items.filter {
                it is ChatView.Message.ChatBoxAttachment.File &&
                        it.state is ChatView.Message.ChatBoxAttachment.State.Preloaded
            }
            assert(filtered.isNotEmpty())
        }


        verifyBlocking(preloadFile, atLeast(1)) { async(any()) }

    }

    @Test
    fun `exit edit message mode onExitEditMessageMode`() = runTest {

        mockInit()

        val vm = createViewModel()
        vm.onExitEditMessageMode()

        advanceUntilIdle()
        vm.chatBoxAttachments.test {
            assert(awaitItem().isEmpty())
        }

        vm.chatBoxMode.test {
            assertIs<ChatViewModel.ChatBoxMode.Default>(awaitItem())
        }
    }

    @Test
    fun `should perform actions on back pressed`() = runTest {

        mockInit()
        whenever(chatContainer.stop(ctx)).thenReturn(Unit)
        whenever(objectWatcher.unwatch(ctx, space)).thenReturn(Unit)
        whenever(exitToVaultDelegate.proceedWithClearingSpaceBeforeExitingToVault()).thenReturn(Unit)

        val vm = createViewModel()

        vm.onBackButtonPressed(true)

        vm.commands.test {
            val item = awaitItem()
            assertIs<ViewModelCommand.Exit>(item)
        }
        advanceUntilIdle()
        verifyBlocking(chatContainer, times(1)) { stop(ctx) }
        verifyBlocking(objectWatcher, times(1)) { unwatch(ctx, space) }
        verifyBlocking(exitToVaultDelegate, times(1)) { proceedWithClearingSpaceBeforeExitingToVault() }
    }

    @Test
    fun `should view member card command execute`() = runTest {

        mockInit()
        val vm = createViewModel()
        vm.onMemberIconClicked("test-member-id")
        vm.commands.test {
            assertIs<ViewModelCommand.ViewMemberCard>(awaitItem())
        }

    }

    @Test
    fun `should execute state actions on empty state`() = runTest {
        mockInit()
        val vm = createViewModel()
        vm.onEmptyStateAction()
        vm.commands.test {
            assertIs<ViewModelCommand.OpenSpaceMembers>(awaitItem())
        }
    }

    @Test
    fun `should show qr code if link is ready`() = runTest {
        mockInit(mapOf(Relations.SPACE_UX_TYPE to SpaceUxType.CHAT.code.toDouble()))
        val vm = createViewModel()
        vm.inviteLinkAccessLevel.value = SpaceInviteLinkAccessLevel.EditorAccess("test-link")
        vm.onShowQRCode()
        advanceUntilIdle()
        vm.uiQrCodeState.test {
            assertIs<UiSpaceQrCodeState.SpaceInvite>(awaitItem())
        }
    }

    @Test
    fun `should scroll to top onChatScrolledToTop`() = runTest {
        mockInit()
        whenever(chatContainer.onLoadPrevious()).thenReturn(Unit)
        val vm = createViewModel()
        vm.onChatScrolledToTop()
        advanceUntilIdle()
        verifyBlocking(chatContainer,times(1)) { onLoadPrevious() }
    }

    @Test
    fun `should scroll to next onChatScrolledToBottom`() = runTest {
        mockInit()
        whenever(chatContainer.onLoadNext()).thenReturn(Unit)
        val vm = createViewModel()
        vm.onChatScrolledToBottom()
        advanceUntilIdle()
        verifyBlocking(chatContainer,times(1)) { onLoadNext() }
    }

    @Test
    fun `should go to mention onGoToMentionClicked`() = runTest {
        mockInit()
        whenever(chatContainer.onGoToMention()).thenReturn(Unit)
        val vm = createViewModel()
        vm.onGoToMentionClicked()
        advanceUntilIdle()
        verifyBlocking(chatContainer,times(1)) { onGoToMention() }
    }

    @Test
    fun `should load reply onChatScrollToReply`() = runTest {
        val replyId = "test-reply-id"
        mockInit()
        whenever(chatContainer.onLoadToReply(replyId)).thenReturn(Unit)
        val vm = createViewModel()
        vm.onChatScrollToReply(replyId)
        advanceUntilIdle()
        verifyBlocking(chatContainer,times(1)) { onLoadToReply(replyId) }
    }

    @Test
    fun `should load last msg onScrollToBottomClicked`() = runTest {
        val lastMessageId = "test-last-msg-id"
        mockInit()
        whenever(chatContainer.onLoadChatTail(lastMessageId)).thenReturn(Unit)
        val vm = createViewModel()
        vm.onScrollToBottomClicked(lastMessageId)
        advanceUntilIdle()
        verifyBlocking(chatContainer,times(1)) { onLoadChatTail(lastMessageId) }
    }

    @Test
    fun `should clear view state onClearChatViewStateIntent`() = runTest {
        mockInit()
        whenever(chatContainer.onClearIntent()).thenReturn(Unit)
        val vm = createViewModel()
        vm.onClearChatViewStateIntent()
        advanceUntilIdle()
        verifyBlocking(chatContainer,times(1)) { onClearIntent() }
    }

    @Test
    fun `should get link preview and add to chat attachment`() = runTest {
        val testUrl = "test-url.com"
        val testLinkPreview = LinkPreview(
            url = testUrl,
            title = "test-title",
            description = "test-description",
        )
        mockInit()
        whenever(getLinkPreview.async(testUrl)).thenReturn(Resultat.success(testLinkPreview))

        val vm = createViewModel()
        vm.onUrlPasted(testUrl)
        advanceUntilIdle()
        vm.chatBoxAttachments.test {
            val items = awaitItem()
            val filtered = items.filter {
                it is ChatView.Message.ChatBoxAttachment.Bookmark
            }
            assert(filtered.isNotEmpty())
            assertIs<ChatView.Message.ChatBoxAttachment.Bookmark>(filtered[0])
        }

        verifyBlocking(getLinkPreview, times(1)) { async(testUrl) }
    }

    @Test
    fun `create and attach object`() = runTest {
        mockInit()
        whenever(createObject.async(any())).thenReturn(
            Resultat.success(
                CreateObject.Result(
                    obj = dummyObject(),
                    objectId = "test-obj-id",
                    typeKey = TypeKey("test-key"),
                    event = Payload(ctx, emptyList())
                )
            )
        )

        val vm = createViewModel()
        vm.onCreateAndAttachObject()
        vm.navigation.test {
            val item = awaitItem()
            assertIs<OpenObjectNavigation>(item)
        }
        advanceUntilIdle()
        verifyBlocking(createObject, times(1)) { async(any()) }
    }

    @Test
    fun `should hide camera permission`() = runTest {
        mockInit()
        val vm = createViewModel()
        vm.onCameraPermissionDenied()
        vm.errorState.test {
            val item = awaitItem()
            assertIs<ChatViewModel.UiErrorState.CameraPermissionDenied>(item)
        }
    }

    private fun mockFileInfos(): List<DefaultFileInfo> {
        return listOf(
            randomFileInfo(),
            randomFileInfo(),
            randomFileInfo(),
            randomFileInfo(),
            randomFileInfo()
        )
    }

    private fun randomFileInfo(): DefaultFileInfo {
        return DefaultFileInfo(
            name = MockDataFactory.randomUuid(),
            uri = MockDataFactory.randomUuid(),
            size = 1234,
        )
    }

    private fun mockMediaUris(): List<ChatViewModel.ChatBoxMediaUri> {
        return listOf(
            ChatViewModel.ChatBoxMediaUri(
                uri = MockDataFactory.randomUuid(),
                isVideo = true,
                capturedByCamera = true
            ),
            randomChatBoxMediaUri(),
            randomChatBoxMediaUri(),
            randomChatBoxMediaUri()
        )
    }

    private fun randomChatBoxMediaUri(): ChatViewModel.ChatBoxMediaUri {
        return ChatViewModel.ChatBoxMediaUri(
            uri = MockDataFactory.randomUuid(),
            isVideo = MockDataFactory.randomBoolean(),
            capturedByCamera = MockDataFactory.randomBoolean()
        )
    }

    private fun mockMarkups(): List<Block.Content.Text.Mark> {
        return listOf(
            Block.Content.Text.Mark(
                range = 0..3,
                type = Block.Content.Text.Mark.Type.BOLD
            ),
            Block.Content.Text.Mark(
                range = 5..6,
                type = Block.Content.Text.Mark.Type.ITALIC
            ),
            Block.Content.Text.Mark(
                range = 8..8,
                type = Block.Content.Text.Mark.Type.UNDERLINE
            ),
            Block.Content.Text.Mark(
                range = 10..13,
                type = Block.Content.Text.Mark.Type.TEXT_COLOR,
                param = "#2196F3"
            )
        )
    }


    private fun mockAttachments(): List<ChatView.Message.ChatBoxAttachment> {
        return listOf(
            ChatView.Message.ChatBoxAttachment.Link(
                target = "link-1",
                wrapper = GlobalSearchItemView(
                    id = "ID",
                    space = SpaceId(""),
                    title = "Autechre",
                    type = "Band",
                    meta = GlobalSearchItemView.Meta.None,
                    layout = ObjectType.Layout.BASIC,
                    icon = ObjectIcon.Basic.Emoji("🎵"),
                    obj = dummyObject()
                )
            ),
            ChatView.Message.ChatBoxAttachment.Existing.Link(
                target = "existing-link-1",
                name = "Existing link",
                typeName = "Note",
                icon = ObjectIcon.None
            ),
            ChatView.Message.ChatBoxAttachment.Existing.Image(
                target = "existing-image-1",
                url = "https://example.com/image.jpg"
            ),
            ChatView.Message.ChatBoxAttachment.Existing.Video(
                target = "existing-video-1",
                url = "https://example.com/video.mp4"
            ),
            ChatView.Message.ChatBoxAttachment.Media(
                uri = "content://preloaded/image",
                isVideo = false,
                capturedByCamera = false,
                state = ChatView.Message.ChatBoxAttachment.State.Preloaded(
                    preloadedFileId = "preloaded-image-id",
                    path = "/cache/preloaded_image.jpg"
                )
            ),
            ChatView.Message.ChatBoxAttachment.Media(
                uri = "file:///storage/emulated/0/DCIM/video.mp4",
                isVideo = true,
                capturedByCamera = true,
                state = ChatView.Message.ChatBoxAttachment.State.Idle
            ),
            ChatView.Message.ChatBoxAttachment.Bookmark(
                preview = LinkPreview(
                    url = "https://anytype.io",
                    title = "Anytype",
                    description = "Privacy-first workspace",
                    imageUrl = "https://example.com/img-test"
                )
            ),
            ChatView.Message.ChatBoxAttachment.File(
                uri = "content://preloaded/file",
                name = "document.pdf",
                size = 1234,
                state = ChatView.Message.ChatBoxAttachment.State.Preloaded(
                    preloadedFileId = "preloaded-file-id",
                    path = "/cache/document.pdf"
                )
            ),
            ChatView.Message.ChatBoxAttachment.File(
                uri = "file:///storage/emulated/0/Download/report.pdf",
                name = "report.pdf",
                size = 5678,
                state = ChatView.Message.ChatBoxAttachment.State.Idle
            )
        )
    }

    private fun dummyMessage(attachments: List<ChatView.Message.Attachment> = emptyList()): ChatView.Message {
        return  ChatView.Message(
            id = "test-msg-Id",
            content = ChatView.Message.Content(
                msg = "test-msg",
                parts = emptyList()
            ),
            author = "test-author",
            timestamp = 0L,
            isSynced = false,
            creator = "test-creator",
            attachments = attachments
        )
    }

    private fun dummyObject() = ObjectWrapper.Basic(
        mapOf(
            "id" to "1",
            "name" to "Name",
            "description" to "Description11",
            Relations.SPACE_ID to "1",
            Relations.LAYOUT to ObjectType.Layout.BASIC.code
        )
    )

    private suspend fun mockInit(spaceViewMap: Struct = emptyMap()) {
        val mockSpaceView = ObjectWrapper.SpaceView(spaceViewMap)
        val mockObjectView = StubObjectView("test-object-Id")
        val mockAcc = StubAccount()
        val mockInviteLinkLevel = SpaceInviteLinkAccessLevel.ViewerAccess("test-link")
        whenever(spacePermissionProvider.observe(space)).thenReturn(flowOf(SpaceMemberPermissions.OWNER))
        whenever(spaceViews.observe(space)).thenReturn(flowOf(mockSpaceView))
        whenever(getObject.async(any())).thenReturn(Resultat.success(mockObjectView))
        whenever(getAccount.async(any())).thenReturn(Resultat.success(mockAcc))
        whenever(objectWatcher.watch(ctx,space)).thenReturn(flowOf(mockObjectView))
        whenever(pinObjectAsWidgetDelegate.isChatPinned(space,ctx)).thenReturn(true)
        whenever(spaceViews.observe()).thenReturn(flowOf(listOf(mockSpaceView)))
        whenever(getCurrentInviteAccessLevel.async(any())).thenReturn(Resultat.success(mockInviteLinkLevel))
        whenever(spaceInviteLinkStore.observe(space)).thenReturn(flowOf(mockInviteLinkLevel))
        whenever(chatContainer.watchWhileTrackingAttachments(ctx)).thenReturn(emptyFlow())
        whenever(chatContainer.subscribeToAttachments(ctx, space)).thenReturn(emptyFlow())
        whenever(chatContainer.fetchReplies(ctx)).thenReturn(emptyFlow())
    }


    private fun createViewModel(): ChatViewModel {
        val params = ChatViewModel.Params.Default(
            ctx = ctx,
            space = space,
            triggeredByPush = false
        )
        return ChatViewModel(
            vmParams = params,
            chatContainer = chatContainer,
            addChatMessage = addChatMessage,
            editChatMessage = editChatMessage,
            deleteChatMessage = deleteChatMessage,
            toggleChatMessageReaction = toggleChatMessageReaction,
            members = members,
            getAccount = getAccount,
            urlBuilder = urlBuilder,
            spaceViews = spaceViews,
            dispatchers = dispatchers,
            uploadFile = uploadFile,
            preloadFile = preloadFile,
            discardPreloadedFile = discardPreloadedFile,
            storeOfObjectTypes = storeOfObjectTypes,
            copyFileToCacheDirectory = copyFileToCacheDirectory,
            exitToVaultDelegate = exitToVaultDelegate,
            getLinkPreview = getLinkPreview,
            createObjectFromUrl = createObjectFromUrl,
            notificationPermissionManager = notificationPermissionManager,
            spacePermissionProvider = spacePermissionProvider,
            notificationBuilder = notificationBuilder,
            clearChatsTempFolder = clearChatsTempFolder,
            objectWatcher = objectWatcher,
            createObject = createObject,
            getObject = getObject,
            analytics = analytics,
            spaceInviteLinkStore = spaceInviteLinkStore,
            getCurrentInviteAccessLevel = getCurrentInviteAccessLevel,
            pinObjectAsWidgetDelegate = pinObjectAsWidgetDelegate,
            setObjectListIsArchived = setObjectListIsArchived,
            setObjectDetails = setObjectDetails,
            setSpaceDetails = setSpaceDetails,
            setChatNotificationMode = setChatNotificationMode
        )
    }

}
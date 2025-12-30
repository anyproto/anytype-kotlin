package com.anytypeio.anytype.presentation.sharing

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.device.FileSharer
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.Permissions
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.objects.CreateObjectFromUrl
import com.anytypeio.anytype.domain.objects.CreatePrefilledNote
import com.anytypeio.anytype.domain.page.AddBackLinkToObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SharingViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    // Use Cases
    private lateinit var createBookmarkObject: CreateBookmarkObject
    private lateinit var createPrefilledNote: CreatePrefilledNote
    private lateinit var createObjectFromUrl: CreateObjectFromUrl
    private lateinit var addChatMessage: AddChatMessage
    private lateinit var uploadFile: UploadFile
    private lateinit var searchObjects: SearchObjects
    private lateinit var addBackLinkToObject: AddBackLinkToObject
    private lateinit var addObjectToCollection: AddObjectToCollection

    // Managers & Providers
    private lateinit var spaceManager: SpaceManager
    private lateinit var urlBuilder: UrlBuilder
    private lateinit var awaitAccountStartManager: AwaitAccountStartManager
    private lateinit var analytics: Analytics
    private lateinit var fileSharer: FileSharer
    private lateinit var permissions: Permissions
    private lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
    private lateinit var spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer
    private lateinit var fieldParser: FieldParser

    // Test data
    private val testDataSpace = StubSpaceView(
        id = "data-space-1",
        targetSpaceId = "data-space-1",
        spaceAccessType = SpaceAccessType.DEFAULT,
        spaceAccountStatus = SpaceStatus.OK,
        spaceLocalStatus = SpaceStatus.OK,
        chatId = "",
        spaceUxType = SpaceUxType.DATA
    )

    private val selectableDataSpace = SelectableSpaceView(
        id = "data-space-1",
        targetSpaceId = "data-space-1",
        name = "Test Space",
        icon = SpaceIconView.Loading,
        uxType = SpaceUxType.DATA,
        chatId = null,
        isSelected = false
    )

    @Before
    fun setup() {
        createBookmarkObject = mock()
        createPrefilledNote = mock()
        createObjectFromUrl = mock()
        addChatMessage = mock()
        uploadFile = mock()
        searchObjects = mock()
        addBackLinkToObject = mock()
        addObjectToCollection = mock()
        spaceManager = mock()
        urlBuilder = mock()
        awaitAccountStartManager = mock()
        analytics = mock()
        fileSharer = mock()
        permissions = mock()
        analyticSpaceHelperDelegate = mock()
        spaceViewSubscriptionContainer = mock()
        fieldParser = mock()
    }

    private fun buildViewModel(): SharingViewModel {
        return SharingViewModel(
            createBookmarkObject = createBookmarkObject,
            createPrefilledNote = createPrefilledNote,
            createObjectFromUrl = createObjectFromUrl,
            spaceManager = spaceManager,
            urlBuilder = urlBuilder,
            awaitAccountStartManager = awaitAccountStartManager,
            analytics = analytics,
            fileSharer = fileSharer,
            permissions = permissions,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            addChatMessage = addChatMessage,
            uploadFile = uploadFile,
            searchObjects = searchObjects,
            fieldParser = fieldParser,
            addBackLinkToObject = addBackLinkToObject,
            addObjectToCollection = addObjectToCollection
        )
    }

    // region Stub Helpers

    private fun stubAwaitAccountStart() {
        whenever(awaitAccountStartManager.awaitStart()).thenReturn(
            flowOf(AwaitAccountStartManager.State.Started)
        )
    }

    private fun stubSpaceViewSubscription(spaces: List<ObjectWrapper.SpaceView> = emptyList()) {
        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(flowOf(spaces))
    }

    private fun stubPermissions(permissions: Map<String, SpaceMemberPermissions> = emptyMap()) {
        whenever(this.permissions.all()).thenReturn(flowOf(permissions))
    }

    private fun stubCreateNote(objectId: String = "test-note-id") {
        createPrefilledNote.stub {
            onBlocking { async(any()) }.thenReturn(Resultat.Success(objectId))
        }
    }

    private fun stubUploadFile(fileId: String = "test-file-id") {
        uploadFile.stub {
            onBlocking { async(any()) }.thenReturn(
                Resultat.Success(
                    ObjectWrapper.File(mapOf(Relations.ID to fileId))
                )
            )
        }
    }

    private fun stubUploadFileFailure() {
        uploadFile.stub {
            onBlocking { async(any()) }.thenReturn(
                Resultat.Failure(Exception("Upload failed"))
            )
        }
    }

    private fun stubUploadFilePartialFailure(fileId: String = "test-file-id") {
        uploadFile.stub {
            onBlocking { async(any()) }
                .thenReturn(Resultat.Success(ObjectWrapper.File(mapOf(Relations.ID to fileId))))
                .thenReturn(Resultat.Failure(Exception("Upload failed")))
        }
    }

    private fun stubFileSharerPath(path: String = "/test/path/file.jpg") {
        fileSharer.stub {
            onBlocking { getPath(any()) }.thenReturn(path)
        }
    }

    private fun stubSpaceManager(spaceId: String = "data-space-1") {
        spaceManager.stub {
            onBlocking { get() }.thenReturn(spaceId)
        }
    }

    private fun stubAnalyticSpaceHelper() {
        whenever(analyticSpaceHelperDelegate.provideParams(any())).thenReturn(
            AnalyticSpaceHelperDelegate.Params(
                permission = "owner",
                spaceType = "private",
                spaceUxType = "data"
            )
        )
    }

    // endregion

    // region Basic Tests

    @Test
    fun `should start in loading state`() = runTest {
        // Given
        stubAwaitAccountStart()
        stubSpaceViewSubscription()
        stubPermissions()

        // When
        val vm = buildViewModel()

        // Then
        vm.screenState.test {
            val initialState = awaitItem()
            assertTrue(
                "Expected Loading state, but got $initialState",
                initialState is SharingScreenState.Loading
            )
        }
    }

    @Test
    fun `should remain in Loading state when shared content received before spaces loaded`() = runTest {
        // Given - spaces subscription returns empty (simulating no spaces loaded yet)
        stubAwaitAccountStart()
        stubSpaceViewSubscription(emptyList())
        stubPermissions()

        // When
        val vm = buildViewModel()
        vm.onSharedDataReceived(SharedContent.Text("Test text"))

        // Then - should still be in Loading state (not SpaceSelection with empty list)
        vm.screenState.test {
            val state = awaitItem()
            assertIs<SharingScreenState.Loading>(
                state,
                "Expected Loading state when spaces not loaded, but got $state",
            )
        }
    }

    @Test
    fun `should show space selection after loading spaces`() = runTest {
        // Given
        stubAwaitAccountStart()
        stubSpaceViewSubscription(listOf(testDataSpace))
        stubPermissions(mapOf(testDataSpace.id to SpaceMemberPermissions.OWNER))

        // When
        val vm = buildViewModel()
        vm.onSharedDataReceived(SharedContent.Text("Test text"))
        advanceUntilIdle()

        // Then
        vm.screenState.test {
            val state = awaitItem()
            assertIs<SharingScreenState.SpaceSelection>(
                state,
                "Expected SpaceSelection state, but got $state",
            )
        }
    }

    // endregion

    // region Group 1: createObjectInSpace (ObjectSelection, no selection)

    @Test
    fun `onSendClicked with Text content should create Note`() = runTest {
        // Given
        stubAwaitAccountStart()
        stubSpaceViewSubscription(listOf(testDataSpace))
        stubPermissions(mapOf(testDataSpace.id to SpaceMemberPermissions.OWNER))
        stubCreateNote()
        stubSpaceManager()
        stubAnalyticSpaceHelper()

        val vm = buildViewModel()
        vm.onSharedDataReceived(SharedContent.Text("Test text content"))
        advanceUntilIdle()

        // Navigate to ObjectSelection by selecting data space
        vm.onSpaceSelected(selectableDataSpace)
        advanceUntilIdle()

        // When - send without selecting any object
        vm.onSendClicked()
        advanceUntilIdle()

        // Then
        verify(createPrefilledNote).async(any())
    }

    @Test
    fun `onSendClicked with SingleMedia should upload file directly`() = runTest {
        // Given
        stubAwaitAccountStart()
        stubSpaceViewSubscription(listOf(testDataSpace))
        stubPermissions(mapOf(testDataSpace.id to SpaceMemberPermissions.OWNER))
        stubUploadFile()
        stubFileSharerPath()
        stubSpaceManager()

        val vm = buildViewModel()
        vm.onSharedDataReceived(
            SharedContent.SingleMedia("content://test/image.jpg", SharedContent.MediaType.IMAGE)
        )
        advanceUntilIdle()

        // Navigate to ObjectSelection
        vm.onSpaceSelected(selectableDataSpace)
        advanceUntilIdle()

        // When - send without selecting any object
        vm.onSendClicked()
        advanceUntilIdle()

        // Then - should upload file, NOT create Note
        verify(uploadFile).async(any())
        verify(createPrefilledNote, never()).async(any())
    }

    @Test
    fun `onSendClicked with SingleMedia upload failure should show error`() = runTest {
        // Given
        stubAwaitAccountStart()
        stubSpaceViewSubscription(listOf(testDataSpace))
        stubPermissions(mapOf(testDataSpace.id to SpaceMemberPermissions.OWNER))
        stubUploadFileFailure()
        stubFileSharerPath()
        stubSpaceManager()

        val vm = buildViewModel()
        vm.onSharedDataReceived(
            SharedContent.SingleMedia("content://test/image.jpg", SharedContent.MediaType.IMAGE)
        )
        advanceUntilIdle()

        // Navigate to ObjectSelection
        vm.onSpaceSelected(selectableDataSpace)
        advanceUntilIdle()

        // When
        vm.onSendClicked()
        advanceUntilIdle()

        // Then - should show error state
        vm.screenState.test {
            val state = awaitItem()
            assertTrue(
                "Expected Error state, but got $state",
                state is SharingScreenState.Error
            )
        }
    }

    @Test
    fun `onSendClicked with MultipleMedia should upload all files`() = runTest {
        // Given
        stubAwaitAccountStart()
        stubSpaceViewSubscription(listOf(testDataSpace))
        stubPermissions(mapOf(testDataSpace.id to SpaceMemberPermissions.OWNER))
        stubUploadFile()
        stubFileSharerPath()
        stubSpaceManager()

        val vm = buildViewModel()
        vm.onSharedDataReceived(
            SharedContent.MultipleMedia(
                uris = listOf("content://test/image1.jpg", "content://test/image2.jpg"),
                type = SharedContent.MediaType.IMAGE
            )
        )
        advanceUntilIdle()

        // Navigate to ObjectSelection
        vm.onSpaceSelected(selectableDataSpace)
        advanceUntilIdle()

        // When
        vm.onSendClicked()
        advanceUntilIdle()

        // Then - should upload files, NOT create Note
        verify(uploadFile, atLeast(1)).async(any())
        verify(createPrefilledNote, never()).async(any())
    }

    @Test
    fun `onSendClicked with MultipleMedia partial failure should show warning toast`() = runTest {
        // Given - first upload succeeds, second fails
        stubAwaitAccountStart()
        stubSpaceViewSubscription(listOf(testDataSpace))
        stubPermissions(mapOf(testDataSpace.id to SpaceMemberPermissions.OWNER))
        stubUploadFilePartialFailure()
        stubFileSharerPath()
        stubSpaceManager()

        val vm = buildViewModel()
        vm.onSharedDataReceived(
            SharedContent.MultipleMedia(
                uris = listOf("content://test/image1.jpg", "content://test/image2.jpg"),
                type = SharedContent.MediaType.IMAGE
            )
        )
        advanceUntilIdle()

        // Navigate to ObjectSelection
        vm.onSpaceSelected(selectableDataSpace)
        advanceUntilIdle()

        // Then - collect commands before triggering send
        vm.commands.test {
            // When
            vm.onSendClicked()
            advanceUntilIdle()

            // Then - should show warning toast for partial failure
            val toastCommand = awaitItem()
            assertIs<SharingCommand.ShowToast>(toastCommand)
            assertEquals("1 of 2 files failed to upload", toastCommand.message)

            // Also expect success snackbar command
            val snackbarCommand = awaitItem()
            assertIs<SharingCommand.ShowSnackbarWithOpenAction>(snackbarCommand)

            // And dismiss command
            val dismissCommand = awaitItem()
            assertIs<SharingCommand.Dismiss>(dismissCommand)
        }
    }

    // endregion
}

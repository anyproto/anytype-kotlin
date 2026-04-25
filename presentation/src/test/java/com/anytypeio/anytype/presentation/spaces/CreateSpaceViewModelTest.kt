package com.anytypeio.anytype.presentation.spaces

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.multiplayer.ChannelCreationType
import com.anytypeio.anytype.core_models.multiplayer.InviteType
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLink
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.multiplayer.AddSpaceMembers
import com.anytypeio.anytype.domain.multiplayer.GenerateSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.MakeSpaceShareable
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.spaces.CreateSpace
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.spaces.SetSpaceDetails
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CreateSpaceViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock lateinit var createSpace: CreateSpace
    @Mock lateinit var spaceManager: SpaceManager
    @Mock lateinit var analytics: Analytics
    @Mock lateinit var uploadFile: UploadFile
    @Mock lateinit var setSpaceDetails: SetSpaceDetails
    @Mock lateinit var saveCurrentSpace: SaveCurrentSpace
    @Mock lateinit var makeSpaceShareable: MakeSpaceShareable
    @Mock lateinit var generateSpaceInviteLink: GenerateSpaceInviteLink
    @Mock lateinit var addSpaceMembers: AddSpaceMembers
    @Mock lateinit var spaceViews: SpaceViewSubscriptionContainer
    @Mock lateinit var permissions: UserPermissionProvider
    @Mock lateinit var profileContainer: ProfileSubscriptionManager
    @Mock lateinit var urlBuilder: UrlBuilder

    private val testConfig = StubConfig()
    private val testSpaceId = MockDataFactory.randomUuid()
    private val testStartingObject = MockDataFactory.randomUuid()
    private val testCreateSpaceResult = Command.CreateSpace.Result(
        space = SpaceId(testSpaceId),
        startingObject = testStartingObject
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        stubSpaceViews()
    }

    // region Personal Channel

    @Test
    fun `personal channel creation - happy path - finishes without share flow`() = runTest {
        // Given
        val vmParams = givenPersonalParams()
        stubCreateSpaceSuccess()
        stubSpaceManagerSet()
        stubSaveCurrentSpace()

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        // When
        vm.onCreateSpace("My Space")
        advanceUntilIdle()

        // Then
        verify(makeSpaceShareable, never()).async(any())
        verify(generateSpaceInviteLink, never()).async(any())
        vm.commands.test {
            // Command already emitted, check via state
        }
        assertEquals(false, vm.isInProgress.value)
        assertNull(vm.createSpaceError.value)
    }

    @Test
    fun `personal channel creation - isInProgress is true while loading`() = runTest {
        // Given
        val vmParams = givenPersonalParams()
        // Return only Loading, never Success — simulates in-progress state
        createSpace.stub {
            on { stream(any()) } doReturn flowOf(Resultat.Loading())
        }
        stubSpaceViews()

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        // When
        vm.onCreateSpace("My Space")
        advanceUntilIdle()

        // Then
        assertTrue(vm.isInProgress.value)
    }

    @Test
    fun `personal channel creation - duplicate call while in progress is ignored`() = runTest {
        // Given
        val vmParams = givenPersonalParams()
        createSpace.stub {
            on { stream(any()) } doReturn flowOf(Resultat.Loading())
        }

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        vm.onCreateSpace("My Space")
        advanceUntilIdle()
        assertTrue(vm.isInProgress.value)

        // When — second call while loading
        vm.onCreateSpace("My Space Again")
        advanceUntilIdle()

        // Then — only one stream call
        verify(createSpace).stream(any())
    }

    // endregion

    // region Group Channel - Happy Path

    @Test
    fun `group channel creation - happy path - share, invite, add members, finish`() = runTest {
        // Given
        val members = listOf("identity-1", "identity-2")
        val vmParams = givenGroupParams(members)
        stubCreateSpaceSuccess()
        stubMakeShareableSuccess()
        stubGenerateInviteLinkSuccess()
        stubAddMembersSuccess()
        stubSpaceManagerSet()
        stubSaveCurrentSpace()
        stubShareableLimitNotReached()

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        // When
        vm.onCreateSpace("Group Channel")
        advanceUntilIdle()

        // Then
        verify(makeSpaceShareable).async(any())
        verify(generateSpaceInviteLink).async(any())
        verify(addSpaceMembers).async(any())
        verify(spaceManager).set(any(), any())
        assertEquals(false, vm.isInProgress.value)
        assertNull(vm.createSpaceError.value)
    }

    // endregion

    // region Group Channel - MakeShareable Failure

    @Test
    fun `group creation with members - makeShareable fails - aborts with error`() = runTest {
        // Given
        val members = listOf("identity-1")
        val vmParams = givenGroupParams(members)
        stubCreateSpaceSuccess()
        stubMakeShareableFailure()
        stubShareableLimitNotReached()

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        // When
        vm.onCreateSpace("Group Channel")
        advanceUntilIdle()

        // Then — should abort: error shown, no finish
        assertNotNull(vm.createSpaceError.value)
        assertEquals(false, vm.isInProgress.value)
        verify(spaceManager, never()).set(any(), any())
        verify(generateSpaceInviteLink, never()).async(any())
    }

    @Test
    fun `group creation without members - makeShareable fails - falls back to finish`() = runTest {
        // Given
        val vmParams = givenGroupParams(emptyList())
        stubCreateSpaceSuccess()
        stubMakeShareableFailure()
        stubSpaceManagerSet()
        stubSaveCurrentSpace()
        stubShareableLimitNotReached()

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        // When
        vm.onCreateSpace("Group Channel")
        advanceUntilIdle()

        // Then — should fall back: no error, finish called
        assertNull(vm.createSpaceError.value)
        verify(spaceManager).set(any(), any())
    }

    // endregion

    // region Group Channel - GenerateInviteLink Failure

    @Test
    fun `group creation with members - generateInviteLink fails - aborts with error`() = runTest {
        // Given
        val members = listOf("identity-1")
        val vmParams = givenGroupParams(members)
        stubCreateSpaceSuccess()
        stubMakeShareableSuccess()
        stubGenerateInviteLinkFailure()
        stubShareableLimitNotReached()

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        // When
        vm.onCreateSpace("Group Channel")
        advanceUntilIdle()

        // Then — should abort: error shown, no finish
        assertNotNull(vm.createSpaceError.value)
        assertEquals(false, vm.isInProgress.value)
        verify(spaceManager, never()).set(any(), any())
        verify(addSpaceMembers, never()).async(any())
    }

    @Test
    fun `group creation without members - generateInviteLink fails - falls back to finish`() = runTest {
        // Given
        val vmParams = givenGroupParams(emptyList())
        stubCreateSpaceSuccess()
        stubMakeShareableSuccess()
        stubGenerateInviteLinkFailure()
        stubSpaceManagerSet()
        stubSaveCurrentSpace()
        stubShareableLimitNotReached()

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        // When
        vm.onCreateSpace("Group Channel")
        advanceUntilIdle()

        // Then — should fall back: no error, finish called
        assertNull(vm.createSpaceError.value)
        verify(spaceManager).set(any(), any())
    }

    // endregion

    // region Create Space Failure

    @Test
    fun `create space fails - shows error`() = runTest {
        // Given
        val vmParams = givenPersonalParams()
        val error = RuntimeException("Creation failed")
        createSpace.stub {
            on { stream(any()) } doReturn flowOf(
                Resultat.Loading(),
                Resultat.Failure(error)
            )
        }

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        // When
        vm.onCreateSpace("My Space")
        advanceUntilIdle()

        // Then
        assertNotNull(vm.createSpaceError.value)
        assertEquals("Creation failed", vm.createSpaceError.value?.msg)
        assertEquals(false, vm.isInProgress.value)
    }

    // endregion

    // region Group Channel - Writer/Reader Split (DROID-4481)

    @Test
    fun `writers limit 2 with 3 selected - reserves owner seat - 1 writer + 2 readers`() = runTest {
        // Given — tier writersLimit=2 means owner + 1 addable writer.
        val members = listOf("identity-1", "identity-2", "identity-3")
        val vmParams = givenGroupParams(members = members, writersLimit = 2)
        stubCreateSpaceSuccess()
        stubMakeShareableSuccess()
        stubGenerateInviteLinkSuccess()
        stubAddMembersSuccess()
        stubSpaceManagerSet()
        stubSaveCurrentSpace()
        stubShareableLimitNotReached()

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        // When
        vm.onCreateSpace("Group Channel")
        advanceUntilIdle()

        // Then — first call: 1 writer, second call: 2 readers
        val captor = argumentCaptor<AddSpaceMembers.Params>()
        verify(addSpaceMembers, org.mockito.kotlin.times(2)).async(captor.capture())
        val writerCall = captor.allValues[0]
        val readerCall = captor.allValues[1]
        assertEquals(SpaceMemberPermissions.WRITER, writerCall.permissions)
        assertEquals(listOf("identity-1"), writerCall.identities)
        assertEquals(SpaceMemberPermissions.READER, readerCall.permissions)
        assertEquals(listOf("identity-2", "identity-3"), readerCall.identities)
    }

    @Test
    fun `writers limit 1 with 3 selected - no writer slots - all 3 readers`() = runTest {
        // Given — tier writersLimit=1 means owner is the only writer; no addable writer slots.
        val members = listOf("identity-1", "identity-2", "identity-3")
        val vmParams = givenGroupParams(members = members, writersLimit = 1)
        stubCreateSpaceSuccess()
        stubMakeShareableSuccess()
        stubGenerateInviteLinkSuccess()
        stubAddMembersSuccess()
        stubSpaceManagerSet()
        stubSaveCurrentSpace()
        stubShareableLimitNotReached()

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        // When
        vm.onCreateSpace("Group Channel")
        advanceUntilIdle()

        // Then — single call with all members as readers
        val captor = argumentCaptor<AddSpaceMembers.Params>()
        verify(addSpaceMembers, org.mockito.kotlin.times(1)).async(captor.capture())
        val readerCall = captor.firstValue
        assertEquals(SpaceMemberPermissions.READER, readerCall.permissions)
        assertEquals(members, readerCall.identities)
    }

    @Test
    fun `writers limit 0 with 3 selected - no tier limit - all 3 writers`() = runTest {
        // Given — writersLimit=0 means "no tier limit"; preserve existing unlimited semantics.
        val members = listOf("identity-1", "identity-2", "identity-3")
        val vmParams = givenGroupParams(members = members, writersLimit = 0)
        stubCreateSpaceSuccess()
        stubMakeShareableSuccess()
        stubGenerateInviteLinkSuccess()
        stubAddMembersSuccess()
        stubSpaceManagerSet()
        stubSaveCurrentSpace()
        stubShareableLimitNotReached()

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        // When
        vm.onCreateSpace("Group Channel")
        advanceUntilIdle()

        // Then — single call with all members as writers
        val captor = argumentCaptor<AddSpaceMembers.Params>()
        verify(addSpaceMembers, org.mockito.kotlin.times(1)).async(captor.capture())
        val writerCall = captor.firstValue
        assertEquals(SpaceMemberPermissions.WRITER, writerCall.permissions)
        assertEquals(members, writerCall.identities)
    }

    @Test
    fun `writers limit 5 with 2 selected - all under cap - both writers no readers`() = runTest {
        // Given — tier allows 4 addable writers (5 - 1 owner); 2 selected fit entirely as writers.
        val members = listOf("identity-1", "identity-2")
        val vmParams = givenGroupParams(members = members, writersLimit = 5)
        stubCreateSpaceSuccess()
        stubMakeShareableSuccess()
        stubGenerateInviteLinkSuccess()
        stubAddMembersSuccess()
        stubSpaceManagerSet()
        stubSaveCurrentSpace()
        stubShareableLimitNotReached()

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        // When
        vm.onCreateSpace("Group Channel")
        advanceUntilIdle()

        // Then — single call: both writers, no reader call
        val captor = argumentCaptor<AddSpaceMembers.Params>()
        verify(addSpaceMembers, org.mockito.kotlin.times(1)).async(captor.capture())
        val writerCall = captor.firstValue
        assertEquals(SpaceMemberPermissions.WRITER, writerCall.permissions)
        assertEquals(members, writerCall.identities)
    }

    @Test
    fun `group creation with empty selection - no addSpaceMembers calls`() = runTest {
        // Given
        val vmParams = givenGroupParams(members = emptyList(), writersLimit = 3)
        stubCreateSpaceSuccess()
        stubMakeShareableSuccess()
        stubGenerateInviteLinkSuccess()
        stubAddMembersSuccess()
        stubSpaceManagerSet()
        stubSaveCurrentSpace()
        stubShareableLimitNotReached()

        val vm = buildViewModel(vmParams)
        advanceUntilIdle()

        // When
        vm.onCreateSpace("Group Channel")
        advanceUntilIdle()

        // Then — finishes without adding members
        verify(addSpaceMembers, never()).async(any())
        verify(spaceManager).set(any(), any())
        assertNull(vm.createSpaceError.value)
    }

    // endregion

    // region Helpers

    private fun givenPersonalParams(
        members: List<String> = emptyList()
    ) = CreateSpaceViewModel.VmParams(
        channelType = ChannelCreationType.PERSONAL,
        selectedMemberIdentities = members
    )

    private fun givenGroupParams(
        members: List<String> = emptyList(),
        writersLimit: Int = 0
    ) = CreateSpaceViewModel.VmParams(
        channelType = ChannelCreationType.GROUP,
        selectedMemberIdentities = members,
        writersLimit = writersLimit
    )

    private fun stubSpaceViews() {
        spaceViews.stub {
            on { observe() } doReturn flowOf(emptyList())
        }
    }

    private fun stubCreateSpaceSuccess() {
        createSpace.stub {
            on { stream(any()) } doReturn flowOf(
                Resultat.Loading(),
                Resultat.Success(testCreateSpaceResult)
            )
        }
    }

    private fun stubMakeShareableSuccess() {
        makeSpaceShareable.stub {
            onBlocking { async(any()) } doReturn Resultat.success(Unit)
        }
    }

    private fun stubMakeShareableFailure() {
        makeSpaceShareable.stub {
            onBlocking { async(any()) } doReturn Resultat.Failure(
                RuntimeException("Share failed")
            )
        }
    }

    private fun stubGenerateInviteLinkSuccess() {
        generateSpaceInviteLink.stub {
            onBlocking { async(any()) } doReturn Resultat.success(
                SpaceInviteLink(
                    fileKey = MockDataFactory.randomUuid(),
                    contentId = MockDataFactory.randomUuid(),
                    inviteType = InviteType.WITHOUT_APPROVE,
                    permissions = SpaceMemberPermissions.WRITER
                )
            )
        }
    }

    private fun stubGenerateInviteLinkFailure() {
        generateSpaceInviteLink.stub {
            onBlocking { async(any()) } doReturn Resultat.Failure(
                RuntimeException("Invite link failed")
            )
        }
    }

    private fun stubAddMembersSuccess() {
        addSpaceMembers.stub {
            onBlocking { async(any()) } doReturn Resultat.success(Unit)
        }
    }

    private fun stubSpaceManagerSet() {
        spaceManager.stub {
            onBlocking { set(any(), any()) } doReturn Result.success(testConfig)
        }
    }

    private fun stubSaveCurrentSpace() {
        saveCurrentSpace.stub {
            onBlocking { async(any()) } doReturn Resultat.success(Unit)
        }
    }

    private fun stubShareableLimitNotReached() {
        permissions.stub {
            on { all() } doReturn flowOf(emptyMap())
        }
        profileContainer.stub {
            on { observe() } doReturn flowOf(
                com.anytypeio.anytype.core_models.ObjectWrapper.Basic(emptyMap())
            )
        }
    }

    private fun buildViewModel(
        vmParams: CreateSpaceViewModel.VmParams
    ) = CreateSpaceViewModel(
        vmParams = vmParams,
        createSpace = createSpace,
        spaceManager = spaceManager,
        analytics = analytics,
        uploadFile = uploadFile,
        setSpaceDetails = setSpaceDetails,
        saveCurrentSpace = saveCurrentSpace,
        makeSpaceShareable = makeSpaceShareable,
        generateSpaceInviteLink = generateSpaceInviteLink,
        addSpaceMembers = addSpaceMembers,
        spaceViews = spaceViews,
        permissions = permissions,
        profileContainer = profileContainer,
        urlBuilder = urlBuilder
    )

    // endregion
}

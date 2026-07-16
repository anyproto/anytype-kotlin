package com.anytypeio.anytype.domain.invite

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.multiplayer.InviteType
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteError
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLink
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.util.dispatchers
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class GetCurrentInviteAccessLevelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var logger: Logger

    private lateinit var store: SpaceInviteLinkStore
    private lateinit var usecase: GetCurrentInviteAccessLevel

    private val space = SpaceId(MockDataFactory.randomUuid())
    private val params = GetCurrentInviteAccessLevel.Params(space = space)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        store = SpaceInviteLinkStoreImpl()
        usecase = GetCurrentInviteAccessLevel(
            dispatchers = dispatchers,
            repo = repo,
            logger = logger,
            store = store
        )
    }

    private fun stubInvite(invite: SpaceInviteLink) {
        repo.stub {
            onBlocking { getSpaceInviteLink(space) } doReturn invite
        }
    }

    private fun invite(
        contentId: String = MockDataFactory.randomUuid(),
        fileKey: String = MockDataFactory.randomUuid(),
        inviteType: InviteType = InviteType.MEMBER,
        permissions: SpaceMemberPermissions = SpaceMemberPermissions.READER,
        heldByOwner: Boolean = false
    ) = SpaceInviteLink(
        contentId = contentId,
        fileKey = fileKey,
        inviteType = inviteType,
        permissions = permissions,
        heldByOwner = heldByOwner
    )

    @Test
    fun `member device with owner-held invite maps to HeldByOwner`() = runBlocking {
        // A member's InviteGetCurrent succeeds with empty cid + key
        stubInvite(
            invite(
                contentId = "",
                fileKey = "",
                heldByOwner = true
            )
        )

        val result = usecase.execute(params).getOrNull()

        assertEquals(SpaceInviteLinkAccessLevel.HeldByOwner, result)
        assertEquals(SpaceInviteLinkAccessLevel.HeldByOwner, store.state.value[space])
    }

    @Test
    fun `owner device with owner-held member invite maps to RequestAccess not shared`() = runBlocking {
        val invite = invite(inviteType = InviteType.MEMBER, heldByOwner = true)
        stubInvite(invite)

        val result = usecase.execute(params).getOrNull()

        assertEquals(
            SpaceInviteLinkAccessLevel.RequestAccess(invite.scheme, isShared = false),
            result
        )
    }

    @Test
    fun `invite shared within space maps with isShared true`() = runBlocking {
        val invite = invite(inviteType = InviteType.MEMBER, heldByOwner = false)
        stubInvite(invite)

        val result = usecase.execute(params).getOrNull()

        assertEquals(
            SpaceInviteLinkAccessLevel.RequestAccess(invite.scheme, isShared = true),
            result
        )
    }

    @Test
    fun `shared anyone-can-join reader invite maps to shared ViewerAccess`() = runBlocking {
        val invite = invite(
            inviteType = InviteType.WITHOUT_APPROVE,
            permissions = SpaceMemberPermissions.READER,
            heldByOwner = false
        )
        stubInvite(invite)

        val result = usecase.execute(params).getOrNull()

        assertEquals(
            SpaceInviteLinkAccessLevel.ViewerAccess(invite.scheme, isShared = true),
            result
        )
    }

    @Test
    fun `owner-held anyone-can-join writer invite maps to EditorAccess not shared`() = runBlocking {
        val invite = invite(
            inviteType = InviteType.WITHOUT_APPROVE,
            permissions = SpaceMemberPermissions.WRITER,
            heldByOwner = true
        )
        stubInvite(invite)

        val result = usecase.execute(params).getOrNull()

        assertEquals(
            SpaceInviteLinkAccessLevel.EditorAccess(invite.scheme, isShared = false),
            result
        )
    }

    @Test
    fun `legacy unsafe invite - shared anyone-can-join writer - maps to shared EditorAccess`() = runBlocking {
        // The exact state that triggers the red legacy warning and the Editor lockout
        val invite = invite(
            inviteType = InviteType.WITHOUT_APPROVE,
            permissions = SpaceMemberPermissions.WRITER,
            heldByOwner = false
        )
        stubInvite(invite)

        val result = usecase.execute(params).getOrNull()

        assertEquals(
            SpaceInviteLinkAccessLevel.EditorAccess(invite.scheme, isShared = true),
            result
        )
    }

    @Test
    fun `guest invite maps to LinkDisabled`() = runBlocking {
        stubInvite(invite(inviteType = InviteType.GUEST))

        val result = usecase.execute(params).getOrNull()

        assertEquals(SpaceInviteLinkAccessLevel.LinkDisabled(), result)
    }

    @Test
    fun `no active invite maps to LinkDisabled`() = runBlocking {
        repo.stub {
            onBlocking { getSpaceInviteLink(space) } doAnswer {
                throw SpaceInviteError.InviteNotActive
            }
        }

        val result = usecase.execute(params).getOrNull()

        assertEquals(SpaceInviteLinkAccessLevel.LinkDisabled(), result)
        assertEquals(SpaceInviteLinkAccessLevel.LinkDisabled(), store.state.value[space])
    }
}

package com.anytypeio.anytype.presentation.multiplayer

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.presentation.multiplayer.SpaceMemberView.ActionType
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

/**
 * Covers the Admin-role additions to the members role selector (DROID-4250, section 2).
 */
class ShareSpaceMemberMappingTest {

    @Mock
    lateinit var urlBuilder: UrlBuilder

    @Mock
    lateinit var strings: StringResourceProvider

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        strings.stub {
            on { getMultiplayerAdmin() } doReturn "Admin"
            on { getMultiplayerEditor() } doReturn "Editor"
            on { getMultiplayerViewer() } doReturn "Viewer"
            on { getMultiplayerOwner() } doReturn "Owner"
            on { getMultiplayerRemoveMember() } doReturn "Remove member"
            on { getMultiplayerNoPermissions() } doReturn "No permissions"
        }
    }

    private val spaceView = ObjectWrapper.SpaceView(mapOf(Relations.ID to "space1"))

    private fun member(
        perm: SpaceMemberPermissions,
        id: String = "m1"
    ) = ObjectWrapper.SpaceMember(
        mapOf(
            Relations.ID to id,
            Relations.IDENTITY to id,
            Relations.NAME to "Member $id",
            Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble(),
            Relations.PARTICIPANT_PERMISSIONS to perm.code.toDouble()
        )
    )

    private fun map(
        perm: SpaceMemberPermissions,
        actor: SpaceMemberPermissions = SpaceMemberPermissions.OWNER,
        account: String? = null
    ) = listOf(member(perm)).toSpaceMemberView(
        spaceView = spaceView,
        urlBuilder = urlBuilder,
        isCurrentUserOwner = actor == SpaceMemberPermissions.OWNER,
        currentUserPermissions = actor,
        account = account,
        stringResourceProvider = strings
    ).single()

    private fun actions(
        perm: SpaceMemberPermissions,
        actor: SpaceMemberPermissions = SpaceMemberPermissions.OWNER
    ) = map(perm, actor).contextActions.map { it.actionType }

    @Test
    fun `owner viewing an editor sees Admin first and Editor selected`() {
        val actions = map(SpaceMemberPermissions.WRITER).contextActions

        assertEquals(
            listOf(
                ActionType.MAKE_ADMIN,
                ActionType.CAN_EDIT,
                ActionType.CAN_VIEW,
                ActionType.REMOVE_MEMBER
            ),
            actions.map { it.actionType }
        )
        assertFalse(actions.first { it.actionType == ActionType.MAKE_ADMIN }.isSelected)
        assertTrue(actions.first { it.actionType == ActionType.CAN_EDIT }.isSelected)
    }

    @Test
    fun `admin member shows Admin label and Admin action selected, Editor not selected`() {
        val view = map(SpaceMemberPermissions.ADMIN)

        assertEquals("Admin", view.statusText)
        assertTrue(view.contextActions.first { it.actionType == ActionType.MAKE_ADMIN }.isSelected)
        assertFalse(view.contextActions.first { it.actionType == ActionType.CAN_EDIT }.isSelected)
    }

    @Test
    fun `editor or viewer actor sees no actions`() {
        assertTrue(actions(SpaceMemberPermissions.WRITER, actor = SpaceMemberPermissions.WRITER).isEmpty())
        assertTrue(actions(SpaceMemberPermissions.READER, actor = SpaceMemberPermissions.READER).isEmpty())
    }

    @Test
    fun `admin actor can remove editors and viewers but sees no role options`() {
        assertEquals(
            listOf(ActionType.REMOVE_MEMBER),
            actions(SpaceMemberPermissions.WRITER, actor = SpaceMemberPermissions.ADMIN)
        )
        assertEquals(
            listOf(ActionType.REMOVE_MEMBER),
            actions(SpaceMemberPermissions.READER, actor = SpaceMemberPermissions.ADMIN)
        )
    }

    @Test
    fun `admin actor cannot remove other admins or the owner`() {
        assertTrue(actions(SpaceMemberPermissions.ADMIN, actor = SpaceMemberPermissions.ADMIN).isEmpty())
        assertTrue(actions(SpaceMemberPermissions.OWNER, actor = SpaceMemberPermissions.ADMIN).isEmpty())
    }

    @Test
    fun `owner can remove an admin`() {
        assertEquals(
            listOf(
                ActionType.MAKE_ADMIN,
                ActionType.CAN_EDIT,
                ActionType.CAN_VIEW,
                ActionType.REMOVE_MEMBER
            ),
            actions(SpaceMemberPermissions.ADMIN, actor = SpaceMemberPermissions.OWNER)
        )
    }

    @Test
    fun `own row shows no role selector`() {
        // current user (account) is an Admin viewing their own row
        val view = listOf(member(SpaceMemberPermissions.ADMIN, id = "me")).toSpaceMemberView(
            spaceView = spaceView,
            urlBuilder = urlBuilder,
            isCurrentUserOwner = false,
            currentUserPermissions = SpaceMemberPermissions.ADMIN,
            account = "me",
            stringResourceProvider = strings
        ).single()
        assertTrue(view.contextActions.isEmpty())
    }
}

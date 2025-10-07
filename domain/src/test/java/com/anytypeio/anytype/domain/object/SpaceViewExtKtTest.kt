package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import junit.framework.TestCase.assertTrue
import kotlin.test.assertFalse
import net.bytebuddy.utility.RandomString
import org.junit.Test

class SpaceViewExtKtTest {

    val spaceId = RandomString.make()

    val isCurrentUserOwner = true

    val newMember = ObjectWrapper.SpaceMember(
        map = mapOf(
            Relations.ID to RandomString.make(),
            Relations.SPACE_ID to spaceId,
            Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
        )
    )

    @Test
    fun `1 participant, zero limits`() {
        val spaceView = createSpaceView(
            writersLimit = 0,
            readersLimit = 0
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertFalse(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertFalse(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `1 participant, owner + join`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 3
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        assertTrue(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `2 participants, owner writer join`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 3
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        assertTrue(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `2 participants, owner reader join`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 3
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        assertTrue(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `3 participants, owner reader reader join`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 3
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertFalse(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `3 participants, owner writer reader join`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 3
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertFalse(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `3 participants, owner writer writer join`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 3
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertFalse(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertFalse(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `3 participants, owner writer writer join, diff limits`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 5
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertFalse(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `3 participants, owner writer reader join, diff limits`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 5
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        assertTrue(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `3 participants, owner reader reader join, diff limits`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 5
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        assertTrue(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `4 participants, owner writer writer reader join, diff limits`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 5
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertFalse(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `5 participants, o w w r r join, diff limits`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 5
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertFalse(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertFalse(spaceView.canChangeReaderToWriter(participants))
    }

    // CHAT space type tests

    @Test
    fun `CHAT space - can add writers even when limit reached`() {
        val spaceView = createSpaceView(
            writersLimit = 2,
            readersLimit = 2,
            spaceUxType = SpaceUxType.CHAT
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        // Writers limit reached for DATA space, but should still allow for CHAT
        assertTrue(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
    }

    @Test
    fun `CHAT space - can add readers even when limit reached`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 3,
            spaceUxType = SpaceUxType.CHAT
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        // Readers limit reached for DATA space, but should still allow for CHAT
        assertTrue(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
    }

    @Test
    fun `CHAT space - can change reader to writer even when writers limit reached`() {
        val spaceView = createSpaceView(
            writersLimit = 2,
            readersLimit = 5,
            spaceUxType = SpaceUxType.CHAT
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.WRITER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.READER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        // Writers limit reached for DATA space, but should still allow for CHAT
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `CHAT space - zero limits still allows adding members`() {
        val spaceView = createSpaceView(
            writersLimit = 0,
            readersLimit = 0,
            spaceUxType = SpaceUxType.CHAT
        )

        val participants = listOf(
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.SPACE_ID to spaceId,
                    Relations.PARTICIPANT_PERMISSIONS to SpaceMemberPermissions.OWNER.code.toDouble(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.ACTIVE.code.toDouble()
                )
            )
        )

        // Zero limits should not affect CHAT spaces
        assertTrue(spaceView.canAddWriters(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants, newMember))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    //region Helpers
    private fun createSpaceView(
        writersLimit: Int,
        readersLimit: Int,
        spaceUxType: SpaceUxType = SpaceUxType.DATA
    ) = ObjectWrapper.SpaceView(
        map = mapOf(
            Relations.ID to spaceId,
            Relations.WRITERS_LIMIT to writersLimit.toDouble(),
            Relations.READERS_LIMIT to readersLimit.toDouble(),
            Relations.SPACE_ACCESS_TYPE to 2.0,
            Relations.SPACE_UX_TYPE to spaceUxType.code.toDouble()
        )
    )
    //endregion

}
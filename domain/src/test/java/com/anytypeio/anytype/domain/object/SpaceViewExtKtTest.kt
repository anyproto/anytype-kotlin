package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import junit.framework.TestCase.assertTrue
import kotlin.test.assertFalse
import net.bytebuddy.utility.RandomString
import org.junit.Test

class SpaceViewExtKtTest {

    val spaceId = RandomString.make()

    val isCurrentUserOwner = true

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

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants))
        assertFalse(spaceView.canAddReaders(isCurrentUserOwner, participants))
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
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
                )
            )
        )

        assertTrue(spaceView.canAddWriters(isCurrentUserOwner, participants))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants))
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
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
                )
            )
        )

        assertTrue(spaceView.canAddWriters(isCurrentUserOwner, participants))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants))
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
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
                )
            )
        )

        assertTrue(spaceView.canAddWriters(isCurrentUserOwner, participants))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants))
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
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
                )
            )
        )

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants))
        assertFalse(spaceView.canAddReaders(isCurrentUserOwner, participants))
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
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
                )
            )
        )

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants))
        assertFalse(spaceView.canAddReaders(isCurrentUserOwner, participants))
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
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
                )
            )
        )

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants))
        assertFalse(spaceView.canAddReaders(isCurrentUserOwner, participants))
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
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
                )
            )
        )

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants))
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
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
                )
            )
        )

        assertTrue(spaceView.canAddWriters(isCurrentUserOwner, participants))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants))
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
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
                )
            )
        )

        assertTrue(spaceView.canAddWriters(isCurrentUserOwner, participants))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants))
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
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
                )
            )
        )

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants))
        assertTrue(spaceView.canAddReaders(isCurrentUserOwner, participants))
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
            ),
            ObjectWrapper.SpaceMember(
                map = mapOf(
                    Relations.ID to RandomString.make(),
                    Relations.PARTICIPANT_STATUS to ParticipantStatus.JOINING.code.toDouble()
                )
            )
        )

        assertFalse(spaceView.canAddWriters(isCurrentUserOwner, participants))
        assertFalse(spaceView.canAddReaders(isCurrentUserOwner, participants))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertFalse(spaceView.canChangeReaderToWriter(participants))
    }

    //region Helpers
    private fun createSpaceView(writersLimit: Int, readersLimit: Int) = ObjectWrapper.SpaceView(
        map = mapOf(
            Relations.ID to spaceId,
            Relations.WRITERS_LIMIT to writersLimit.toDouble(),
            Relations.READERS_LIMIT to readersLimit.toDouble(),
            Relations.SPACE_ACCESS_TYPE to 2.0
        )
    )
    //endregion

}
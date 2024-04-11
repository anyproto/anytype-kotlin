package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import junit.framework.TestCase.assertTrue
import kotlin.test.assertFalse
import net.bytebuddy.utility.RandomString
import org.junit.Test

class SpaceViewExtKtTest {

    val spaceId = RandomString.make()

    @Test
    fun `1 participant, zero limits`() {
        val spaceView = createSpaceView(
            writersLimit = 0,
            readersLimit = 0
        )

        val participants = createParticipants(
            SpaceMemberPermissions.OWNER
        )

        assertFalse(spaceView.canAddWriters(participants))
        assertFalse(spaceView.canAddReaders(participants))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertFalse(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `1 participant, o`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 3
        )

        val participants = createParticipants(
            SpaceMemberPermissions.OWNER
        )

        assertTrue(spaceView.canAddWriters(participants))
        assertTrue(spaceView.canAddReaders(participants))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `2 participants, o w`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 3
        )

        val participants = createParticipants(
            SpaceMemberPermissions.OWNER,
            SpaceMemberPermissions.WRITER
        )

        assertTrue(spaceView.canAddWriters(participants))
        assertTrue(spaceView.canAddReaders(participants))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `2 participants, o r`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 3
        )

        val participants = createParticipants(
            SpaceMemberPermissions.OWNER,
            SpaceMemberPermissions.READER
        )

        assertTrue(spaceView.canAddWriters(participants))
        assertTrue(spaceView.canAddReaders(participants))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `3 participants, o r r`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 3
        )

        val participants = createParticipants(
            SpaceMemberPermissions.OWNER,
            SpaceMemberPermissions.READER,
            SpaceMemberPermissions.READER
        )

        assertFalse(spaceView.canAddWriters(participants))
        assertFalse(spaceView.canAddReaders(participants))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `3 participants, o w r`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 3
        )

        val participants = createParticipants(
            SpaceMemberPermissions.OWNER,
            SpaceMemberPermissions.WRITER,
            SpaceMemberPermissions.READER
        )

        assertFalse(spaceView.canAddWriters(participants))
        assertFalse(spaceView.canAddReaders(participants))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `3 participants, o w w`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 3
        )

        val participants = createParticipants(
            SpaceMemberPermissions.OWNER,
            SpaceMemberPermissions.WRITER,
            SpaceMemberPermissions.WRITER
        )

        assertFalse(spaceView.canAddWriters(participants))
        assertFalse(spaceView.canAddReaders(participants))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertFalse(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `3 participants, o w w, diff limits`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 5
        )

        val participants = createParticipants(
            SpaceMemberPermissions.OWNER,
            SpaceMemberPermissions.WRITER,
            SpaceMemberPermissions.WRITER
        )

        assertFalse(spaceView.canAddWriters(participants))
        assertTrue(spaceView.canAddReaders(participants))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertFalse(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `3 participants, o w r, diff limits`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 5
        )

        val participants = createParticipants(
            SpaceMemberPermissions.OWNER,
            SpaceMemberPermissions.WRITER,
            SpaceMemberPermissions.READER
        )

        assertTrue(spaceView.canAddWriters(participants))
        assertTrue(spaceView.canAddReaders(participants))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `3 participants, o r r, diff limits`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 5
        )

        val participants = createParticipants(
            SpaceMemberPermissions.OWNER,
            SpaceMemberPermissions.READER,
            SpaceMemberPermissions.READER
        )

        assertTrue(spaceView.canAddWriters(participants))
        assertTrue(spaceView.canAddReaders(participants))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertTrue(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `4 participants, o w w r, diff limits`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 5
        )

        val participants = createParticipants(
            SpaceMemberPermissions.OWNER,
            SpaceMemberPermissions.WRITER,
            SpaceMemberPermissions.WRITER,
            SpaceMemberPermissions.READER
        )

        assertFalse(spaceView.canAddWriters(participants))
        assertTrue(spaceView.canAddReaders(participants))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertFalse(spaceView.canChangeReaderToWriter(participants))
    }

    @Test
    fun `5 participants, o w w r r, diff limits`() {
        val spaceView = createSpaceView(
            writersLimit = 3,
            readersLimit = 5
        )

        val participants = createParticipants(
            SpaceMemberPermissions.OWNER,
            SpaceMemberPermissions.WRITER,
            SpaceMemberPermissions.WRITER,
            SpaceMemberPermissions.READER,
            SpaceMemberPermissions.READER
        )

        assertFalse(spaceView.canAddWriters(participants))
        assertFalse(spaceView.canAddReaders(participants))
        assertTrue(spaceView.canChangeWriterToReader(participants))
        assertFalse(spaceView.canChangeReaderToWriter(participants))
    }

    //region Helpers
    private fun createSpaceView(writersLimit: Int, readersLimit: Int) = ObjectWrapper.SpaceView(
        map = mapOf(
            Relations.ID to spaceId,
            Relations.WRITERS_LIMIT to writersLimit.toDouble(),
            Relations.READERS_LIMIT to readersLimit.toDouble()
        )
    )

    private fun createParticipants(vararg permissions: SpaceMemberPermissions) = permissions.map {
        ObjectWrapper.SpaceMember(
            map = mapOf(
                Relations.ID to RandomString.make(),
                Relations.SPACE_ID to spaceId,
                Relations.PARTICIPANT_PERMISSIONS to it.code.toDouble()
            )
        )
    }
    //endregion

}
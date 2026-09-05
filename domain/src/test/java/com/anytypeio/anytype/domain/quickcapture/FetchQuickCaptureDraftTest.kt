package com.anytypeio.anytype.domain.quickcapture

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.multiplayer.ParticipantSubscriptionContainer
import com.anytypeio.anytype.domain.util.dispatchers
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

/**
 * These tests stub the EXACT query the use case is expected to issue, rather than verifying
 * with argument matchers: SpaceId is a value class, and a Mockito matcher for one returns
 * null, which NPEs when the call unboxes it. A stub that does not match returns Mockito's
 * default (an empty list), so a wrong or missing filter surfaces as a null result.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FetchQuickCaptureDraftTest {

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var participants: ParticipantSubscriptionContainer

    @Mock
    lateinit var auth: AuthRepository

    private lateinit var usecase: FetchQuickCaptureDraft

    private val space = SpaceId(MockDataFactory.randomUuid())
    private val draft = MockDataFactory.randomUuid()
    private val identity = MockDataFactory.randomUuid()
    private val participantId = MockDataFactory.randomUuid()
    private val keys = listOf(Relations.ID, Relations.NAME)

    @Before
    fun setup() {
        usecase = FetchQuickCaptureDraft(
            repo = repo,
            participants = participants,
            auth = auth,
            dispatchers = dispatchers
        )
        auth.stub { onBlocking { getCurrentAccountId() } doReturn identity }
    }

    private fun member(spaceId: String, id: String = participantId) = ObjectWrapper.SpaceMember(
        mapOf(
            Relations.ID to id,
            Relations.IDENTITY to identity,
            Relations.SPACE_ID to spaceId
        )
    )

    /** The query the use case must issue: by id AND scoped to our participant as creator. */
    private fun scopedFilters(creator: String) = listOf(
        DVFilter(
            relation = Relations.ID,
            value = draft,
            condition = DVFilterCondition.EQUAL
        ),
        DVFilter(
            relation = Relations.CREATOR,
            value = creator,
            condition = DVFilterCondition.EQUAL
        )
    )

    private fun stubSearch(filters: List<DVFilter>, result: List<Map<String, Any?>>) {
        repo.stub {
            onBlocking {
                searchObjects(space = space, filters = filters, limit = 1, keys = keys)
            } doReturn result
        }
    }

    private suspend fun run() = usecase.run(
        FetchQuickCaptureDraft.Params(space = space, draft = draft, keys = keys)
    )

    @Test
    fun `scopes the query to this participant as creator`() = runBlocking {
        participants.stub { on { get() } doReturn listOf(member(space.id)) }
        // Only the creator-scoped query is stubbed. An unscoped one returns Mockito's
        // default empty list, so this assertion fails if the filter is dropped — which is
        // the whole point: another participant's draft must never come back.
        stubSearch(scopedFilters(participantId), listOf(mapOf(Relations.ID to draft)))

        assertEquals(draft, run()?.id)
    }

    @Test
    fun `matches the participant of this space, not merely this identity`() = runBlocking {
        val otherSpaceParticipant = MockDataFactory.randomUuid()
        // Same account, another space listed first. Taking the first identity match would
        // filter on a participant id that never appears as creator in this space, so every
        // draft here would read as missing.
        participants.stub {
            on { get() } doReturn listOf(
                member(spaceId = MockDataFactory.randomUuid(), id = otherSpaceParticipant),
                member(space.id)
            )
        }
        stubSearch(scopedFilters(otherSpaceParticipant), emptyList())
        stubSearch(scopedFilters(participantId), listOf(mapOf(Relations.ID to draft)))

        assertEquals(draft, run()?.id)
    }

    @Test
    fun `returns null when the store has no match`() = runBlocking {
        participants.stub { on { get() } doReturn listOf(member(space.id)) }
        stubSearch(scopedFilters(participantId), emptyList())

        assertNull(run())
    }
}

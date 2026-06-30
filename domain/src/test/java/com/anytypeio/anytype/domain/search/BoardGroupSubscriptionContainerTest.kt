package com.anytypeio.anytype.domain.search

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.DataViewGroup
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals

class BoardGroupSubscriptionContainerTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var channel: SubscriptionEventChannel

    @Mock
    lateinit var logger: Logger

    private lateinit var container: BoardGroupSubscriptionContainer

    private val space = SpaceId(MockDataFactory.randomUuid())
    private val subscription = "ctx-board-groups"
    private val relationKey = "tag"

    private val empty = DataViewGroup(id = "empty", value = DataViewGroup.Value.Empty)
    private val groupA = DataViewGroup(id = "A", value = DataViewGroup.Value.Tag(listOf("optA")))
    private val groupB = DataViewGroup(id = "B", value = DataViewGroup.Value.Tag(listOf("optB")))
    private val groupC = DataViewGroup(id = "C", value = DataViewGroup.Value.Tag(listOf("optC")))

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        container = BoardGroupSubscriptionContainer(
            repo = repo,
            channel = channel,
            dispatchers = AppCoroutineDispatchers(
                io = rule.testDispatcher,
                main = rule.testDispatcher,
                computation = rule.testDispatcher
            ),
            logger = logger
        )
    }

    @Test
    fun `should keep column order when an existing group is updated`() = runTest {
        stubInitialGroups(listOf(empty, groupA, groupB, groupC))

        // Backend re-emits group B with a changed value (same id).
        val updatedB = DataViewGroup(id = "B", value = DataViewGroup.Value.Tag(listOf("optB", "optX")))
        stubChannel(
            listOf(
                SubscriptionEvent.Group(group = updatedB, remove = false, subscription = subscription)
            )
        )

        container.observe(params()).test {
            assertEquals(
                expected = listOf("empty", "A", "B", "C"),
                actual = awaitItem().map { it.id }
            )
            val afterUpdate = awaitItem()
            // B stays in place (not moved to the end) and carries the updated value.
            assertEquals(expected = listOf("empty", "A", "B", "C"), actual = afterUpdate.map { it.id })
            assertEquals(expected = updatedB, actual = afterUpdate[2])
            awaitComplete()
        }
    }

    @Test
    fun `should append a brand new group to the end`() = runTest {
        stubInitialGroups(listOf(empty, groupA, groupB, groupC))

        val groupD = DataViewGroup(id = "D", value = DataViewGroup.Value.Tag(listOf("optD")))
        stubChannel(
            listOf(
                SubscriptionEvent.Group(group = groupD, remove = false, subscription = subscription)
            )
        )

        container.observe(params()).test {
            assertEquals(expected = listOf("empty", "A", "B", "C"), actual = awaitItem().map { it.id })
            assertEquals(expected = listOf("empty", "A", "B", "C", "D"), actual = awaitItem().map { it.id })
            awaitComplete()
        }
    }

    private fun stubInitialGroups(groups: List<DataViewGroup>) {
        repo.stub {
            onBlocking {
                objectGroupsSubscribe(
                    space = space,
                    subscription = subscription,
                    relationKey = relationKey,
                    filters = emptyList(),
                    source = emptyList(),
                    collection = null
                )
            } doReturn groups
        }
    }

    private fun stubChannel(events: List<SubscriptionEvent>) {
        channel.stub {
            on { subscribe(listOf(subscription)) } doReturn flow { emit(events) }
        }
    }

    private fun params() = BoardGroupSubscriptionContainer.Params(
        space = space,
        subscription = subscription,
        relationKey = relationKey,
        filters = emptyList(),
        sources = emptyList(),
        collection = null
    )
}

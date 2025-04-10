package com.anytypeio.anytype.presentation

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Process.Event
import com.anytypeio.anytype.domain.workspace.EventProcessMigrationChannel
import com.anytypeio.anytype.presentation.auth.account.MigrationHelperDelegate
import com.anytypeio.anytype.presentation.auth.account.MigrationProgressObserver
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class MigrationProgressObserverTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock
    lateinit var channel: EventProcessMigrationChannel

    lateinit var observer: MigrationProgressObserver

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        observer = MigrationProgressObserver(channel)
    }

    @Test
    fun test() = runTest {

        val processId = "test-process"
        val initEvent = Event.Migration.New(
            process = com.anytypeio.anytype.core_models.Process(
                id = processId,
                spaceId = "space-id",
                type = com.anytypeio.anytype.core_models.Process.Type.MIGRATION,
                state = com.anytypeio.anytype.core_models.Process.State.RUNNING,
                progress = null
            )
        )

        val updateEvent = Event.Migration.Update(
            process = com.anytypeio.anytype.core_models.Process(
                id = processId,
                spaceId = "space-id",
                type = com.anytypeio.anytype.core_models.Process.Type.MIGRATION,
                state = com.anytypeio.anytype.core_models.Process.State.RUNNING,
                progress = com.anytypeio.anytype.core_models.Process.Progress(done = 50, total = 100, message = "")
            )
        )

        val eventsFlow = flow {
            emit(listOf(initEvent)) // Emit the initial event
            emit(listOf(updateEvent)) // Emit the update event
        }

        channel.stub {
            on { observe() } doReturn eventsFlow
        }

        observer.state.test {
            // Test initial state
            val firstState = awaitItem() // Should receive initial event's state

            assertEquals(
                expected = MigrationHelperDelegate.State.InProgress.Idle,
                actual = firstState
            )

            val secondState = awaitItem()

            assertEquals(
                expected = MigrationHelperDelegate.State.InProgress.Progress(
                    processId = processId,
                    progress = 0f
                ),
                actual = secondState
            )

            // Test the updated state
            val updatedState = awaitItem() // Should receive the update event's state
            assert(updatedState is MigrationHelperDelegate.State.InProgress.Progress)
            assert((updatedState as MigrationHelperDelegate.State.InProgress.Progress).progress == 0.5f)

            awaitComplete() // Ensure the flow completes properly
        }
    }

    @Test
    fun testIdleState() = runTest {

        val eventsFlow = flow<List<Event.Migration>> {
            emit(listOf()) // Emit the initial event with no progress
        }

        channel.stub {
            on { observe() } doReturn eventsFlow
        }

        observer.state.test {
            // Test initial state
            val firstState = awaitItem() // Should receive initial event's state
            assertEquals(
                expected = MigrationHelperDelegate.State.InProgress.Idle,
                actual = firstState
            )

            awaitComplete() // Ensure the flow completes properly
        }
    }

    @Test
    fun testCompletedProgressState() = runTest {
        val processId = "test-process"
        val initEvent = Event.Migration.New(
            process = com.anytypeio.anytype.core_models.Process(
                id = processId,
                spaceId = "space-id",
                type = com.anytypeio.anytype.core_models.Process.Type.MIGRATION,
                state = com.anytypeio.anytype.core_models.Process.State.RUNNING,
                progress = null
            )
        )

        val completedEvent = Event.Migration.Done(
            process = com.anytypeio.anytype.core_models.Process(
                id = processId,
                spaceId = "space-id",
                type = com.anytypeio.anytype.core_models.Process.Type.MIGRATION,
                state = com.anytypeio.anytype.core_models.Process.State.DONE,
                progress = com.anytypeio.anytype.core_models.Process.Progress(done = 100, total = 100, message = "Migration complete")
            )
        )

        val eventsFlow = flow {
            emit(listOf(initEvent)) // Emit the initial event
            delay(100) // Simulate some delay
            emit(listOf(completedEvent)) // Emit the completed event
        }

        channel.stub {
            on { observe() } doReturn eventsFlow
        }

        observer.state.test {
            // Test initial state
            val firstState = awaitItem() // Should receive initial event's state
            assertEquals(
                expected = MigrationHelperDelegate.State.InProgress.Idle,
                actual = firstState
            )

            val secondState = awaitItem()

            assertEquals(
                expected = MigrationHelperDelegate.State.InProgress.Progress(
                    processId = processId,
                    progress = 0f
                ),
                actual = secondState
            )

            val completedState = awaitItem()
            assertTrue(completedState is MigrationHelperDelegate.State.Migrated)

            awaitComplete()

        }
    }
}
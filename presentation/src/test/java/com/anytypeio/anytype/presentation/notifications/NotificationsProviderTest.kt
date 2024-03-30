package com.anytypeio.anytype.presentation.notifications

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Notification
import com.anytypeio.anytype.core_models.NotificationPayload
import com.anytypeio.anytype.core_models.NotificationStatus
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.workspace.NotificationsChannel
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class NotificationsProviderTest {

    private val dispatchers = mock<AppCoroutineDispatchers>()
    private val scope = mock<CoroutineScope>()
    private val notificationsChannel = mock<NotificationsChannel>()
    private val awaitAccountStartManager = AwaitAccountStartManager.Default
    private val notificationsProvider = NotificationsProvider.Default(
        dispatchers,
        scope,
        notificationsChannel,
        awaitAccountStartManager
    )

    val testEvent = Notification.Event.Update(
        Notification(
            id = "id",
            createTime = 0,
            status = NotificationStatus.CREATED,
            isLocal = false,
            payload = NotificationPayload.RequestToJoin(
                spaceId = SpaceId("spaceId"),
                identity = "identity",
                identityName = "identityName",
                identityIcon = "identityIcon"
            ),
            space = SpaceId("space"),
            aclHeadId = "aclHeadId"
        )
    )

    @Test
    fun `observe should emit events when account is started`() = runTest {
        // Arrange
        val eventList = listOf(testEvent)
        whenever(notificationsChannel.observe()).thenReturn(flowOf(eventList))
        awaitAccountStartManager.setIsStarted(true)

        // Act
        val result = notificationsProvider.observe().first()

        // Assert
        assertEquals(eventList, result)
    }

    @Test
    fun `observe should stop emitting events when account is stopped`() = runTest {
        // Arrange
        val eventList = listOf(testEvent)
        whenever(notificationsChannel.observe()).thenReturn(flowOf(eventList))
        awaitAccountStartManager.setIsStarted(true) // Start and then stop
        awaitAccountStartManager.setIsStarted(false)

        notificationsProvider.observe().test(timeout = Duration.parse("2s")) { expectNoEvents() }
    }

    @Test
    fun `observe should emit the same event as sent by the channel when account is started`() =
        runBlocking {
            // Arrange
            val eventFlow = flowOf(listOf(testEvent))
            whenever(notificationsChannel.observe()).thenReturn(eventFlow)
            awaitAccountStartManager.setIsStarted(true)

            // Act
            val result = notificationsProvider.observe().first()

            // Assert
            assertEquals(listOf(testEvent), result)
        }

    @Test
    fun `observe should not emit any events after the account is stopped`() = runTest {
        // Arrange
        whenever(notificationsChannel.observe()).thenReturn(flowOf(listOf(testEvent)))
        awaitAccountStartManager.setIsStarted(true) // Start first
        awaitAccountStartManager.setIsStarted(false) // Then stop

        val collectedEvents = mutableListOf<List<Notification.Event>>()

        // Act
        val job = launch {
            notificationsProvider.observe().collect { events ->
                collectedEvents.add(events)
            }
        }
        delay(100) // Short delay to allow any emissions to be collected

        // Assert
        assertTrue(collectedEvents.isEmpty()) // Verify no events were collected

        job.cancel()
    }
}
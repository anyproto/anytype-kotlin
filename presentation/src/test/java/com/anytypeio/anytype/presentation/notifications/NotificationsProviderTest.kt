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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class NotificationsProviderTest {

    val dispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatchers = AppCoroutineDispatchers(
        io = dispatcher,
        main = dispatcher,
        computation = dispatcher
    ).also { Dispatchers.setMain(dispatcher) }
    private val scope = TestScope()
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
                identityIcon = "identityIcon",
                spaceName = "spaceName"
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
        notificationsProvider.events.test {
            assertEquals(emptyList<Notification.Event>(), awaitItem())
            assertEquals(eventList, awaitItem())
        }
    }

    @Test
    fun `observe should stop emitting events when account is stopped`() = runTest {
        // Arrange
        val eventList = listOf(testEvent)
        whenever(notificationsChannel.observe()).thenReturn(flowOf(eventList))
        awaitAccountStartManager.setIsStarted(true) // Start and then stop
        awaitAccountStartManager.setIsStarted(false)

        // Act & Assert
        notificationsProvider.events.test(timeout = Duration.parse("2s")) {
            assertEquals(emptyList<Notification.Event>(), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `observe should emit the same event as sent by the channel when account is started`() =
        runTest {
            // Arrange
            val eventFlow = flowOf(listOf(testEvent))
            whenever(notificationsChannel.observe()).thenReturn(eventFlow)
            awaitAccountStartManager.setIsStarted(true)

            // Act & Assert
            notificationsProvider.events.test(timeout = Duration.parse("2s")) {
                assertEquals(emptyList<Notification.Event>(), awaitItem())
                assertEquals(listOf(testEvent), awaitItem())
            }
        }
}
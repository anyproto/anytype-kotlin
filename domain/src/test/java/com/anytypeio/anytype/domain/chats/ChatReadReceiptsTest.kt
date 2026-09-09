package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChatReadReceiptsTest {
    @Test
    fun `back drains the final visibility update before returning`() = runTest {
        val f = fixture(listOf(message("A"), message("B")), state("B", 2))
        val job = f.start(backgroundScope)
        runCurrent()
        f.visible("A")
        runCurrent()
        f.visible("B")
        // No scheduler turn between reporting B and exiting.
        f.container.stop("chat")
        assertEquals(setOf("A", "B"), f.readIds)
        assertEquals(listOf("read:A", "unsubscribe", "read:B"), f.operations)
        job.cancelAndJoin()
    }

    @Test
    fun `collector cancellation drains a receipt whose worker has not started`() = runTest {
        val f = fixture(listOf(message("A")), state("A", 1))
        val job = f.start(backgroundScope)
        runCurrent()
        f.visible("A")
        job.cancelAndJoin()
        assertEquals(setOf("A"), f.readIds)
    }

    @Test
    fun `in flight read survives collector cancellation`() = runTest {
        val f = fixture(listOf(message("A")), state("A", 1))
        val gate = CompletableDeferred<Unit>()
        f.beforeRead = { gate.await() }
        val job = f.start(backgroundScope)
        runCurrent()
        f.visible("A")
        runCurrent()
        job.cancelAndJoin()
        assertEquals(1, f.attempts.size)
        assertTrue(f.readIds.isEmpty())
        gate.complete(Unit)
        runCurrent()
        assertEquals(setOf("A"), f.readIds)
    }

    @Test
    fun `scrolling backwards cannot replace a pending newer read`() = runTest {
        val f = fixture(listOf(message("A"), message("B"), message("C")), state("C", 3))
        val gate = CompletableDeferred<Unit>()
        f.beforeRead = { if (it.beforeOrderId == "A") gate.await() }
        val job = f.start(backgroundScope)
        runCurrent()
        f.visible("A")
        runCurrent()
        f.visible("C")
        f.visible("B")
        gate.complete(Unit)
        runCurrent()
        assertEquals(listOf("A", "C"), f.attempts.map { it.beforeOrderId })
        assertEquals(setOf("A", "B", "C"), f.readIds)
        job.cancelAndJoin()
    }

    @Test
    fun `receipt merging never combines an old viewport with a newer state watermark`() = runTest {
        val repo = RecordingRepository(emptyList(), state("A", 1))
        val reads = ChatReadSession("chat", backgroundScope, repo, mock())
        val messages = listOf(message("A"), message("B"), message("C"))
        val old = reads.snapshot(ChatContainer.ChatStreamState(messages, state("A", 3)))
        reads.visible("C", old)
        // A new message may have been inserted in history while the user moved back to B.
        reads.visible("B", old)
        reads.visible("B", reads.snapshot(ChatContainer.ChatStreamState(messages, state("B", 3, order = 2))))
        reads.close().join()
        assertEquals(listOf("C" to "A", "B" to "B"), repo.attempts.map { it.beforeOrderId to it.lastStateId })
    }

    @Test
    fun `new state retries the same visible message after an older watermark excluded it`() = runTest {
        val f = fixture(listOf(message("A"), message("B")), state("A", 2))
        val job = f.start(backgroundScope)
        runCurrent()
        f.visible("B")
        runCurrent()
        assertEquals(setOf("A"), f.readIds)
        f.update(state("B", 1, oldest = "B", order = 2))
        runCurrent()
        assertEquals(setOf("A", "B"), f.readIds)
        assertEquals(listOf("A", "B"), f.attempts.map { it.lastStateId })
        job.cancelAndJoin()
    }

    @Test
    fun `late unread counters read an unchanged viewport`() = runTest {
        val f = fixture(listOf(message("A")), state("A", 0, oldest = ""))
        val job = f.start(backgroundScope)
        runCurrent()
        f.visible("A")
        runCurrent()
        assertTrue(f.attempts.isEmpty())
        f.update(state("A", 1, order = 2))
        runCurrent()
        assertEquals(setOf("A"), f.readIds)
        job.cancelAndJoin()
    }

    @Test
    fun `hidden viewport is not reread when a new state arrives`() = runTest {
        val f = fixture(listOf(message("A")), state("A", 0, oldest = ""))
        val job = f.start(backgroundScope)
        runCurrent()
        f.visible("A")
        f.hide()
        f.update(state("A", 1, order = 2))
        runCurrent()
        assertTrue(f.attempts.isEmpty())
        // Resuming with the same ids must report visibility again.
        f.visible("A")
        runCurrent()
        assertEquals(setOf("A"), f.readIds)
        job.cancelAndJoin()
    }

    @Test
    fun `forward paging applies response state before reporting new messages`() = runTest {
        val f = fixture(listOf(message("A")), state("A", 1))
        val job = f.start(backgroundScope)
        runCurrent()
        val b = message("B")
        f.backend[b.id] = b
        f.page = { Command.ChatCommand.GetMessages.Response(listOf(b), state("B", 2, order = 2)) }
        f.container.onLoadNext()
        runCurrent()
        assertEquals("B", f.latest?.state?.lastStateId)
        f.visible("B")
        runCurrent()
        assertEquals(setOf("A", "B"), f.readIds)
        job.cancelAndJoin()
    }

    @Test
    fun `older paging state does not replace a newer event state`() = runTest {
        val f = fixture(listOf(message("A")), state("A", 1))
        val job = f.start(backgroundScope)
        runCurrent()
        f.update(state("C", 1, order = 3))
        runCurrent()
        f.page = { Command.ChatCommand.GetMessages.Response(emptyList(), state("B", 1, order = 2)) }
        f.container.onLoadNext()
        runCurrent()
        assertEquals("C", f.latest?.state?.lastStateId)
        job.cancelAndJoin()
    }

    @Test
    fun `messages and mentions are independent and serialized`() = runTest {
        val f = fixture(listOf(message("A")), state("A", 1).copy(unreadMentions = Chat.State.UnreadState("A", 1)))
        val gate = CompletableDeferred<Unit>()
        f.beforeRead = { if (!it.isMention) gate.await() }
        val job = f.start(backgroundScope)
        runCurrent()
        f.visible("A")
        runCurrent()
        assertEquals(1, f.attempts.size)
        assertFalse(f.attempts.single().isMention)
        gate.complete(Unit)
        runCurrent()
        assertEquals(listOf(false, true), f.attempts.map { it.isMention })
        assertTrue(f.attempts.all { it.afterOrderId == null })
        job.cancelAndJoin()
    }

    @Test
    fun `transient read failure retries without another visibility callback`() = runTest {
        val f = fixture(listOf(message("A")), state("A", 1))
        f.beforeRead = { if (f.attempts.size == 1) error("temporary failure") }
        val job = f.start(backgroundScope)
        runCurrent()
        f.visible("A")
        runCurrent()
        advanceTimeBy(250)
        runCurrent()
        assertEquals(2, f.attempts.size)
        assertEquals(setOf("A"), f.readIds)
        job.cancelAndJoin()
    }

    @Test
    fun `permanent failure has bounded retries and does not hang exit`() = runTest {
        val f = fixture(listOf(message("A")), state("A", 1))
        f.beforeRead = { error("unavailable") }
        val job = f.start(backgroundScope)
        runCurrent()
        f.visible("A")
        val stop = launch { f.container.stop("chat") }
        advanceTimeBy(2_000)
        runCurrent()
        assertTrue(stop.isCompleted)
        assertEquals(6, f.attempts.size) // three attempts, then a final exit retry batch
        assertEquals(listOf("unsubscribe"), f.operations)
        job.cancelAndJoin()
    }

    @Test
    fun `a later unread state can reread the same order and watermark`() = runTest {
        val f = fixture(listOf(message("A")), state("A", 1))
        val job = f.start(backgroundScope)
        runCurrent()
        f.visible("A")
        runCurrent()
        f.update(state("A", 0, oldest = "", order = 2))
        runCurrent()
        f.update(state("A", 1, order = 3))
        runCurrent()
        assertEquals(2, f.attempts.size)
        job.cancelAndJoin()
    }

    @Test
    fun `stalled receipt does not block exit or unsubscribe a reopened chat later`() = runTest {
        val f = fixture(listOf(message("A"), message("B")), state("B", 2))
        val gate = CompletableDeferred<Unit>()
        f.beforeRead = { gate.await() }
        val oldWatcher = f.start(backgroundScope)
        runCurrent()
        f.visible("A")
        runCurrent()
        f.visible("B")
        val exit = launch { f.container.stop("chat") }
        runCurrent()
        assertFalse(exit.isCompleted)
        assertEquals(listOf("unsubscribe"), f.operations)
        oldWatcher.cancelAndJoin()
        f.hide()
        // Reopen even before the previous exit's bounded wait has completed.
        val newWatcher = f.start(backgroundScope)
        runCurrent()
        advanceTimeBy(ChatContainer.READ_EXIT_WAIT_MS)
        runCurrent()
        assertTrue(exit.isCompleted)
        assertTrue(f.readIds.isEmpty())
        gate.complete(Unit)
        runCurrent()
        assertEquals(setOf("A", "B"), f.readIds)
        assertEquals(listOf("A", "B"), f.attempts.map { it.beforeOrderId })
        assertEquals(1, f.operations.count { it == "unsubscribe" })
        newWatcher.cancelAndJoin()
    }

    @Test
    fun `a new backfill snapshot cannot be read by an older viewport callback`() = runTest {
        val repo = RecordingRepository(emptyList(), state("A", 1))
        val reads = ChatReadSession("chat", backgroundScope, repo, mock())
        val old = reads.snapshot(ChatContainer.ChatStreamState(listOf(message("A")), state("A", 1)))
        reads.visible("A", old)
        runCurrent()
        val fresh = reads.snapshot(ChatContainer.ChatStreamState(listOf(message("0"), message("A")), state("B", 2, oldest = "0", order = 2)))
        reads.visible("A", old)
        runCurrent()
        assertEquals(listOf("A"), repo.attempts.map { it.lastStateId })
        reads.visible("A", fresh)
        reads.close().join()
        assertEquals(listOf("A", "B"), repo.attempts.map { it.lastStateId })
    }

    @Test
    fun `snapshots from a previous session cannot mark the reopened chat read`() = runTest {
        val repo = RecordingRepository(emptyList(), state("A", 1))
        val previous = ChatReadSession("chat", backgroundScope, repo, mock())
        val old = previous.snapshot(ChatContainer.ChatStreamState(listOf(message("A")), state("A", 1)))
        previous.close().join()
        val reopened = ChatReadSession("chat", backgroundScope, repo, mock())
        reopened.visible("A", old)
        reopened.close().join()
        assertTrue(repo.attempts.isEmpty())
    }

    @Test
    fun `legacy empty state watermark is preserved as an empty RPC bound`() = runTest {
        val f = fixture(listOf(message("A")), state("", 1))
        val job = f.start(backgroundScope)
        runCurrent()
        f.visible("A")
        runCurrent()
        assertEquals("", f.attempts.single().lastStateId)
        job.cancelAndJoin()
    }

    @Test
    fun `successful wider receipt removes a covered failed receipt before exit`() = runTest {
        val f = fixture(listOf(message("A"), message("B")), state("B", 2))
        f.beforeRead = { if (it.beforeOrderId == "A") error("temporary failure") }
        val job = f.start(backgroundScope)
        runCurrent()
        f.visible("A")
        runCurrent()
        f.visible("B")
        advanceTimeBy(1_000)
        runCurrent()
        f.container.stop("chat")
        assertEquals(listOf("A", "A", "A", "B"), f.attempts.map { it.beforeOrderId })
        job.cancelAndJoin()
    }

    @Test
    fun `state events preserve an active scroll intent until the UI clears it`() = runTest {
        val f = fixture(listOf(message("A")), state("A", 1))
        val job = f.start(backgroundScope)
        runCurrent()
        val intent = f.latest?.intent
        assertTrue(intent is ChatContainer.Intent.ScrollToMessage)
        f.update(state("A", 0, oldest = "", order = 2))
        runCurrent()
        assertEquals(intent, f.latest?.intent)
        f.container.onClearIntent()
        runCurrent()
        assertEquals(ChatContainer.Intent.None, f.latest?.intent)
        job.cancelAndJoin()
    }

    @Test
    fun `deleting the scroll target cancels its intent instead of waiting on a missing index`() = runTest {
        val f = fixture(listOf(message("A")), state("A", 1))
        val job = f.start(backgroundScope)
        runCurrent()
        assertTrue(f.latest?.intent is ChatContainer.Intent.ScrollToMessage)
        f.container.onPayload(listOf(Event.Command.Chats.Delete("chat", "A")))
        runCurrent()
        assertEquals(ChatContainer.Intent.None, f.latest?.intent)
        assertTrue(f.latest?.messages.orEmpty().isEmpty())
        job.cancelAndJoin()
    }

    @Test
    fun `backward paging and tail loads preserve fetched chat state`() = runTest {
        for (loadPrevious in listOf(true, false)) {
            val f = fixture(listOf(message("B")), state("B", 1, oldest = "B"))
            val job = f.start(backgroundScope)
            runCurrent()
            f.page = { Command.ChatCommand.GetMessages.Response(listOf(message("C")), state("C", 2, order = 2)) }
            if (loadPrevious) f.container.onLoadPrevious() else f.container.onLoadChatTail("B")
            runCurrent()
            assertEquals("C", f.latest?.state?.lastStateId)
            job.cancelAndJoin()
        }
    }

    @Test
    fun `initial unread window keeps the newest state of both around queries`() = runTest {
        val f = fixture(listOf(message("C")), state("C", 2, oldest = "A"))
        f.page = { command ->
            if (command.beforeOrderId != null) {
                Command.ChatCommand.GetMessages.Response(emptyList(), state("D", 2, order = 2))
            } else {
                Command.ChatCommand.GetMessages.Response(listOf(message("A"), message("B")), state("E", 2, order = 3))
            }
        }
        val job = f.start(backgroundScope)
        runCurrent()
        assertEquals("E", f.latest?.state?.lastStateId)
        f.visible("B")
        runCurrent()
        assertEquals("E", f.attempts.single().lastStateId)
        job.cancelAndJoin()
    }

    private fun TestScope.fixture(messages: List<Chat.Message>, state: Chat.State) =
        Fixture(messages, state, backgroundScope)

    private class Fixture(messages: List<Chat.Message>, state: Chat.State, readScope: CoroutineScope) : RecordingRepository(messages, state) {
        private val events = MutableSharedFlow<List<Event.Command.Chats>>()
        var latest: ChatContainer.ChatStreamState? = null
        val container = ChatContainer(this, object : ChatEventChannel {
            override fun observe(chat: String) = events
            override fun subscribe(subscribe: String) = events
        }, mock<Logger>(), mock(), readScope)

        fun start(scope: CoroutineScope) = scope.launch {
            container.watch("chat").collect {
                latest = it
                // Model Compose acknowledging a new UI snapshot even if the range is unchanged.
                visibleId?.let { id -> container.onVisibleRangeChanged(id, id, it.readSnapshot) }
            }
        }

        private var visibleId: String? = null
        fun visible(id: String) {
            visibleId = id
            container.onVisibleRangeChanged(id, id, latest?.readSnapshot)
        }
        fun hide() {
            visibleId = null
            container.onVisibleRangeChanged(null, null)
        }
        suspend fun update(state: Chat.State) = events.emit(listOf(Event.Command.Chats.UpdateState("chat", state)))
    }

    private open class RecordingRepository(
        private val initial: List<Chat.Message>,
        private val initialState: Chat.State
    ) : BlockRepository by mock() {
        val attempts = mutableListOf<Command.ChatCommand.ReadMessages>()
        val operations = mutableListOf<String>()
        val readIds = mutableSetOf<String>()
        val backend = initial.associateByTo(linkedMapOf()) { it.id }
        var beforeRead: suspend (Command.ChatCommand.ReadMessages) -> Unit = {}
        var page: (Command.ChatCommand.GetMessages) -> Command.ChatCommand.GetMessages.Response = {
            Command.ChatCommand.GetMessages.Response(emptyList())
        }

        override suspend fun subscribeLastChatMessages(command: Command.ChatCommand.SubscribeLastMessages) =
            Command.ChatCommand.SubscribeLastMessages.Response(initial, 0, initialState)

        override suspend fun getChatMessages(command: Command.ChatCommand.GetMessages) = page(command)

        override suspend fun readChatMessages(command: Command.ChatCommand.ReadMessages) {
            attempts += command
            beforeRead(command)
            operations += "read:${command.beforeOrderId}"
            // Simulate Heart's inclusive order and insertion-state bounds. Fixture message
            // ids double as insertion state ids, independently of command state.order.
            if (!command.isMention) backend.values.filter {
                it.order <= command.beforeOrderId.orEmpty() && it.id <= command.lastStateId.orEmpty()
            }.forEach { readIds += it.id }
        }

        override suspend fun unsubscribeChat(chat: String) { operations += "unsubscribe" }
        override suspend fun cancelObjectSearchSubscription(subscriptions: List<String>) = Unit
    }

    companion object {
        private fun message(id: String) = Chat.Message(id, id, "someone", 0, 0, null)
        private fun state(last: String, count: Int, oldest: String = "A", order: Long = 1) = Chat.State(
            unreadMessages = Chat.State.UnreadState(oldest, count), lastStateId = last, order = order
        )
    }
}

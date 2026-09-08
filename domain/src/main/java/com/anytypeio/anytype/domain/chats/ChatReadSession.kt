package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Owns read receipts for one collection of a chat. Visibility is recorded synchronously,
 * before Back can unsubscribe or cancel the collector. RPCs run in a single worker.
 *
 * Pending receipts retain the order boundary AND the database state at which that boundary
 * was observed. Taking the maximum of each independently could read a newly inserted message
 * that was never visible. We only discard a receipt when another actually covers both bounds.
 */
internal class ChatReadSession(
    val chat: Id,
    scope: CoroutineScope,
    private val repo: BlockRepository,
    private val logger: Logger
) {
    private val lock = Any()
    private val wake = Channel<Unit>(Channel.CONFLATED)
    private var accepting = true
    private var visibleMessage: Id? = null
    private var inFlight: Receipt? = null
    private val pending = mutableListOf<Receipt>()
    private val completed = mutableListOf<Receipt>()
    private val failed = mutableListOf<Receipt>()

    val newestVisibleMessageId: Id?
        get() = synchronized(lock) { visibleMessage }

    private val worker = scope.launch {
        for (signal in wake) drain()
        // One final bounded retry batch after closing. There is only one drain owner,
        // even when the screen is already gone or two exit paths both call close().
        synchronized(lock) {
            pending.addAll(failed)
            failed.clear()
        }
        drain()
    }

    fun snapshot(stream: ChatContainer.ChatStreamState) = ChatReadSnapshot(
        owner = this,
        orders = stream.messages.associate { it.id to it.order },
        state = stream.state
    )

    fun visible(from: Id?, snapshot: ChatReadSnapshot?) = synchronized(lock) {
        if (snapshot != null && snapshot.owner !== this) return@synchronized
        visibleMessage = from
        val order = snapshot?.orders?.get(from) ?: return@synchronized
        // Only the UI can acknowledge a new snapshot. A delayed callback from an older
        // layout must never adopt the latest database watermark or read a new backfill.
        completed.removeAll { it.stateOrder < snapshot.state.order }
        enqueue(order, snapshot.state, isMention = false)
        enqueue(order, snapshot.state, isMention = true)
    }

    fun readMention(beforeOrderId: Id, state: Chat.State) = synchronized(lock) {
        enqueue(beforeOrderId, state, isMention = true)
    }

    private fun enqueue(before: Id, state: Chat.State, isMention: Boolean) {
        if (!accepting) return
        val unread = if (isMention) state.unreadMentions else state.unreadMessages
        val lastStateId = state.lastStateId.orEmpty()
        if (unread == null || unread.counter <= 0 || unread.olderOrderId.isEmpty() ||
            before < unread.olderOrderId
        ) return

        val receipt = Receipt(
            command = Command.ChatCommand.ReadMessages(
                chat = chat,
                beforeOrderId = before,
                lastStateId = lastStateId,
                isMention = isMention
            ),
            stateOrder = state.order
        )
        if (completed.any { it.covers(receipt) } || pending.any { it.covers(receipt) } ||
            inFlight?.covers(receipt) == true || failed.any { it.covers(receipt) }
        ) return

        pending.removeAll { receipt.covers(it) }
        failed.removeAll { receipt.covers(it) }
        pending += receipt
        wake.trySend(Unit)
    }

    /**
     * Freeze visibility and drain on the application-owned worker. Closing a screen never
     * cancels that worker, and waiting for a stalled native RPC must not block navigation.
     */
    fun close(): Job = synchronized(lock) {
        accepting = false
        wake.close()
        worker
    }

    private suspend fun drain() {
        while (true) {
            val receipt = synchronized(lock) {
                if (pending.isEmpty()) null else pending.removeAt(0).also { inFlight = it }
            } ?: return
            try {
                val success = sendWithRetry(receipt.command)
                synchronized(lock) {
                    if (success) {
                        completed.removeAll { receipt.covers(it) }
                        failed.removeAll { receipt.covers(it) }
                        completed += receipt
                    } else {
                        failed += receipt
                    }
                }
            } catch (e: CancellationException) {
                synchronized(lock) { pending.add(0, receipt) }
                throw e
            } finally {
                synchronized(lock) { inFlight = null }
            }
        }
    }

    private suspend fun sendWithRetry(command: Command.ChatCommand.ReadMessages): Boolean {
        repeat(3) { attempt ->
            try {
                repo.readChatMessages(command)
                return true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.logWarning("Error reading chat $chat through ${command.beforeOrderId} " +
                    "at state ${command.lastStateId}, mention=${command.isMention}, " +
                    "attempt=${attempt + 1}: ${e.message}")
                if (attempt < 2) delay(250L * (attempt + 1))
            }
        }
        return false
    }

    private data class Receipt(
        val command: Command.ChatCommand.ReadMessages,
        val stateOrder: Long
    ) {
        fun covers(other: Receipt): Boolean =
            command.isMention == other.command.isMention &&
                stateOrder >= other.stateOrder &&
                command.beforeOrderId.orEmpty() >= other.command.beforeOrderId.orEmpty() &&
                command.lastStateId.orEmpty() >= other.command.lastStateId.orEmpty()
    }
}

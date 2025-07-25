package com.anytypeio.anytype.domain.chats

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.StubChatMessage
import com.anytypeio.anytype.core_models.StubChatMessageContent
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class ChatContainerTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    val dispatchers = AppCoroutineDispatchers(
        io = rule.dispatcher,
        computation = rule.dispatcher,
        main = rule.dispatcher
    )

    @Mock
    lateinit var channel: ChatEventChannel

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var logger: Logger

    @Mock
    lateinit var storelessSubscriptionContainer: StorelessSubscriptionContainer

    private val givenChatID = MockDataFactory.randomUuid()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test()
    fun `should add one message to basic initial state`() = runTest {

        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger,
            subscription = storelessSubscriptionContainer
        )

        val msg = StubChatMessage(
            content = StubChatMessageContent(
                text = "With seemingly endless talent and versatility, Sully puts his garage hat on to produce one super-slick plate"
            )
        )

        repo.stub {
            onBlocking {
                subscribeLastChatMessages(
                    Command.ChatCommand.SubscribeLastMessages(
                        chat = givenChatID,
                        limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
                    )
                )
            } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                messages = emptyList(),
                messageCountBefore = 0
            )
        }

        channel.stub {
            on {
                observe(chat = givenChatID)
            } doReturn flow {
                delay(300)
                emit(
                    listOf(
                        Event.Command.Chats.Add(
                            context = givenChatID,
                            message = msg,
                            id = msg.id,
                            order = "A",
                            spaceId = SpaceId(MockDataFactory.randomUuid())
                        )
                    )
                )
            }
        }

        container.watch(givenChatID).test {
            val first = awaitItem()
            assertEquals(
                expected = emptyList(),
                actual = first.messages
            )
            advanceUntilIdle()
            val second = awaitItem()
            assertEquals(
                expected = listOf(
                    msg
                ),
                actual = second.messages
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test()
    fun `should update existing message`() = runTest {

        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger,
            subscription = storelessSubscriptionContainer
        )

        val initialMsg = StubChatMessage(
            id = "msg1",
            content = StubChatMessageContent(
                text = "Hello, Walter"
            )
        )

        val updatedMsg = StubChatMessage(
            id = "msg1", // Same ID as initial message
            content = StubChatMessageContent(
                text = "Hello, Jesse" // Updated text
            )
        )

        repo.stub {
            onBlocking {
                subscribeLastChatMessages(
                    Command.ChatCommand.SubscribeLastMessages(
                        chat = givenChatID,
                        limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
                    )
                )
            } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                messages = listOf(initialMsg),
                messageCountBefore = 0
            )
        }

        channel.stub {
            on {
                observe(chat = givenChatID)
            } doReturn flow {
                delay(300)
                emit(
                    listOf(
                        Event.Command.Chats.Update(
                            context = givenChatID,
                            message = updatedMsg,
                            id = updatedMsg.id
                        )
                    )
                )
            }
        }

        container.watch(givenChatID).test {
            val first = awaitItem()
            assertEquals(
                expected = listOf(
                    initialMsg
                ),
                actual = first.messages
            )
            advanceUntilIdle()
            val second = awaitItem()
            assertEquals(
                expected = listOf(
                    updatedMsg // Should have updated message with new text
                ),
                actual = second.messages
            )
            // Verify the text was actually updated
            assertEquals("Hello, Jesse", second.messages.first().content?.text)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test()
    fun `should delete existing message`() = runTest {

        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger,
            subscription = storelessSubscriptionContainer
        )

        val messageToDelete = StubChatMessage(
            id = "msg1",
            content = StubChatMessageContent(
                text = "This message will be deleted"
            )
        )

        repo.stub {
            onBlocking {
                subscribeLastChatMessages(
                    Command.ChatCommand.SubscribeLastMessages(
                        chat = givenChatID,
                        limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
                    )
                )
            } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                messages = listOf(messageToDelete),
                messageCountBefore = 0
            )
        }

        channel.stub {
            on {
                observe(chat = givenChatID)
            } doReturn flow {
                delay(300)
                emit(
                    listOf(
                        Event.Command.Chats.Delete(
                            context = givenChatID,
                            message = messageToDelete.id
                        )
                    )
                )
            }
        }

        container.watch(givenChatID).test {
            val first = awaitItem()
            assertEquals(
                expected = listOf(
                    messageToDelete
                ),
                actual = first.messages
            )
            advanceUntilIdle()
            val second = awaitItem()
            assertEquals(
                expected = emptyList(),
                actual = second.messages
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test()
    fun `should load next page of messages`() = runTest {

        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger,
            subscription = storelessSubscriptionContainer
        )

        val initialMessage = StubChatMessage(order = "A")
        val newerMessage = StubChatMessage(order = "B")

        repo.stub {
            onBlocking {
                subscribeLastChatMessages(
                    Command.ChatCommand.SubscribeLastMessages(
                        chat = givenChatID,
                        limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
                    )
                )
            } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                messages = listOf(initialMessage),
                messageCountBefore = 0
            )

            onBlocking {
                getChatMessages(
                    Command.ChatCommand.GetMessages(
                        chat = givenChatID,
                        afterOrderId = initialMessage.order,
                        limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
                    )
                )
            } doReturn Command.ChatCommand.GetMessages.Response(
                messages = listOf(newerMessage)
            )
        }

        channel.stub {
            on {
                observe(chat = givenChatID)
            } doReturn flow { }
        }

        container.watch(givenChatID).test {
            val initial = awaitItem()
            assertEquals(
                expected = listOf(initialMessage),
                actual = initial.messages
            )

            container.onLoadNext()

            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(
                expected = listOf(initialMessage, newerMessage),
                actual = updated.messages
            )
        }
    }

    @Test()
    fun `should insert new message before existing message according to order`() = runTest {

        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger,
            subscription = storelessSubscriptionContainer
        )

        val initialMsg = StubChatMessage(
            order = "B",
            content = StubChatMessageContent(
                text = "Hello, "
            )
        )

        val newMsg = StubChatMessage(
            content = StubChatMessageContent(
                text = "Hello, "
            ),
            order = "A"
        )

        repo.stub {
            onBlocking {
                subscribeLastChatMessages(
                    Command.ChatCommand.SubscribeLastMessages(
                        chat = givenChatID,
                        limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
                    )
                )
            } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                messages = listOf(initialMsg),
                messageCountBefore = 0
            )
        }

        channel.stub {
            on {
                observe(chat = givenChatID)
            } doReturn flow {
                delay(300)
                emit(
                    listOf(
                        Event.Command.Chats.Add(
                            context = givenChatID,
                            message = newMsg,
                            id = newMsg.id,
                            order = newMsg.order,
                            spaceId = SpaceId(MockDataFactory.randomUuid())
                        )
                    )
                )
            }
        }

        container.watch(givenChatID).test {
            val first = awaitItem()
            assertEquals(
                expected = listOf(
                    initialMsg
                ),
                actual = first.messages
            )
            advanceUntilIdle()
            val second = awaitItem()
            assertEquals(
                expected = listOf(
                    newMsg,
                    initialMsg
                ),
                actual = second.messages
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should load next messages and prepend to state`() = runTest {

        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger,
            subscription = storelessSubscriptionContainer
        )

        val firstMessage = StubChatMessage(order = "B")
        val nextMessage = StubChatMessage(order = "A")

        repo.stub {
            onBlocking {
                subscribeLastChatMessages(
                    Command.ChatCommand.SubscribeLastMessages(
                        chat = givenChatID,
                        limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
                    )
                )
            } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                messages = listOf(firstMessage),
                messageCountBefore = 0
            )

            onBlocking {
                getChatMessages(
                    Command.ChatCommand.GetMessages(
                        chat = givenChatID,
                        beforeOrderId = firstMessage.order,
                        limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE,
                        afterOrderId = null
                    )
                )
            } doReturn Command.ChatCommand.GetMessages.Response(
                messages = listOf(nextMessage)
            )
        }

        channel.stub {
            on {
                observe(chat = givenChatID)
            } doReturn flow { }
        }

        container.watch(givenChatID).test {
            val initial = awaitItem()
            assertEquals(
                expected = listOf(firstMessage),
                actual = initial.messages
            )

            container.onLoadPrevious()

            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(
                expected = listOf(nextMessage, firstMessage),
                actual = updated.messages
            )
        }
    }

    @Test
    fun `should scroll to bottom when scroll-to-bottom is clicked when subscribing chat`() =
        runTest {
            turbineScope {
                val container = ChatContainer(
                    repo = repo,
                    channel = channel,
                    logger = logger,
                    subscription = storelessSubscriptionContainer
                )

                val messages = buildList {
                    repeat(100) {
                        add(
                            StubChatMessage(
                                id = it.toString(),
                                order = it.toString()
                            )
                        )
                    }
                }

                val state = Chat.State.UnreadState(
                    counter = 10,
                    olderOrderId = "90"
                )

                repo.stub {
                    onBlocking {
                        subscribeLastChatMessages(
                            Command.ChatCommand.SubscribeLastMessages(
                                chat = givenChatID,
                                limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
                            )
                        )
                    } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                        messages = messages.takeLast(10),
                        messageCountBefore = 0,
                        chatState = Chat.State(unreadMessages = state)
                    )

                    onBlocking {
                        getChatMessages(
                            Command.ChatCommand.GetMessages(
                                chat = givenChatID,
                                beforeOrderId = state.olderOrderId,
                                limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE,
                                afterOrderId = null,
                                includeBoundary = false
                            )
                        )
                    } doReturn Command.ChatCommand.GetMessages.Response(
                        messages = messages.slice(80..89)
                    )

                    onBlocking {
                        getChatMessages(
                            Command.ChatCommand.GetMessages(
                                chat = givenChatID,
                                afterOrderId = state.olderOrderId,
                                limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE,
                                includeBoundary = true
                            )
                        )
                    } doReturn Command.ChatCommand.GetMessages.Response(
                        messages = messages.slice(90..99)
                    )
                }

                channel.stub {
                    on { observe(chat = givenChatID) } doReturn emptyFlow()
                }

                container.watch(givenChatID).test {

                    val initial = awaitItem()

                    assertEquals(
                        expected = messages.takeLast(10),
                        actual = initial.messages,
                    )

                    assertEquals(
                        expected = ChatContainer.Intent.ScrollToMessage(
                            id = "90",
                            startOfUnreadMessageSection = true
                        ),
                        actual = initial.intent,
                    )

                    container.onLoadChatTail(
                        msg = "80"
                    )

                    val next = awaitItem()

                    assertEquals(
                        expected = ChatContainer.Intent.ScrollToBottom,
                        actual = next.intent,
                    )

                    // New state is not emitted, since it does not change.
                }
            }
        }

    @Test
    fun `should scroll to unread message section from subscription results after opening chat`() =
        runTest {

            val container = ChatContainer(
                repo = repo,
                channel = channel,
                logger = logger,
                subscription = storelessSubscriptionContainer
            )

            val messages = buildList {
                repeat(10) {
                    add(
                        StubChatMessage(
                            id = it.toString(),
                            order = it.toString()
                        )
                    )
                }
            }

            val state = Chat.State.UnreadState(
                counter = 1,
                olderOrderId = "9"
            )

            val subscriptionCommand = Command.ChatCommand.SubscribeLastMessages(
                chat = givenChatID,
                limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
            )

            repo.stub {
                onBlocking {
                    subscribeLastChatMessages(subscriptionCommand)
                } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                    messages = messages,
                    messageCountBefore = 0,
                    chatState = Chat.State(unreadMessages = state)
                )
            }

            channel.stub {
                on { observe(chat = givenChatID) } doReturn emptyFlow()
            }

            container.watch(givenChatID).test {

                val initial = awaitItem()

                assertEquals(
                    expected = messages,
                    actual = initial.messages,
                )

                assertEquals(
                    expected = ChatContainer.Intent.ScrollToMessage(
                        id = "9",
                        startOfUnreadMessageSection = true,
                        smooth = false
                    ),
                    actual = initial.intent,
                )

                verify(repo, times(1)).subscribeLastChatMessages(subscriptionCommand)
                verify(repo, never()).getChatMessages(any())
            }
        }

    @Test
    fun `should scroll to unread message section from unread-message section after opening chat`() =
        runTest {

            val container = ChatContainer(
                repo = repo,
                channel = channel,
                logger = logger,
                subscription = storelessSubscriptionContainer
            )

            val allMessages = buildList {
                repeat(200) {
                    add(
                        StubChatMessage(
                            id = it.toString(),
                            order = it.toString()
                        )
                    )
                }
            }

            val state = Chat.State.UnreadState(
                counter = 150,
                olderOrderId = "50"
            )

            val subscriptionCommand = Command.ChatCommand.SubscribeLastMessages(
                chat = givenChatID,
                limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
            )

            repo.stub {
                onBlocking {
                    subscribeLastChatMessages(subscriptionCommand)
                } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                    messages = allMessages.takeLast(ChatContainer.DEFAULT_CHAT_PAGING_SIZE),
                    messageCountBefore = 0,
                    chatState = Chat.State(unreadMessages = state)
                )
                onBlocking {
                    getChatMessages(
                        Command.ChatCommand.GetMessages(
                            chat = givenChatID,
                            beforeOrderId = state.olderOrderId,
                            limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE / 2,
                            afterOrderId = null,
                            includeBoundary = false
                        )
                    )
                } doReturn Command.ChatCommand.GetMessages.Response(
                    messages = allMessages.slice(0..49)
                )

                onBlocking {
                    getChatMessages(
                        Command.ChatCommand.GetMessages(
                            chat = givenChatID,
                            afterOrderId = state.olderOrderId,
                            limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE / 2,
                            includeBoundary = true
                        )
                    )
                } doReturn Command.ChatCommand.GetMessages.Response(
                    messages = allMessages.slice(50..99)
                )
            }

            channel.stub {
                on { observe(chat = givenChatID) } doReturn emptyFlow()
            }

            container.watch(givenChatID).test {

                val initial = awaitItem()

                assertEquals(
                    expected = allMessages.slice(0..99),
                    actual = initial.messages,
                )

                assertEquals(
                    expected = ChatContainer.Intent.ScrollToMessage(
                        id = "50",
                        startOfUnreadMessageSection = true,
                        smooth = false
                    ),
                    actual = initial.intent,
                )

                verify(repo, times(1)).subscribeLastChatMessages(subscriptionCommand)
                verify(repo, times(2)).getChatMessages(any())
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should apply ChatState update with higher order`() = runTest {
        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger,
            subscription = storelessSubscriptionContainer
        )

        val initialState = Chat.State(order = 1L)
        val newerState = Chat.State(order = 2L)

        repo.stub {
            onBlocking {
                subscribeLastChatMessages(
                    Command.ChatCommand.SubscribeLastMessages(
                        chat = givenChatID,
                        limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
                    )
                )
            } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                messages = emptyList(),
                messageCountBefore = 0,
                chatState = initialState
            )
        }

        channel.stub {
            on { observe(chat = givenChatID) } doReturn flow {
                emit(
                    listOf(
                        Event.Command.Chats.UpdateState(
                            context = givenChatID,
                            state = newerState
                        )
                    )
                )
            }
        }

        container.watch(givenChatID).test {
            val initial = awaitItem()
            assertEquals(1L, initial.state.order)

            val updated = awaitItem()
            assertEquals(2L, updated.state.order)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should reject ChatState update with lower order`() = runTest {
        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger,
            subscription = storelessSubscriptionContainer
        )

        val newerState = Chat.State(order = 5L)
        val olderState = Chat.State(order = 3L)

        repo.stub {
            onBlocking {
                subscribeLastChatMessages(
                    Command.ChatCommand.SubscribeLastMessages(
                        chat = givenChatID,
                        limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
                    )
                )
            } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                messages = emptyList(),
                messageCountBefore = 0,
                chatState = newerState
            )
        }

        channel.stub {
            on { observe(chat = givenChatID) } doReturn flow {
                emit(
                    listOf(
                        Event.Command.Chats.UpdateState(
                            context = givenChatID,
                            state = olderState
                        )
                    )
                )
            }
        }

        container.watch(givenChatID).test {
            val initial = awaitItem()
            assertEquals(5L, initial.state.order)

            // Should not emit a new state since the older state is rejected
            expectNoEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle ChatState update with equal order`() = runTest {
        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger,
            subscription = storelessSubscriptionContainer
        )

        val currentState = Chat.State(order = 3L)
        val sameOrderState = Chat.State(order = 3L)

        repo.stub {
            onBlocking {
                subscribeLastChatMessages(
                    Command.ChatCommand.SubscribeLastMessages(
                        chat = givenChatID,
                        limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
                    )
                )
            } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                messages = emptyList(),
                messageCountBefore = 0,
                chatState = currentState
            )
        }

        channel.stub {
            on { observe(chat = givenChatID) } doReturn flow {
                emit(
                    listOf(
                        Event.Command.Chats.UpdateState(
                            context = givenChatID,
                            state = sameOrderState
                        )
                    )
                )
            }
        }

        container.watch(givenChatID).test {
            val initial = awaitItem()
            assertEquals(3L, initial.state.order)

            // Should not emit a new state since equal order is rejected
            expectNoEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should apply first ChatState when no previous state exists`() = runTest {
        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger,
            subscription = storelessSubscriptionContainer
        )

        val firstState = Chat.State(order = 1L)

        repo.stub {
            onBlocking {
                subscribeLastChatMessages(
                    Command.ChatCommand.SubscribeLastMessages(
                        chat = givenChatID,
                        limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
                    )
                )
            } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                messages = emptyList(),
                messageCountBefore = 0,
                chatState = null
            )
        }

        channel.stub {
            on { observe(chat = givenChatID) } doReturn flow {
                emit(
                    listOf(
                        Event.Command.Chats.UpdateState(
                            context = givenChatID,
                            state = firstState
                        )
                    )
                )
            }
        }

        container.watch(givenChatID).test {
            val initial = awaitItem()
            assertEquals(-1L, initial.state.order) // Default empty state

            val updated = awaitItem()
            assertEquals(1L, updated.state.order)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle null ChatState update gracefully`() = runTest {
        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger,
            subscription = storelessSubscriptionContainer
        )

        val initialState = Chat.State(order = 1L)

        repo.stub {
            onBlocking {
                subscribeLastChatMessages(
                    Command.ChatCommand.SubscribeLastMessages(
                        chat = givenChatID,
                        limit = ChatContainer.DEFAULT_CHAT_PAGING_SIZE
                    )
                )
            } doReturn Command.ChatCommand.SubscribeLastMessages.Response(
                messages = emptyList(),
                messageCountBefore = 0,
                chatState = initialState
            )
        }

        channel.stub {
            on { observe(chat = givenChatID) } doReturn flow {
                emit(
                    listOf(
                        Event.Command.Chats.UpdateState(
                            context = givenChatID,
                            state = null
                        )
                    )
                )
            }
        }

        container.watch(givenChatID).test {
            val initial = awaitItem()
            assertEquals(1L, initial.state.order)

            // When null state is received and current state order is 1L, 
            // the new default state (order = -1L) is not applied because -1L < 1L
            // So we should not get a new emission
            expectNoEvents()
        }
    }
}
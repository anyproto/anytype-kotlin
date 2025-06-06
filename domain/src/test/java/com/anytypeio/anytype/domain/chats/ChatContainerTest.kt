package com.anytypeio.anytype.domain.chats

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.TextStyle
import com.anytypeio.anytype.core_models.chats.Chat
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

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
                            order = "A"
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

    @Test()
    fun `should update existing message`() = runTest {

        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger,
            subscription = storelessSubscriptionContainer
        )

        val initialMsg = StubChatMessage(
            content = StubChatMessageContent(
                text = "Hello, Walter"
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
                        Event.Command.Chats.Delete(
                            context = givenChatID,
                            message = initialMsg.id,
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
                expected = emptyList(),
                actual = second.messages
            )
        }
    }

    @Test()
    fun `should delete existing message`() = runTest {

        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger,
            subscription = storelessSubscriptionContainer
        )

        val initialMsg = StubChatMessage(
            content = StubChatMessageContent(
                text = "Hello, "
            )
        )

        val msgAfterUpdate = initialMsg.copy(
            content = initialMsg.content?.copy(
                text = "Hello, Walter"
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
                            message = msgAfterUpdate,
                            id = initialMsg.id,
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
                    msgAfterUpdate
                ),
                actual = second.messages
            )
        }
    }

    @Test()
    fun `should insert new message before existing message according to alphabetic sorting`() = runTest {

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
                            order = newMsg.order
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
    fun `should scroll to bottom when scroll-to-bottom is clicked when subscribing chat`() = runTest {

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
                expected = messages.slice(80..89) + messages.slice(90..99),
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

    // TODO move to test-utils
    fun StubChatMessage(
        id: Id = MockDataFactory.randomUuid(),
        order: Id = MockDataFactory.randomUuid(),
        creator: Id = MockDataFactory.randomUuid(),
        timestamp: Long = MockDataFactory.randomLong(),
        modifiedAt: Long = MockDataFactory.randomLong(),
        reactions: Map<String, List<Id>> = emptyMap(),
        content: Chat.Message.Content? = null

    ): Chat.Message = Chat.Message(
        id = id,
        order = order,
        creator = creator,
        createdAt = timestamp,
        reactions = reactions,
        content = content,
        modifiedAt = modifiedAt
    )

    // TODO move to test-utils
    fun StubChatMessageContent(
        text: String,
        style: TextStyle = TextStyle.P,
        marks: List<Block.Content.Text.Mark> = emptyList()
    ): Chat.Message.Content = Chat.Message.Content(
        text = text,
        style = style,
        marks = marks
    )
}
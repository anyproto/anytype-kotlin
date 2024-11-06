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
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
            logger = logger
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
                        limit = ChatContainer.DEFAULT_LAST_MESSAGE_COUNT
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
                actual = first
            )
            advanceUntilIdle()
            val second = awaitItem()
            assertEquals(
                expected = listOf(
                    msg
                ),
                actual = second
            )
        }
    }

    @Test()
    fun `should update existing message`() = runTest {

        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger
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
                        limit = ChatContainer.DEFAULT_LAST_MESSAGE_COUNT
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
                actual = first
            )
            advanceUntilIdle()
            val second = awaitItem()
            assertEquals(
                expected = emptyList(),
                actual = second
            )
        }
    }

    @Test()
    fun `should delete existing message`() = runTest {

        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger
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
                        limit = ChatContainer.DEFAULT_LAST_MESSAGE_COUNT
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
                actual = first
            )
            advanceUntilIdle()
            val second = awaitItem()
            assertEquals(
                expected = listOf(
                    msgAfterUpdate
                ),
                actual = second
            )
        }
    }

    @Test()
    fun `should insert new message before existing message according to alphabetic sorting`() = runTest {

        val container = ChatContainer(
            repo = repo,
            channel = channel,
            logger = logger
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
                        limit = ChatContainer.DEFAULT_LAST_MESSAGE_COUNT
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
                actual = first
            )
            advanceUntilIdle()
            val second = awaitItem()
            assertEquals(
                expected = listOf(
                    newMsg,
                    initialMsg
                ),
                actual = second
            )
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
package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.domain.auth.interactor.GetCurrentAccount
import com.anytypeio.anytype.domain.auth.model.Account
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.MockDataFactory
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCurrentAccountTest {

    @Mock lateinit var repo: BlockRepository

    @Mock
    lateinit var gateway: Gateway

    lateinit var getCurrentAccount: GetCurrentAccount

    private val config = Config(
        home = MockDataFactory.randomUuid(),
        profile = MockDataFactory.randomUuid(),
        gateway = MockDataFactory.randomString()
    )

    private val builder: UrlBuilder get() = UrlBuilder(gateway)

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)
        getCurrentAccount = GetCurrentAccount(
            repo = repo,
            builder = builder
        )
    }

    @Test
    fun `should return error if payload contains no event`() {

        val context = MockDataFactory.randomUuid()

        stubGetConfig()

        stubOpenProfile(context = context)

        val result = runBlocking {
            getCurrentAccount.invoke(BaseUseCase.None)
        }

        assertTrue { result is Either.Left }
    }

    @Test
    fun `should create account object without avatar from data contained in details`() {

        val context = MockDataFactory.randomUuid()

        val name = MockDataFactory.randomString()

        stubGetConfig()

        stubOpenProfile(
            context = context,
            events = listOf(
                Event.Command.ShowBlock(
                    context = context,
                    details = Block.Details(
                        details = mapOf(
                            config.profile to Block.Fields(
                                mapOf(
                                    "name" to name
                                )
                            )
                        )
                    ),
                    blocks = emptyList(),
                    root = config.profile
                )

            )
        )

        val result = runBlocking {
            getCurrentAccount.invoke(BaseUseCase.None)
        }

        val expected = Account(
            id = config.profile,
            name = name,
            avatar = null,
            color = null
        )

        assertEquals(
            expected = Either.Right(expected),
            actual = result
        )
    }

    @Test
    fun `should create account object with avatar from data contained in details`() {

        val context = MockDataFactory.randomUuid()

        val name = MockDataFactory.randomString()

        val avatar = MockDataFactory.randomString()

        stubGetConfig()

        stubOpenProfile(
            context = context,
            events = listOf(
                Event.Command.ShowBlock(
                    context = context,
                    details = Block.Details(
                        details = mapOf(
                            config.profile to Block.Fields(
                                mapOf(
                                    "name" to name,
                                    "iconImage" to avatar
                                )
                            )
                        )
                    ),
                    blocks = emptyList(),
                    root = config.profile
                )

            )
        )

        val result = runBlocking {
            getCurrentAccount.invoke(BaseUseCase.None)
        }

        val expected = Account(
            id = config.profile,
            name = name,
            avatar = builder.image(avatar),
            color = null
        )

        assertEquals(
            expected = Either.Right(expected),
            actual = result
        )
    }

    private fun stubOpenProfile(
        context: String,
        events: List<Event> = emptyList()
    ) {
        repo.stub {
            onBlocking { openProfile(config.profile) } doReturn Payload(
                context = context,
                events = events
            )
        }
    }

    private fun stubGetConfig() {
        repo.stub {
            onBlocking { getConfig() } doReturn config
        }
    }
}
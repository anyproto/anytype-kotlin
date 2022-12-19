package com.anytypeio.anytype.domain.dashboard

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.auth.interactor.GetProfile
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals

class GetProfileTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var channel: SubscriptionEventChannel

    @Mock
    lateinit var configStorage: ConfigStorage

    private lateinit var usecase: GetProfile

    private val config = StubConfig()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        usecase = GetProfile(
            repo = repo,
            channel = channel,
            provider = configStorage
        )
    }

    @Test
    fun `should emit initial data with profile and complete`() = runTest {

        val subscription = MockDataFactory.randomUuid()

        val profile = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to config.profile,
                Relations.NAME to "Friedrich Kittler"
            )
        )

        configStorage.stub {
            onBlocking { get() } doReturn config
        }

        channel.stub {
            on { subscribe(listOf(subscription)) } doReturn flowOf()
        }

        repo.stub {
            onBlocking {
                searchObjectsByIdWithSubscription(
                    subscription = subscription,
                    keys = emptyList(),
                    ids = listOf(config.profile)
                )
            } doReturn SearchResult(
                results = listOf(profile),
                dependencies = emptyList()
            )
        }

        usecase.observe(
            subscription = subscription,
            keys = emptyList(),
            dispatcher = rule.testDispatcher
        ).test {
            assertEquals(
                expected = profile.map,
                actual = awaitItem().map
            )
            awaitComplete()
        }
    }

    @Test
    fun `should emit transformated profile object, then complete`() = runTest {

        val subscription = MockDataFactory.randomUuid()

        val nameBeforeUpdate = "Friedrich"
        val nameAfterUpdate = "Friedrich Kittler"

        val profileObjectBeforeUpdate = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to config.profile,
                Relations.NAME to nameBeforeUpdate
            )
        )

        val profileObjectAfterUpdate = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to config.profile,
                Relations.NAME to nameAfterUpdate
            )
        )

        configStorage.stub {
            onBlocking { get() } doReturn config
        }

        channel.stub {
            on { subscribe(listOf(subscription)) } doReturn flow {
                emit(
                    listOf(
                        SubscriptionEvent.Amend(
                            diff = mapOf(
                                Relations.NAME to nameAfterUpdate
                            ),
                            target = config.profile,
                            subscriptions = listOf(subscription)
                        )
                    )
                )
            }
        }

        repo.stub {
            onBlocking {
                searchObjectsByIdWithSubscription(
                    subscription = subscription,
                    keys = emptyList(),
                    ids = listOf(config.profile)
                )
            } doReturn SearchResult(
                results = listOf(profileObjectBeforeUpdate),
                dependencies = emptyList()
            )
        }

        usecase.observe(
            subscription = subscription,
            keys = emptyList(),
            dispatcher = rule.testDispatcher
        ).test {
            assertEquals(
                expected = profileObjectBeforeUpdate.map,
                actual = awaitItem().map
            )
            assertEquals(
                expected = profileObjectAfterUpdate.map,
                actual = awaitItem().map
            )
            awaitComplete()
        }
    }

    @Test
    fun `should apply several transformations, then complete`() = runTest {

        val subscription = MockDataFactory.randomUuid()

        val nameBeforeUpdate = "Friedrich"
        val nameAfterUpdate = "Friedrich Kittler"

        val iconImageBeforeUpdate = null
        val iconImageAfterUpdate = MockDataFactory.randomUuid()

        val profileObjectBeforeUpdate = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to config.profile,
                Relations.NAME to nameBeforeUpdate,
                Relations.ICON_IMAGE to iconImageBeforeUpdate
            )
        )

        configStorage.stub {
            onBlocking { get() } doReturn config
        }

        channel.stub {
            on { subscribe(listOf(subscription)) } doReturn flow {
                emit(
                    listOf(
                        SubscriptionEvent.Amend(
                            diff = mapOf(
                                Relations.NAME to nameAfterUpdate
                            ),
                            target = config.profile,
                            subscriptions = listOf(subscription)
                        ),
                        SubscriptionEvent.Amend(
                            diff = mapOf(
                                Relations.ICON_IMAGE to iconImageAfterUpdate
                            ),
                            target = config.profile,
                            subscriptions = listOf(subscription)
                        )
                    )
                )
            }
        }

        repo.stub {
            onBlocking {
                searchObjectsByIdWithSubscription(
                    subscription = subscription,
                    keys = emptyList(),
                    ids = listOf(config.profile)
                )
            } doReturn SearchResult(
                results = listOf(profileObjectBeforeUpdate),
                dependencies = emptyList()
            )
        }

        usecase.observe(
            subscription = subscription,
            keys = emptyList(),
            dispatcher = rule.testDispatcher
        ).test {
            assertEquals(
                expected = profileObjectBeforeUpdate.map,
                actual = awaitItem().map
            )
            assertEquals(
                expected = mapOf(
                    Relations.ID to config.profile,
                    Relations.NAME to nameAfterUpdate,
                    Relations.ICON_IMAGE to iconImageAfterUpdate
                ),
                actual = awaitItem().map
            )
            awaitComplete()
        }
    }

    @Test
    fun `should apply all transformations, then complete`() = runTest {

        val subscription = MockDataFactory.randomUuid()

        val nameBeforeUpdate = "Friedrich"
        val nameAfterUpdate = "Friedrich Kittler"

        val iconImageBeforeUpdate = null
        val iconImageAfterUpdate = MockDataFactory.randomUuid()

        val profileObjectBeforeUpdate = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to config.profile,
                Relations.NAME to nameBeforeUpdate,
                Relations.ICON_IMAGE to iconImageBeforeUpdate
            )
        )

        configStorage.stub {
            onBlocking { get() } doReturn config
        }

        channel.stub {
            on { subscribe(listOf(subscription)) } doReturn flow {
                emit(
                    listOf(
                        SubscriptionEvent.Amend(
                            diff = mapOf(
                                Relations.NAME to nameAfterUpdate
                            ),
                            target = config.profile,
                            subscriptions = listOf(subscription)
                        )
                    )
                )
                emit(
                    listOf(
                        SubscriptionEvent.Amend(
                            diff = mapOf(
                                Relations.ICON_IMAGE to iconImageAfterUpdate
                            ),
                            target = config.profile,
                            subscriptions = listOf(subscription)
                        )
                    )
                )
            }
        }

        repo.stub {
            onBlocking {
                searchObjectsByIdWithSubscription(
                    subscription = subscription,
                    keys = emptyList(),
                    ids = listOf(config.profile)
                )
            } doReturn SearchResult(
                results = listOf(profileObjectBeforeUpdate),
                dependencies = emptyList()
            )
        }

        usecase.observe(
            subscription = subscription,
            keys = emptyList(),
            dispatcher = rule.testDispatcher
        ).test {
            assertEquals(
                expected = profileObjectBeforeUpdate.map,
                actual = awaitItem().map
            )
            assertEquals(
                expected = mapOf(
                    Relations.ID to config.profile,
                    Relations.NAME to nameAfterUpdate,
                    Relations.ICON_IMAGE to iconImageBeforeUpdate
                ),
                actual = awaitItem().map
            )
            assertEquals(
                expected = mapOf(
                    Relations.ID to config.profile,
                    Relations.NAME to nameAfterUpdate,
                    Relations.ICON_IMAGE to iconImageAfterUpdate
                ),
                actual = awaitItem().map
            )
            awaitComplete()
        }
    }
}
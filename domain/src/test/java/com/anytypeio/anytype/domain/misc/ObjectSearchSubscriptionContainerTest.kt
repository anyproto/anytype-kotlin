package com.anytypeio.anytype.domain.misc

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class ObjectSearchSubscriptionContainerTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    private lateinit var container: ObjectSearchSubscriptionContainer

    private val defaultLimit = 0
    private val defaultOffset = 0

    private val defaultKeys = listOf(
        Relations.ID,
        Relations.NAME,
        Relations.TYPE,
        Relations.LAYOUT
    )

    @Mock
    lateinit var channel: SubscriptionEventChannel

    lateinit var store: DefaultObjectStore

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        store = DefaultObjectStore()
        setupContainer()
    }

    @Test
    fun `should initialize then consume all subscriptions events for two objects`() {

        val subscription1 = "sub1"

        val keys1 = defaultKeys + listOf(Relations.DESCRIPTION)

        val initialResults1 = listOf(
            ObjectWrapper.Basic(
                mapOf(
                    Relations.ID to "obj1",
                    Relations.NAME to "Jonathan Littel",
                    Relations.TYPE to ObjectType.PROFILE_URL,
                    Relations.LAYOUT to ObjectType.Layout.PROFILE.code,
                    Relations.DESCRIPTION to null
                )
            )
        )

        val dependentResults1 = listOf(
            ObjectWrapper.Basic(
                mapOf(
                    Relations.ID to "obj2",
                    Relations.NAME to "Writer",
                    Relations.TYPE to ObjectType.PROFILE_URL,
                    Relations.LAYOUT to ObjectType.Layout.PROFILE.code
                )
            )
        )

        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    subscription = subscription1,
                    limit = defaultLimit,
                    offset = defaultOffset,
                    filters = emptyList(),
                    sorts = emptyList(),
                    keys = keys1,
                    afterId = null,
                    beforeId = null
                )
            } doReturn SearchResult(
                results = initialResults1,
                dependencies = dependentResults1
            )
        }

        channel.stub {
            on {
                subscribe(listOf(subscription1))
            } doReturn flow {
                emit(
                    listOf(
                        SubscriptionEvent.Amend(
                            target = "obj4",
                            subscriptions = listOf(
                                subscription1
                            ),
                            diff = mapOf(
                                Relations.ID to "obj4"
                            )
                        ),
                        SubscriptionEvent.Add(
                            target = "obj4",
                            subscription = subscription1,
                            afterId = "obj1"
                        )
                    )
                )
                emit(
                    listOf(
                        SubscriptionEvent.Position(
                            target = "obj4",
                            afterId = null
                        )
                    )
                )
            }
        }

        runTest {
            container.observe(
                subscription1,
                limit = defaultLimit,
                offset = defaultOffset,
                keys = keys1
            ).test {

                // checking subscription

                assertEquals(
                    expected = Subscription(
                        listOf("obj1")
                    ),
                    actual = awaitItem()
                )

                assertEquals(
                    expected = Subscription(
                        listOf("obj1", "obj4")
                    ),
                    actual = awaitItem()
                )

                assertEquals(
                    expected = Subscription(
                        listOf("obj4", "obj1")
                    ),
                    actual = awaitItem()
                )

                awaitComplete()

                // checking store

                assertEquals(
                    actual = store.get("obj1")?.map,
                    expected = initialResults1.firstOrNull()?.map
                )
                assertEquals(
                    actual = store.get("obj2")?.map,
                    expected = dependentResults1.firstOrNull()?.map
                )
                assertEquals(
                    actual = store.get("obj3"),
                    expected = null
                )
                assertEquals(
                    actual = store.get("obj4")?.map,
                    expected = mapOf(Relations.ID to "obj4"),
                )
            }
        }
    }

    private fun setupContainer() {
        container = ObjectSearchSubscriptionContainer(
            repo = repo,
            channel = channel,
            store = store,
            dispatchers = AppCoroutineDispatchers(
                io = rule.testDispatcher,
                main = rule.testDispatcher,
                computation = rule.testDispatcher
            )
        )
    }
}
package com.anytypeio.anytype.domain.misc

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataViewSubscriptionContainerTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    private lateinit var container: DataViewSubscriptionContainer

    private val defaultLimit = 0
    private val defaultOffset = 0L

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
                    Relations.TYPE to ObjectTypeIds.PROFILE,
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
                    Relations.TYPE to ObjectTypeIds.PROFILE,
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
                    beforeId = null,
                    source = emptyList(),
                    noDepSubscription = null,
                    ignoreWorkspace = null
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

        val params = DataViewSubscriptionContainer.Params(
            subscription = subscription1,
            limit = defaultLimit,
            offset = defaultOffset,
            keys = keys1,
            filters = emptyList(),
            sorts = emptyList(),
            sources = emptyList()
        )

        runTest {
            container.observe(params).test {

                // checking subscription

                val firstEmission = awaitItem()

                assertTrue {
                    with(firstEmission) {
                        objects == listOf("obj1") && dependencies == listOf("obj2")
                    }
                }

                val secondEmission = awaitItem()

                assertTrue {
                    with(secondEmission) {
                        objects == listOf("obj1", "obj4") && dependencies ==  listOf("obj2")
                    }
                }

                val thirdEmission = awaitItem()

                assertTrue {
                    with(thirdEmission) {
                        objects == listOf("obj4", "obj1") && dependencies ==  listOf("obj2")
                    }
                }

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
        container = DataViewSubscriptionContainer(
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
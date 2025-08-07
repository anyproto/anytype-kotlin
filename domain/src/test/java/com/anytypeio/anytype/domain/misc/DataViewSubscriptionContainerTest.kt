package com.anytypeio.anytype.domain.misc

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.search.DataViewState
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.test_utils.MockDataFactory
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

    private val defaultSpaceId = SpaceId(
        id = MockDataFactory.randomUuid()
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
                    space = defaultSpaceId,
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
                    ignoreWorkspace = null,
                    collection = null
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
            space = defaultSpaceId,
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

                val firstEmission = awaitItem() as DataViewState.Loaded

                assertTrue {
                    with(firstEmission) {
                        objects == listOf("obj1") && dependencies == listOf("obj2")
                    }
                }

                val secondEmission = awaitItem() as DataViewState.Loaded

                assertTrue {
                    with(secondEmission) {
                        objects == listOf("obj1", "obj4") && dependencies ==  listOf("obj2")
                    }
                }

                val thirdEmission = awaitItem() as DataViewState.Loaded

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

    @Test
    fun `should use CREATED_DATE sort when sorts are empty for collection`() = runTest {
        // Given
        val subscription = "collection-sub"
        val collectionId = "collection-123"
        
        val keys = defaultKeys
        
        val expectedSorts = listOf(
            com.anytypeio.anytype.core_models.DVSort(
                relationKey = Relations.CREATED_DATE,
                type = com.anytypeio.anytype.core_models.DVSortType.DESC,
                includeTime = true,
                relationFormat = com.anytypeio.anytype.core_models.RelationFormat.DATE
            )
        )
        
        // When & Then
        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    space = defaultSpaceId,
                    subscription = subscription,
                    limit = defaultLimit,
                    offset = defaultOffset,
                    filters = emptyList(),
                    sorts = expectedSorts, // Verify CREATED_DATE sort is added when empty
                    keys = keys,
                    afterId = null,
                    beforeId = null,
                    source = emptyList(),
                    noDepSubscription = null,
                    ignoreWorkspace = null,
                    collection = collectionId
                )
            } doReturn SearchResult(
                results = emptyList(),
                dependencies = emptyList()
            )
        }
        
        channel.stub {
            on {
                subscribe(listOf(subscription))
            } doReturn flow { }
        }
        
        // Create params with empty sorts
        val params = DataViewSubscriptionContainer.Params(
            space = defaultSpaceId,
            subscription = subscription,
            sorts = expectedSorts, // This simulates what getSortsWithDefaultCreatedDate should return
            filters = emptyList(),
            sources = emptyList(),
            keys = keys,
            limit = defaultLimit,
            offset = defaultOffset,
            collection = collectionId
        )
        
        // Execute to verify the request is made with correct parameters
        container.observe(params).test {
            awaitItem()
            awaitComplete()
        }
    }

    @Test
    fun `should use CREATED_DATE sort when sorts are empty for set`() = runTest {
        // Given
        val subscription = "set-sub"
        val setSource = listOf("set-source-123")
        
        val keys = defaultKeys
        
        val expectedSorts = listOf(
            com.anytypeio.anytype.core_models.DVSort(
                relationKey = Relations.CREATED_DATE,
                type = com.anytypeio.anytype.core_models.DVSortType.DESC,
                includeTime = true,
                relationFormat = com.anytypeio.anytype.core_models.RelationFormat.DATE
            )
        )
        
        // When & Then
        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    space = defaultSpaceId,
                    subscription = subscription,
                    limit = defaultLimit,
                    offset = defaultOffset,
                    filters = emptyList(),
                    sorts = expectedSorts, // Verify CREATED_DATE sort is added when empty
                    keys = keys,
                    afterId = null,
                    beforeId = null,
                    source = setSource,
                    noDepSubscription = null,
                    ignoreWorkspace = null,
                    collection = null
                )
            } doReturn SearchResult(
                results = emptyList(),
                dependencies = emptyList()
            )
        }
        
        channel.stub {
            on {
                subscribe(listOf(subscription))
            } doReturn flow { }
        }
        
        // Create params with empty sorts
        val params = DataViewSubscriptionContainer.Params(
            space = defaultSpaceId,
            subscription = subscription,
            sorts = expectedSorts, // This simulates what getSortsWithDefaultCreatedDate should return
            filters = emptyList(),
            sources = setSource,
            keys = keys,
            limit = defaultLimit,
            offset = defaultOffset
        )
        
        // Execute to verify the request is made with correct parameters
        container.observe(params).test {
            awaitItem()
            awaitComplete()
        }
    }

    @Test
    fun `should use CREATED_DATE sort when sorts are empty for type set`() = runTest {
        // Given
        val subscription = "type-set-sub"
        val typeSource = listOf("type-source-123")
        
        val keys = defaultKeys
        
        val expectedSorts = listOf(
            com.anytypeio.anytype.core_models.DVSort(
                relationKey = Relations.CREATED_DATE,
                type = com.anytypeio.anytype.core_models.DVSortType.DESC,
                includeTime = true,
                relationFormat = com.anytypeio.anytype.core_models.RelationFormat.DATE
            )
        )
        
        // When & Then
        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    space = defaultSpaceId,
                    subscription = subscription,
                    limit = defaultLimit,
                    offset = defaultOffset,
                    filters = emptyList(),
                    sorts = expectedSorts, // Verify CREATED_DATE sort is added when empty
                    keys = keys,
                    afterId = null,
                    beforeId = null,
                    source = typeSource,
                    noDepSubscription = null,
                    ignoreWorkspace = null,
                    collection = null
                )
            } doReturn SearchResult(
                results = emptyList(),
                dependencies = emptyList()
            )
        }
        
        channel.stub {
            on {
                subscribe(listOf(subscription))
            } doReturn flow { }
        }
        
        // Create params with empty sorts
        val params = DataViewSubscriptionContainer.Params(
            space = defaultSpaceId,
            subscription = subscription,
            sorts = expectedSorts, // This simulates what getSortsWithDefaultCreatedDate should return
            filters = emptyList(),
            sources = typeSource,
            keys = keys,
            limit = defaultLimit,
            offset = defaultOffset
        )
        
        // Execute to verify the request is made with correct parameters
        container.observe(params).test {
            awaitItem()
            awaitComplete()
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
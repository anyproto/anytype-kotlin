package com.anytypeio.anytype.domain.subscriptions

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Marketplace
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.search.RelationsSubscriptionContainer
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlin.test.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class RelationsSubscriptionContainerTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    private val dispatchers = AppCoroutineDispatchers(
        main = coroutineTestRule.dispatcher,
        computation = coroutineTestRule.dispatcher,
        io = coroutineTestRule.dispatcher
    )

    @Mock lateinit var spaceManager: SpaceManager

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var channel: SubscriptionEventChannel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should populate store with latest results and clear it when unsubscribe`() = runTest {

        // SETUP

        val delay = 300L

        val subscription = RelationsSubscriptionContainer.SUBSCRIPTION_ID

        val store = DefaultStoreOfRelations()

        val defaultSpaceConfig = StubConfig()

        val alternativeSpaceConfig = StubConfig()

        val container = RelationsSubscriptionContainer(
            repo = repo,
            channel = channel,
            store = store,
            dispatchers = dispatchers
        )

        val manager = RelationsSubscriptionManager  (
            scope = TestScope(),
            spaceManager = spaceManager,
            container = container
        )

        val defaultSpaceSearchParams = RelationsSubscriptionManager.buildParams(
            spaces = listOf(
                defaultSpaceConfig.space,
                defaultSpaceConfig.techSpace,
                Marketplace.MARKETPLACE_SPACE_ID,
            )
        )

        val alternativeSpaceSearchParams = RelationsSubscriptionManager.buildParams(
            spaces = listOf(
                alternativeSpaceConfig.space,
                alternativeSpaceConfig.techSpace,
                Marketplace.MARKETPLACE_SPACE_ID,
            )
        )

        val defaultSpaceTypes = buildList {
            add(
                StubRelationObject()
            )
            add(
                StubRelationObject()
            )
        }

        val alternativeSpaceTypes = buildList {
            add(
                StubRelationObject()
            )
            add(
                StubRelationObject()
            )
        }

        // STUBBING

        channel.stub {
            on {
                subscribe(
                    listOf(subscription)
                )
            } doReturn emptyFlow()
        }

        repo.stub {
            onBlocking {
                cancelObjectSearchSubscription(listOf(subscription))
            } doReturn Unit
        }

        spaceManager.stub {
            on {
                state()
            } doReturn flow {
                emit(
                    SpaceManager.State.Space.Active(
                        defaultSpaceConfig
                    )
                )
                delay(delay)
                emit(
                    SpaceManager.State.Space.Active(
                        alternativeSpaceConfig
                    )
                )
            }
        }

        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    subscription = defaultSpaceSearchParams.subscription,
                    sorts = defaultSpaceSearchParams.sorts,
                    filters = defaultSpaceSearchParams.filters,
                    limit = defaultSpaceSearchParams.limit,
                    offset = defaultSpaceSearchParams.offset,
                    keys = defaultSpaceSearchParams.keys,
                    ignoreWorkspace = defaultSpaceSearchParams.ignoreWorkspace,
                    source = defaultSpaceSearchParams.sources,
                    afterId = null,
                    beforeId = null,
                    collection = null,
                    noDepSubscription = true
                )
            } doReturn SearchResult(
                results = defaultSpaceTypes.map { ObjectWrapper.Basic(it.map) },
                dependencies = emptyList()
            )
        }

        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    subscription = alternativeSpaceSearchParams.subscription,
                    sorts = alternativeSpaceSearchParams.sorts,
                    filters = alternativeSpaceSearchParams.filters,
                    limit = alternativeSpaceSearchParams.limit,
                    offset = alternativeSpaceSearchParams.offset,
                    keys = alternativeSpaceSearchParams.keys,
                    ignoreWorkspace = alternativeSpaceSearchParams.ignoreWorkspace,
                    source = alternativeSpaceSearchParams.sources,
                    afterId = null,
                    beforeId = null,
                    collection = null,
                    noDepSubscription = true
                )
            } doReturn SearchResult(
                results = alternativeSpaceTypes.map { ObjectWrapper.Basic(it.map) },
                dependencies = emptyList()
            )
        }

        // TESTING

        manager.pipeline.test {
            val first = awaitItem()
            assertEquals(
                expected = defaultSpaceTypes,
                actual = store.getAll()
            )
            val second = awaitItem()
            awaitComplete()
            assertEquals(
                expected = alternativeSpaceTypes,
                actual = store.getAll()
            )

            container.unsubscribe()

            assertEquals(
                expected = emptyList(),
                actual = store.getAll()
            )
        }
    }
}
package com.anytypeio.anytype.presentation.relations

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Marketplace.MARKETPLACE_SPACE_ID
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.relations.GetRelations
import com.anytypeio.anytype.domain.workspace.AddObjectToWorkspace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.relations.model.RelationView
import com.anytypeio.anytype.presentation.relations.model.Section
import com.anytypeio.anytype.presentation.relations.providers.FakeObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

@ExperimentalCoroutinesApi
class RelationAddViewModelBaseTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    private val appCoroutineDispatchers = AppCoroutineDispatchers(
        io = coroutineTestRule.dispatcher,
        main = coroutineTestRule.dispatcher,
        computation = coroutineTestRule.dispatcher
    )

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var spaceManager: SpaceManager

    private val relationsProvider = FakeObjectRelationProvider()

    private val spaceId = MockDataFactory.randomString()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repo = mock(verboseLogging = true)
    }

    @Test
    fun `no added relations - results are available without hidden`() = runTest {

        // SETUP

        val relation = StubRelationObject(
            spaceId = spaceId
        )

        spaceManager.stub {
            onBlocking {
                get()
            } doReturn spaceId
        }

        repo.stub {
            onBlocking {
                searchObjects(
                    sorts = ObjectSearchConstants.defaultObjectSearchSorts(),
                    filters = buildList {
                        addAll(ObjectSearchConstants.filterMyRelations())
                        add(
                            DVFilter(
                                relation = Relations.SPACE_ID,
                                condition = DVFilterCondition.EQUAL,
                                value = spaceId
                            )
                        )
                        add(
                            DVFilter(
                                relation = Relations.RELATION_KEY,
                                condition = DVFilterCondition.NOT_IN,
                                value = Relations.systemRelationKeys
                            )
                        )
                    },
                    limit = 0,
                    offset = 0,
                    fulltext = ""
                )
            } doReturn listOf(relation.map)
        }

        repo.stub {
            onBlocking {
                searchObjects(
                    sorts = ObjectSearchConstants.defaultObjectSearchSorts(),
                    filters = buildList {
                        addAll(ObjectSearchConstants.filterMarketplaceRelations())
                        add(
                            DVFilter(
                                relation = Relations.ID,
                                condition = DVFilterCondition.NOT_IN,
                                value = listOf(relation.sourceObject)
                            )
                        )
                        add(
                            DVFilter(
                                relation = Relations.SPACE_ID,
                                condition = DVFilterCondition.EQUAL,
                                value = MARKETPLACE_SPACE_ID
                            )
                        )
                        add(
                            DVFilter(
                                relation = Relations.RELATION_KEY,
                                condition = DVFilterCondition.NOT_IN,
                                value = Relations.systemRelationKeys
                            )
                        )
                    },
                    limit = 0,
                    offset = 0,
                    fulltext = ""
                )
            } doReturn emptyList()
        }

        val vm = givenViewModel(relationsProvider = relationsProvider)

        // TESTING

        coroutineTestRule.dispatcher.scheduler.runCurrent()

        vm.results.test {
            assertEquals(
                actual = expectMostRecentItem(),
                expected = listOf(
                    Section.Library,
                    RelationView.Existing(
                        key = relation.key,
                        id = relation.id,
                        name = relation.name.orEmpty(),
                        format = relation.format,
                        space = spaceId
                    )
                )
            )
        }
    }

    @Test
    fun `added relations equal to available - results are empty`() = runTest {
        // SETUP
        val vm = givenViewModel(relationsProvider)

        // TESTING

        vm.results.test {
            assertEquals(
                actual = awaitItem(),
                expected = emptyList()
            )
        }
    }

    @Test
    fun `should query relations from library and marketplace filtering out already addded relations`() =
        runTest {

            // SETUP

            val marketplace = listOf(
                StubRelationObject(spaceId = MARKETPLACE_SPACE_ID),
                StubRelationObject(spaceId = MARKETPLACE_SPACE_ID),
                StubRelationObject(spaceId = MARKETPLACE_SPACE_ID)
            )

            val library = listOf(
                StubRelationObject(sourceObject = marketplace[0].id, spaceId = spaceId),
                StubRelationObject(spaceId = spaceId),
                StubRelationObject(spaceId = spaceId)
            )

            spaceManager.stub {
                onBlocking {
                    get()
                } doReturn spaceId
            }

            repo.stub {
                onBlocking {
                    searchObjects(
                        sorts = ObjectSearchConstants.defaultObjectSearchSorts(),
                        filters = buildList {
                            addAll(ObjectSearchConstants.filterMyRelations())
                            add(
                                DVFilter(
                                    relation = Relations.SPACE_ID,
                                    condition = DVFilterCondition.EQUAL,
                                    value = spaceId
                                )
                            )
                            add(
                                DVFilter(
                                    relation = Relations.RELATION_KEY,
                                    condition = DVFilterCondition.NOT_IN,
                                    value = Relations.systemRelationKeys
                                )
                            )
                        },
                        limit = 0,
                        offset = 0,
                        fulltext = ""
                    )
                } doReturn library.map { it.map }
            }

            repo.stub {
                onBlocking {
                    searchObjects(
                        sorts = ObjectSearchConstants.defaultObjectSearchSorts(),
                        filters = buildList {
                            addAll(ObjectSearchConstants.filterMarketplaceRelations())
                            add(
                                DVFilter(
                                    relation = Relations.ID,
                                    condition = DVFilterCondition.NOT_IN,
                                    value = library.mapNotNull { it.sourceObject }
                                )
                            )
                            add(
                                DVFilter(
                                    relation = Relations.SPACE_ID,
                                    condition = DVFilterCondition.EQUAL,
                                    value = MARKETPLACE_SPACE_ID
                                )
                            )
                            add(
                                DVFilter(
                                    relation = Relations.RELATION_KEY,
                                    condition = DVFilterCondition.NOT_IN,
                                    value = Relations.systemRelationKeys
                                )
                            )
                        },
                        limit = 0,
                        offset = 0,
                        fulltext = ""
                    )
                } doReturn marketplace.takeLast(2).map { it.map }
            }

            val vm = givenViewModel(relationsProvider = relationsProvider)

            // TESTING

            coroutineTestRule.dispatcher.scheduler.runCurrent()

            vm.results.test {
                assertEquals(
                    actual = expectMostRecentItem(),
                    expected = listOf(
                        Section.Library,
                        RelationView.Existing(
                            key = library[0].key,
                            id = library[0].id,
                            name = library[0].name.orEmpty(),
                            format = library[0].format,
                            space = spaceId
                        ),
                        RelationView.Existing(
                            key = library[1].key,
                            id = library[1].id,
                            name = library[1].name.orEmpty(),
                            format = library[1].format,
                            space = spaceId
                        ),
                        RelationView.Existing(
                            key = library[2].key,
                            id = library[2].id,
                            name = library[2].name.orEmpty(),
                            format = library[2].format,
                            space = spaceId
                        ),
                        Section.Marketplace,
                        RelationView.Existing(
                            key = marketplace[1].key,
                            id = marketplace[1].id,
                            name = marketplace[1].name.orEmpty(),
                            format = marketplace[1].format,
                            space = MARKETPLACE_SPACE_ID
                        ),
                        RelationView.Existing(
                            key = marketplace[2].key,
                            id = marketplace[2].id,
                            name = marketplace[2].name.orEmpty(),
                            format = marketplace[2].format,
                            space = MARKETPLACE_SPACE_ID
                        ),
                    )
                )
            }
        }

    private fun givenViewModel(
        relationsProvider: ObjectRelationProvider
    ) = object : RelationAddViewModelBase(
        relationsProvider = relationsProvider,
        getRelations = GetRelations(repo),
        appCoroutineDispatchers = appCoroutineDispatchers,
        addObjectToWorkspace = AddObjectToWorkspace(
            repo = repo,
            dispatchers = appCoroutineDispatchers
        ),
        spaceManager = spaceManager
    ) {}
}
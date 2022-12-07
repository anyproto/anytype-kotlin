package com.anytypeio.anytype.presentation.types

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubObjectType
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.workspace.AddObjectToWorkspace
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModel
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class ObjectTypeChangeViewModelTest {

    @Mock
    lateinit var repo: BlockRepository

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private lateinit var getObjectTypes: GetObjectTypes
    private lateinit var addObjectToWorkspace: AddObjectToWorkspace

    private val dispatchers = AppCoroutineDispatchers(
        io = coroutineTestRule.testDispatcher,
        main = coroutineTestRule.testDispatcher,
        computation = coroutineTestRule.testDispatcher
    )

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        getObjectTypes = GetObjectTypes(repo)
        addObjectToWorkspace = AddObjectToWorkspace(
            repo = repo,
            dispatchers = dispatchers
        )
    }

    @Test
    fun `should emit empty view list when view model is initialized`() = runTest {

        // SETUP

        val vm = givenViewModel()

        // TESTING

        vm.views.test {
            assertEquals(
                expected = emptyList(),
                actual = awaitItem()
            )
        }
    }

    @Test
    fun `should start query for user types as soon as setup is provided`() = runTest {

        // SETUP

        val vm = givenViewModel()

        val expectedMyTypesFilters = ObjectSearchConstants.filterObjectType

        // TESTING

        verifyNoInteractions(repo)

        vm.onStart(
            isWithBookmark = false,
            isWithSet = false,
            isSetSource = false,
            excludeTypes = emptyList(),
            selectedTypes = emptyList()
        )

        verifyBlocking(repo, times(1)) {
            searchObjects(
                filters = expectedMyTypesFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                limit = 0,
                offset = 0,
                keys = ObjectSearchConstants.defaultKeysObjectType,
                fulltext = ""
            )
        }
    }

    @Test
    fun `should query marketplace object types excluding already installed user types by source-object relation`() = runTest {

        // SETUP

        val marketplaceType1 = StubObjectType()
        val marketplaceType2 = StubObjectType()
        val installedType1 = StubObjectType(sourceObject = marketplaceType1.id)
        val installedType2 = StubObjectType(sourceObject = marketplaceType2.id)

        val vm = givenViewModel()

        val expectedMyTypesFilters = ObjectSearchConstants.filterObjectType
        val expectedMyTypeKeys = ObjectSearchConstants.defaultKeysObjectType

        val expectedMarketplaceTypeKeys = ObjectSearchConstants.defaultKeysObjectType
        val expectedMarketplaceTypeFilters = buildList {
            addAll(ObjectSearchConstants.filterObjectTypeMarketplace)
            add(
                DVFilter(
                    relationKey = Relations.ID,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(
                        marketplaceType1.id,
                        marketplaceType2.id,
                        MarketplaceObjectTypeIds.BOOKMARK
                    )
                )
            )
        }

        repo.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMyTypesFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = "",
                    keys = expectedMyTypeKeys
                )
            } doReturn listOf(
                installedType1.map, installedType2.map
            )
        }

        // TESTING

        verifyNoInteractions(repo)

        vm.onStart(
            isWithBookmark = false,
            isWithSet = false,
            isSetSource = false,
            excludeTypes = emptyList(),
            selectedTypes = emptyList()
        )

        // Checking search query for my types

        verifyBlocking(repo, times(1)) {
            searchObjects(
                filters = expectedMyTypesFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                limit = 0,
                offset = 0,
                keys = expectedMyTypeKeys,
                fulltext = ""
            )
        }

        // Checking search query for marketplace types

        verifyBlocking(repo, times(1)) {
            searchObjects(
                filters = expectedMarketplaceTypeFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                limit = 0,
                offset = 0,
                keys = expectedMarketplaceTypeKeys,
                fulltext = ""
            )
        }
    }

    @Test
    fun `should query marketplace object types when user input is changed`() = runTest {

        // SETUP

        val marketplaceType1 = StubObjectType()
        val marketplaceType2 = StubObjectType()
        val installedType1 = StubObjectType(sourceObject = marketplaceType1.id)
        val installedType2 = StubObjectType(sourceObject = marketplaceType2.id)

        val vm = givenViewModel()

        val expectedMyTypesFilters = ObjectSearchConstants.filterObjectType
        val expectedMyTypeKeys = ObjectSearchConstants.defaultKeysObjectType

        val expectedMarketplaceTypeKeys = ObjectSearchConstants.defaultKeysObjectType
        val expectedMarketplaceTypeFilters = buildList {
            addAll(ObjectSearchConstants.filterObjectTypeMarketplace)
            add(
                DVFilter(
                    relationKey = Relations.ID,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(
                        marketplaceType1.id,
                        marketplaceType2.id,
                        MarketplaceObjectTypeIds.BOOKMARK
                    )
                )
            )
        }

        val query = MockDataFactory.randomString()

        repo.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMyTypesFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = "",
                    keys = expectedMyTypeKeys
                )
            } doReturn listOf(
                installedType1.map, installedType2.map
            )
        }

        repo.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMarketplaceTypeFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = "",
                    keys = expectedMarketplaceTypeKeys
                )
            } doReturn emptyList()
        }

        repo.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMyTypesFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = query,
                    keys = expectedMyTypeKeys
                )
            } doReturn listOf(
                installedType1.map, installedType2.map
            )
        }

        repo.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMarketplaceTypeFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = query,
                    keys = expectedMarketplaceTypeKeys
                )
            } doReturn emptyList()
        }

        // TESTING

        verifyNoInteractions(repo)

        vm.onStart(
            isWithBookmark = false,
            isWithSet = false,
            isSetSource = false,
            excludeTypes = emptyList(),
            selectedTypes = emptyList()
        )

        // Checking search query for my types

        verifyBlocking(repo, times(1)) {
            searchObjects(
                filters = expectedMyTypesFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                limit = 0,
                offset = 0,
                keys = expectedMyTypeKeys,
                fulltext = ""
            )
        }

        // Checking search query for marketplace types

        verifyBlocking(repo, times(1)) {
            searchObjects(
                filters = expectedMarketplaceTypeFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                limit = 0,
                offset = 0,
                keys = expectedMarketplaceTypeKeys,
                fulltext = ""
            )
        }

        testScheduler.advanceUntilIdle()

        vm.onQueryChanged(query)

        testScheduler.advanceUntilIdle()

        // Checking search query for my types

        verifyBlocking(repo, times(1)) {
            searchObjects(
                filters = expectedMyTypesFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                limit = 0,
                offset = 0,
                keys = expectedMyTypeKeys,
                fulltext = query
            )
        }

        // Checking search query for marketplace types

        verifyBlocking(repo, times(1)) {
            searchObjects(
                filters = expectedMarketplaceTypeFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                limit = 0,
                offset = 0,
                keys = expectedMarketplaceTypeKeys,
                fulltext = query
            )
        }
    }

    @Test
    fun `should add marketplace object to workspace if marketplace type is selected, then dispatch selected type`() = runTest {

        // SETUP

        val marketplaceType1 = StubObjectType()
        val marketplaceType2 = StubObjectType()
        val marketplaceType3 = StubObjectType(id = MarketplaceObjectTypeIds.PAGE)
        val installedType1 = StubObjectType(sourceObject = marketplaceType1.id)
        val installedType2 = StubObjectType(sourceObject = marketplaceType2.id)

        val expectedInstalledTypeId = ObjectTypeIds.PAGE

        val expectedMyTypesFilters = ObjectSearchConstants.filterObjectType
        val expectedMyTypeKeys = ObjectSearchConstants.defaultKeysObjectType

        val expectedMarketplaceTypeKeys = ObjectSearchConstants.defaultKeysObjectType
        val expectedMarketplaceTypeFilters = buildList {
            addAll(ObjectSearchConstants.filterObjectTypeMarketplace)
            add(
                DVFilter(
                    relationKey = Relations.ID,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(
                        marketplaceType1.id,
                        marketplaceType2.id,
                        MarketplaceObjectTypeIds.BOOKMARK
                    )
                )
            )
        }

        repo.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMyTypesFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = "",
                    keys = expectedMyTypeKeys
                )
            } doReturn listOf(
                installedType1.map, installedType2.map
            )
        }

        repo.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMarketplaceTypeFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = "",
                    keys = expectedMarketplaceTypeKeys
                )
            } doReturn listOf(marketplaceType3.map)
        }

        repo.stub {
            onBlocking {
                addObjectToWorkspace(objects = listOf(marketplaceType3.id))
            } doReturn listOf(expectedInstalledTypeId)
        }

        val vm = givenViewModel()

        // TESTING

        vm.onStart(
            isWithBookmark = false,
            isWithSet = false,
            isSetSource = false,
            excludeTypes = emptyList(),
            selectedTypes = emptyList()
        )

        // Checking search query for my types

        verifyBlocking(repo, times(1)) {
            searchObjects(
                filters = expectedMyTypesFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                limit = 0,
                offset = 0,
                keys = expectedMyTypeKeys,
                fulltext = ""
            )
        }

        // Checking search query for marketplace types

        verifyBlocking(repo, times(1)) {
            searchObjects(
                filters = expectedMarketplaceTypeFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                limit = 0,
                offset = 0,
                keys = expectedMarketplaceTypeKeys,
                fulltext = ""
            )
        }

        vm.commands.test {
            vm.onItemClicked(
                id = marketplaceType3.id,
                name = marketplaceType3.name.orEmpty()
            )
            delay(100)
            assertEquals(
                expected = ObjectTypeChangeViewModel.Command.DispatchType(
                    id = expectedInstalledTypeId,
                    name = marketplaceType3.name.orEmpty()
                ),
                actual = awaitItem()
            )
            verifyBlocking(repo, times(1)) {
                addObjectToWorkspace(objects = listOf(marketplaceType3.id))
            }
        }
    }

    @Test
    fun `should dispatch selected type if one is user types is selected`() = runTest {

        // SETUP

        val marketplaceType1 = StubObjectType()
        val marketplaceType2 = StubObjectType()
        val installedType1 = StubObjectType(sourceObject = marketplaceType1.id)
        val installedType2 = StubObjectType(sourceObject = marketplaceType2.id)


        val vm = givenViewModel()

        val expectedMyTypesFilters = ObjectSearchConstants.filterObjectType
        val expectedMyTypeKeys = ObjectSearchConstants.defaultKeysObjectType

        repo.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMyTypesFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = "",
                    keys = expectedMyTypeKeys
                )
            } doReturn listOf(
                installedType1.map, installedType2.map
            )
        }

        // TESTING

        vm.onStart(
            isWithBookmark = false,
            isWithSet = false,
            isSetSource = false,
            excludeTypes = emptyList(),
            selectedTypes = emptyList()
        )

        vm.commands.test {
            vm.onItemClicked(
                id = installedType1.id,
                name = installedType1.name.orEmpty()
            )
            assertEquals(
                expected = ObjectTypeChangeViewModel.Command.DispatchType(
                    id = installedType1.id,
                    name = installedType1.name.orEmpty()
                ),
                actual = awaitItem()
            )
        }

        verifyBlocking(repo, times(0)) {
            addObjectToWorkspace(objects = listOf(installedType1.id))
        }
    }

    private fun givenViewModel() = ObjectTypeChangeViewModel(
        getObjectTypes = getObjectTypes,
        addObjectToWorkspace = addObjectToWorkspace,
        dispatchers = dispatchers
    )
}
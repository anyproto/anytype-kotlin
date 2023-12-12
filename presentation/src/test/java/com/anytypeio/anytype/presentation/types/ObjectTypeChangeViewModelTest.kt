package com.anytypeio.anytype.presentation.types

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Marketplace.MARKETPLACE_SPACE_ID
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubObjectType
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.spaces.AddObjectToSpace
import com.anytypeio.anytype.domain.spaces.AddObjectTypeToSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModel
import com.anytypeio.anytype.presentation.objects.ObjectTypeView
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
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
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions

@ExperimentalCoroutinesApi
class ObjectTypeChangeViewModelTest {

    @Mock
    lateinit var blockRepository: BlockRepository

    @Mock
    lateinit var userSettingsRepository: UserSettingsRepository

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private lateinit var getObjectTypes: GetObjectTypes
    private lateinit var addObjectToSpace: AddObjectTypeToSpace
    private lateinit var getDefaultObjectType: GetDefaultObjectType

    val spaceId = MockDataFactory.randomUuid()

    @Mock
    lateinit var spaceManager: SpaceManager

    @Mock
    lateinit var configStorage: ConfigStorage

    private val dispatchers = AppCoroutineDispatchers(
        io = coroutineTestRule.testDispatcher,
        main = coroutineTestRule.testDispatcher,
        computation = coroutineTestRule.testDispatcher
    )

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        getObjectTypes = GetObjectTypes(blockRepository, dispatchers)
        addObjectToSpace = AddObjectToSpace(
            repo = blockRepository,
            dispatchers = dispatchers
        )
        getDefaultObjectType = GetDefaultObjectType(
            userSettingsRepository = userSettingsRepository,
            blockRepository = blockRepository,
            dispatchers = dispatchers,
            spaceManager = spaceManager,
            configStorage = configStorage
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

        stubSpaceManager(spaceId)
        val vm = givenViewModel()

        val expectedMyTypesFilters = ObjectSearchConstants.filterTypes(
            spaces = listOf(spaceId),
            recommendedLayouts = SupportedLayouts.editorLayouts
        )

        // TESTING

        verifyNoInteractions(blockRepository)

        vm.onStart(
            isWithBookmark = false,
            isWithCollection = false,
            isSetSource = false,
            excludeTypes = emptyList(),
            selectedTypes = emptyList()
        )

        verifyBlocking(blockRepository, times(1)) {
            searchObjects(
                filters = expectedMyTypesFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
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

        val marketplaceType1 = StubObjectType(uniqueKey = MockDataFactory.randomUuid())
        val marketplaceType2 = StubObjectType(uniqueKey = MockDataFactory.randomUuid())
        val installedType1 = StubObjectType(uniqueKey = marketplaceType1.uniqueKey)
        val installedType2 = StubObjectType(uniqueKey = marketplaceType2.uniqueKey)

        stubSpaceManager(spaceId)
        val vm = givenViewModel()

        val expectedMyTypesFilters = ObjectSearchConstants.filterTypes(
            spaces = listOf(spaceId),
            recommendedLayouts = SupportedLayouts.editorLayouts
        )
        val expectedMyTypeKeys = ObjectSearchConstants.defaultKeysObjectType

        val expectedMarketplaceTypeKeys = ObjectSearchConstants.defaultKeysObjectType
        val expectedMarketplaceTypeFilters = buildList {
            addAll(
                ObjectSearchConstants.filterTypes(
                    spaces = listOf(MARKETPLACE_SPACE_ID),
                    recommendedLayouts = SupportedLayouts.editorLayouts
                )
            )
            add(
                DVFilter(
                    relation = Relations.UNIQUE_KEY,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(
                        marketplaceType1.uniqueKey,
                        marketplaceType2.uniqueKey,
                        MarketplaceObjectTypeIds.BOOKMARK
                    )
                )
            )
        }

        blockRepository.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMyTypesFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
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

        verifyNoInteractions(blockRepository)

        vm.onStart(
            isWithBookmark = false,
            isWithCollection = false,
            isSetSource = false,
            excludeTypes = emptyList(),
            selectedTypes = emptyList()
        )

        // Checking search query for my types

        verifyBlocking(blockRepository, times(1)) {
            searchObjects(
                filters = expectedMyTypesFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                limit = 0,
                offset = 0,
                keys = expectedMyTypeKeys,
                fulltext = ""
            )
        }

        // Checking search query for marketplace types

        verifyBlocking(blockRepository, times(1)) {
            searchObjects(
                filters = expectedMarketplaceTypeFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
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

        val marketplaceType1 = StubObjectType(uniqueKey = MockDataFactory.randomUuid())
        val marketplaceType2 = StubObjectType(uniqueKey = MockDataFactory.randomUuid())
        val installedType1 = StubObjectType(uniqueKey = marketplaceType1.uniqueKey)
        val installedType2 = StubObjectType(uniqueKey = marketplaceType2.uniqueKey)

        stubSpaceManager(spaceId)
        val vm = givenViewModel()

        val expectedMyTypesFilters = ObjectSearchConstants.filterTypes(
            spaces = listOf(spaceId),
            recommendedLayouts = SupportedLayouts.editorLayouts
        )
        val expectedMyTypeKeys = ObjectSearchConstants.defaultKeysObjectType

        val expectedMarketplaceTypeKeys = ObjectSearchConstants.defaultKeysObjectType
        val expectedMarketplaceTypeFilters = buildList {
            addAll(
                ObjectSearchConstants.filterTypes(
                    spaces = listOf(MARKETPLACE_SPACE_ID),
                    recommendedLayouts = SupportedLayouts.editorLayouts
                )
            )
            add(
                DVFilter(
                    relation = Relations.UNIQUE_KEY,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(
                        marketplaceType1.uniqueKey,
                        marketplaceType2.uniqueKey,
                        MarketplaceObjectTypeIds.BOOKMARK
                    )
                )
            )
        }

        val query = MockDataFactory.randomString()

        blockRepository.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMyTypesFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = "",
                    keys = expectedMyTypeKeys
                )
            } doReturn listOf(
                installedType1.map, installedType2.map
            )
        }

        blockRepository.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMarketplaceTypeFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = "",
                    keys = expectedMarketplaceTypeKeys
                )
            } doReturn emptyList()
        }

        blockRepository.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMyTypesFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = query,
                    keys = expectedMyTypeKeys
                )
            } doReturn listOf(
                installedType1.map, installedType2.map
            )
        }

        blockRepository.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMarketplaceTypeFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = query,
                    keys = expectedMarketplaceTypeKeys
                )
            } doReturn emptyList()
        }

        // TESTING

        verifyNoInteractions(blockRepository)

        vm.onStart(
            isWithBookmark = false,
            isWithCollection = false,
            isSetSource = false,
            excludeTypes = emptyList(),
            selectedTypes = emptyList()
        )

        // Checking search query for my types

        verifyBlocking(blockRepository, times(1)) {
            searchObjects(
                filters = expectedMyTypesFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                limit = 0,
                offset = 0,
                keys = expectedMyTypeKeys,
                fulltext = ""
            )
        }

        // Checking search query for marketplace types

        verifyBlocking(blockRepository, times(1)) {
            searchObjects(
                filters = expectedMarketplaceTypeFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
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

        verifyBlocking(blockRepository, times(1)) {
            searchObjects(
                filters = expectedMyTypesFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                limit = 0,
                offset = 0,
                keys = expectedMyTypeKeys,
                fulltext = query
            )
        }

        // Checking search query for marketplace types

        verifyBlocking(blockRepository, times(1)) {
            searchObjects(
                filters = expectedMarketplaceTypeFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                limit = 0,
                offset = 0,
                keys = expectedMarketplaceTypeKeys,
                fulltext = query
            )
        }
    }

    @Test
    fun `should add marketplace object to space if marketplace type is selected, then dispatch selected type`() = runTest {

        // SETUP

        val space = "space-id"
        stubSpaceManager(space)
        val marketplaceType1 = StubObjectType()
        val marketplaceType2 = StubObjectType()
        val marketplaceType3 = StubObjectType(id = MarketplaceObjectTypeIds.PAGE)
        val installedType1 = StubObjectType(sourceObject = marketplaceType1.id)
        val installedType2 = StubObjectType(sourceObject = marketplaceType2.id)

        val expectedInstalledTypeId = MockDataFactory.randomUuid()
        val expectedInstalledTypeUniqueKey = ObjectTypeIds.PAGE

        val expectedMyTypesFilters = ObjectSearchConstants.filterTypes(
            spaces = listOf(space),
            recommendedLayouts = SupportedLayouts.editorLayouts
        )
        val expectedMyTypeKeys = ObjectSearchConstants.defaultKeysObjectType

        val expectedMarketplaceTypeKeys = ObjectSearchConstants.defaultKeysObjectType
        val expectedMarketplaceTypeFilters = buildList {
            addAll(
                ObjectSearchConstants.filterTypes(
                    spaces = listOf(MARKETPLACE_SPACE_ID),
                    recommendedLayouts = SupportedLayouts.editorLayouts
                )
            )
            add(
                DVFilter(
                    relation = Relations.ID,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(
                        marketplaceType1.uniqueKey,
                        marketplaceType2.uniqueKey,
                        MarketplaceObjectTypeIds.BOOKMARK
                    )
                )
            )
        }

        blockRepository.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMyTypesFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = "",
                    keys = expectedMyTypeKeys
                )
            } doReturn listOf(
                installedType1.map, installedType2.map
            )
        }

        blockRepository.stub {
            onBlocking {
                searchObjects(
                    filters = expectedMarketplaceTypeFilters,
                    sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                    limit = 0,
                    offset = 0,
                    fulltext = "",
                    keys = expectedMarketplaceTypeKeys
                )
            } doReturn listOf(marketplaceType3.map)
        }

        val expectedType = mapOf(
            Relations.ID to  expectedInstalledTypeId,
            Relations.UNIQUE_KEY to expectedInstalledTypeUniqueKey,
            Relations.NAME to  "added type name",
        )
        blockRepository.stub {
            onBlocking {
                val command = Command.AddObjectToSpace(
                    objectId = marketplaceType3.id,
                    space = space
                )
                addObjectToSpace(command)
            } doReturn Pair(
                expectedInstalledTypeId,
                expectedType
            )
        }

        val vm = givenViewModel()

        // TESTING

        vm.onStart(
            isWithBookmark = false,
            isWithCollection = false,
            isSetSource = false,
            excludeTypes = emptyList(),
            selectedTypes = emptyList()
        )

        // Checking search query for my types

        verifyBlocking(blockRepository, times(1)) {
            searchObjects(
                filters = expectedMyTypesFilters,
                sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                limit = 0,
                offset = 0,
                keys = expectedMyTypeKeys,
                fulltext = ""
            )
        }

        vm.commands.test {
            val item = ObjectTypeView(
                id = marketplaceType3.id,
                key = marketplaceType3.uniqueKey.orEmpty(),
                name = marketplaceType3.name.orEmpty(),
                description = marketplaceType3.description.orEmpty(),
                emoji = marketplaceType3.iconEmoji.orEmpty(),
            )
            vm.onItemClicked(item)
            assertEquals(
                expected =
                ObjectTypeChangeViewModel.Command.TypeAdded(type = "added type name"),
                actual = awaitItem()
            )
            val expected = expectedType.mapToObjectWrapperType()!!
            assertEquals(
                expected = ObjectTypeChangeViewModel.Command.DispatchType(
                    expected
                ),
                actual = awaitItem()
            )
            verifyBlocking(blockRepository, times(1)) {
                addObjectToSpace(
                    Command.AddObjectToSpace(
                        objectId = marketplaceType3.id,
                        space = space
                    )
                )
            }
        }
    }

    private fun givenViewModel() = ObjectTypeChangeViewModel(
        getObjectTypes = getObjectTypes,
        addObjectTypeToSpace = addObjectToSpace,
        dispatchers = dispatchers,
        spaceManager = spaceManager,
        getDefaultObjectType = getDefaultObjectType
    )

    fun stubSpaceManager(spaceId: Id) {
        spaceManager.stub {
            onBlocking { get() } doReturn spaceId
        }
    }
}
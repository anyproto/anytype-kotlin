package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.collections.MockCollection
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.state.ObjectState.Companion.VIEW_DEFAULT_OBJECT_TYPE
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations


@OptIn(ExperimentalCoroutinesApi::class)
class ViewerDefaultObjectTypeTest : ObjectSetViewModelTestSetup() {

    private lateinit var closable: AutoCloseable
    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockSetByType: MockSet
    private lateinit var mockSetByRelation: MockSet
    private lateinit var mockCollection: MockCollection

    val defaultTypeId = "defaultTypeId-${RandomString.make()}"
    val defaultTypeKey = VIEW_DEFAULT_OBJECT_TYPE
    val customType1Id = "customType1-${RandomString.make()}"
    val customType1Key = "customType1Key-${RandomString.make()}"
    val customType2Id = "customType2-${RandomString.make()}"
    val customType2Key = "customType2Key-${RandomString.make()}"
    val customType1Map = mapOf(
        Relations.ID to customType1Id,
        Relations.UNIQUE_KEY to customType1Key,
        Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
        Relations.NAME to "customType1"
    )

    val customType2Map = mapOf(
        Relations.ID to customType2Id,
        Relations.UNIQUE_KEY to customType2Key,
        Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.SET.code.toDouble(),
        Relations.NAME to "customType2"
    )

    val pageTypeMap = mapOf(
        Relations.ID to defaultTypeId,
        Relations.UNIQUE_KEY to defaultTypeKey,
        Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
        Relations.NAME to MockDataFactory.randomString()
    )

    @Before
    fun setup() {
        closable = MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        stubNetworkMode()
        stubObservePermissions()
        stubAnalyticSpaceHelperDelegate()
    }

    @After
    fun after() {
        rule.advanceTime()
        closable.close()
    }

    @Test
    fun `set by type should has this type for all views default object type ids`() = runTest {

        val setOfValue = MockDataFactory.randomUuid()
        mockSetByType = MockSet(context = root, setOfValue = setOfValue, space = defaultSpace)

        with(mockSetByType) {
            stubSpaceManager(spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()

            with(storeOfObjectTypes) {
                set(defaultTypeId, pageTypeMap)
                set(customType1Id, customType1Map)
                set(customType2Id, customType2Map)
            }

            val viewer1 = viewerGrid.copy(defaultObjectType = customType1Id)
            val viewer2 = viewerGallery.copy(defaultObjectType = customType2Id)
            val viewer3 = viewerList.copy(defaultObjectType = defaultTypeId)

            val dataViewWith3Views = StubDataView(
                id = "dv-${RandomString.make()}",
                views = listOf(viewer1, viewer2, viewer3),
                relationLinks = listOf(
                    relationLink1,
                    relationLink2,
                    relationLink3,
                    relationLink4,
                    relationLink5
                )
            )
            stubOpenObject(
                doc = listOf(header, title, dataViewWith3Views),
                details = details
            )
            stubSubscriptionResults(
                subscription = subscriptionId,
                spaceId = spaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                sources = listOf(setOfValue),
                dvFilters = filters
            )
            stubTemplatesForTemplatesContainer()
        }

        proceedWithStartingViewModel()

        advanceUntilIdle()

        val result = viewModel.viewersWidgetState.value

        val viewer1 = result.items[0]
        val viewer2 = result.items[1]
        val viewer3 = result.items[2]

        assertEquals(
            expected = setOfValue,
            actual = viewer1.defaultObjectType
        )
        assertEquals(
            expected = setOfValue,
            actual = viewer2.defaultObjectType
        )
        assertEquals(
            expected = setOfValue,
            actual = viewer3.defaultObjectType
        )
        assertEquals(
            expected = 3,
            actual = result.items.size
        )
    }

    @Test
    fun `set by relation should has proper views default object type ids`() = runTest {

        mockSetByRelation = MockSet(
            context = root,
            space = defaultSpace,
            setOfValue = MockSet("", space = defaultSpace).relationObject3.id
        )
        with(mockSetByRelation) {

            stubSpaceManager(spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()

            with(storeOfObjectTypes) {
                set(defaultTypeId, pageTypeMap)
                set(customType1Id, customType1Map)
                set(customType2Id, customType2Map)
            }

            val viewer1 = viewerGrid.copy(defaultObjectType = customType1Id)
            val viewer2 = viewerGallery.copy(defaultObjectType = customType2Id)
            val viewer3 = viewerList.copy(defaultObjectType = defaultTypeId)

            val dataViewWith3Views = StubDataView(
                id = "dv-${RandomString.make()}",
                views = listOf(viewer1, viewer2, viewer3),
                relationLinks = listOf(
                    relationLink1,
                    relationLink2,
                    relationLink3,
                    relationLink4,
                    relationLink5
                )
            )
            stubOpenObject(
                doc = listOf(header, title, dataViewWith3Views),
                details = detailsSetByRelation
            )
            stubSubscriptionResults(
                subscription = subscriptionId,
                spaceId = spaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                sources = listOf(relationObject3.id),
                dvFilters = filters
            )
            stubTemplatesForTemplatesContainer()
        }

        proceedWithStartingViewModel()

        advanceUntilIdle()

        val result = viewModel.viewersWidgetState.value

        val viewer1 = result.items[0]
        val viewer2 = result.items[1]
        val viewer3 = result.items[2]

        assertEquals(
            expected = customType1Id,
            actual = viewer1.defaultObjectType
        )
        assertEquals(
            expected = customType2Id,
            actual = viewer2.defaultObjectType
        )
        assertEquals(
            expected = defaultTypeId,
            actual = viewer3.defaultObjectType
        )
        assertEquals(
            expected = 3,
            actual = result.items.size
        )
    }

    @Test
    fun `collection should has proper views default object type ids`() = runTest {

        mockCollection = MockCollection(context = root, space = defaultSpace)
        with(mockCollection) {
            stubSpaceManager(spaceId)
            stubStoreOfRelations(this)

            stubSpaceManager(spaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()

            with(storeOfObjectTypes) {
                set(defaultTypeId, pageTypeMap)
                set(customType1Id, customType1Map)
                set(customType2Id, customType2Map)
            }

            val viewer1 = viewerGrid.copy(defaultObjectType = customType1Id)
            val viewer2 = viewerGallery.copy(defaultObjectType = customType2Id)
            val viewer3 = viewerList.copy(defaultObjectType = defaultTypeId)

            session.currentViewerId.value = viewer3.id

            val dataViewWith3Views = StubDataView(
                id = "dv-${RandomString.make()}",
                views = listOf(viewer1, viewer2, viewer3),
                relationLinks = listOf(
                    relationLink1,
                    relationLink2,
                    relationLink3,
                    relationLink4,
                    relationLink5,
                    relationLink6
                )
            )
            stubOpenObject(
                doc = listOf(header, title, dataViewWith3Views),
                details = details
            )
            stubSubscriptionResults(
                subscription = this.subscriptionId,
                collection = root,
                spaceId = spaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                objects = listOf(obj1, obj2),
                dvSorts = sorts
            )
            stubTemplatesForTemplatesContainer()
        }

        proceedWithStartingViewModel()

        advanceUntilIdle()

        val result = viewModel.viewersWidgetState.value

        val viewer1 = result.items[0]
        val viewer2 = result.items[1]
        val viewer3 = result.items[2]

        assertEquals(
            expected = customType1Id,
            actual = viewer1.defaultObjectType
        )
        assertEquals(
            expected = customType2Id,
            actual = viewer2.defaultObjectType
        )
        assertEquals(
            expected = defaultTypeId,
            actual = viewer3.defaultObjectType
        )
        assertEquals(
            expected = 3,
            actual = result.items.size
        )
    }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart(ctx = root, space = defaultSpace)
    }
}
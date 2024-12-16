package com.anytypeio.anytype.feature_date

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.relations.GetObjectRelationListById
import com.anytypeio.anytype.feature_date.ui.models.DateEvent
import com.anytypeio.anytype.feature_date.viewmodel.ActiveField
import com.anytypeio.anytype.feature_date.viewmodel.DateObjectViewModel
import com.anytypeio.anytype.feature_date.viewmodel.DateObjectViewModel.Companion.DEFAULT_SEARCH_LIMIT
import com.anytypeio.anytype.feature_date.viewmodel.DateObjectVmParams
import com.anytypeio.anytype.feature_date.viewmodel.createSearchParams
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class DateObjectViewModelTest {

    @get:Rule
    var rule: TestRule = DefaultCoroutineTestRule()

    @Mock private lateinit var getObject: GetObject
    @Mock private lateinit var analytics: Analytics
    @Mock private lateinit var urlBuilder: UrlBuilder
    @Mock lateinit var dateProvider: DateProvider
    @Mock private lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
    @Mock private lateinit var userPermissionProvider: UserPermissionProvider
    @Mock private lateinit var relationListWithValue: GetObjectRelationListById
    @Mock private lateinit var storeOfObjectTypes: StoreOfObjectTypes
    @Mock private lateinit var storelessSubscriptionContainer: StorelessSubscriptionContainer
    @Mock private lateinit var spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider
    @Mock lateinit var createObject: CreateObject
    @Mock lateinit var setObjectListIsArchived: SetObjectListIsArchived
    @Mock lateinit var fieldParser: FieldParser
    @Mock lateinit var getDateObjectByTimestamp: GetDateObjectByTimestamp
    private lateinit var storeOfRelations: StoreOfRelations

    private val spaceId = SpaceId("testSpaceId")

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        storelessSubscriptionContainer = mock(verboseLogging = true)
        setupDefaultMocks()
        storeOfRelations = DefaultStoreOfRelations()
    }

    private fun setupDefaultMocks() {
        `when`(userPermissionProvider.get(space = spaceId)).thenReturn(SpaceMemberPermissions.OWNER)
        `when`(userPermissionProvider.observe(space = spaceId)).thenReturn(flowOf(SpaceMemberPermissions.OWNER))
    }

    private fun createGetObjectParams(objectId: String): GetObject.Params {
        return GetObject.Params(
            target = objectId,
            space = spaceId,
            saveAsLastOpened = true
        )
    }

    private fun createRelationListWithValueParams(objectId: String): GetObjectRelationListById.Params {
        return GetObjectRelationListById.Params(
            space = spaceId,
            value = objectId
        )
    }

    private suspend fun mockGetObjectSuccess(objectId: String, stubObjectView: ObjectView) {
        val params = createGetObjectParams(objectId)
        whenever(getObject.async(params)).thenReturn(Resultat.success(stubObjectView))
    }

    private suspend fun mockRelationListWithValueSuccess(objectId: String, list: List<RelationListWithValueItem>) {
        val params = createRelationListWithValueParams(objectId)
        whenever(relationListWithValue.async(params)).thenReturn(Resultat.success(list))
    }

    @Test
    fun `should call getObjects and getRelationListWithValue on init`() = runTest {

        // Arrange
        val objectId = "testObjectId-${RandomString.make()}"
        val stubObjectView = StubObjectView(root = objectId)
        mockGetObjectSuccess(objectId, stubObjectView)
        mockRelationListWithValueSuccess(objectId, emptyList())

        // Act
        getViewModel(objectId = objectId, spaceId = spaceId)
        advanceUntilIdle()

        // Assert
        verifyBlocking(getObject, times(1)) { async(createGetObjectParams(objectId)) }
        verifyBlocking(relationListWithValue, times(1)) { async(createRelationListWithValueParams(objectId)) }
    }

    @Test
    fun `should call getObjects and getRelationListWithValue again after the dateObjectId was updated `() =
        runTest {

            // Arrange
            val objectId = "testObjectId1-${RandomString.make()}"
            val nextObjectId = "nextObjectId-${RandomString.make()}"
            val stubObjectView = StubObjectView(root = objectId)

            mockGetObjectSuccess(objectId, stubObjectView)
            mockRelationListWithValueSuccess(objectId, emptyList())

            val vm = getViewModel(objectId = objectId, spaceId = spaceId)

            // Act
            advanceUntilIdle()

            // Assert
            verifyBlocking(getObject, times(1)) { async(createGetObjectParams(objectId)) }
            verifyBlocking(relationListWithValue, times(1)) { async(createRelationListWithValueParams(objectId)) }

            // Arrange for next call
            val tomorrowTimestamp = 211L
            whenever(dateProvider.getTimestampForTomorrowAtStartOfDay())
                .thenReturn(tomorrowTimestamp)

            val params = GetDateObjectByTimestamp.Params(
                timestampInSeconds = tomorrowTimestamp,
                space = spaceId
            )
            whenever(getDateObjectByTimestamp.async(params)).thenReturn(
                Resultat.success(
                    mapOf(
                        Relations.ID to nextObjectId
                    )
                )
            )

            mockGetObjectSuccess(nextObjectId, stubObjectView)
            mockRelationListWithValueSuccess(nextObjectId, emptyList())

            // Act
            vm.onDateEvent(DateEvent.Calendar.OnTomorrowClick)
            advanceUntilIdle()

            // Assert
            verifyBlocking(getObject, times(1)) { async(createGetObjectParams(nextObjectId)) }
            verifyBlocking(relationListWithValue, times(1)) { async(createRelationListWithValueParams(nextObjectId)) }
        }

    @Test
    fun `should properly filter date object relation links`() =
        runTest {

            // Arrange

            val fieldKey = RelationKey("key1-${RandomString.make()}")
            val fieldKey2 = RelationKey("key2-${RandomString.make()}")
            val fieldKey3 = RelationKey("key3-${RandomString.make()}")

            val relationsListWithValues = listOf(
                RelationListWithValueItem(
                    key = RelationKey(Relations.LINKS),
                    counter = 2L
                ),
                RelationListWithValueItem(
                    key = fieldKey3,
                    counter = 13L
                ),
                RelationListWithValueItem(
                    key = RelationKey(Relations.MENTIONS),
                    counter = 5L
                ),
                RelationListWithValueItem(
                    key = RelationKey(Relations.BACKLINKS),
                    counter = 5L
                ),
                RelationListWithValueItem(
                    key = fieldKey, //hidden
                    counter = 9L
                ),
                RelationListWithValueItem(
                    key = fieldKey2,
                    counter = 1L
                ),
                RelationListWithValueItem(
                    key = RelationKey(RandomString.make()), // not present in the store
                    counter = 1L
                ),
            )

            whenever(dateProvider.formatTimestampToDateAndTime(123 * 1000))
                .thenReturn("01-01-2024" to "12:00")
            whenever(dateProvider.calculateRelativeDates(123))
                .thenReturn(
                    RelativeDate.Other(
                        initialTimeInMillis = 123 * 1000,
                        dayOfWeek = DayOfWeekCustom.MONDAY,
                        formattedDate = "01-01-2024",
                        formattedTime = "12:00"
                    )
                )

            val objectId = "testObjectId1-${RandomString.make()}"
            val stubObjectView = StubObjectView(
                root = objectId,
                details = mapOf(
                    objectId to mapOf(
                        Relations.TIMESTAMP to 123.0
                    )
                )
            )

            mockGetObjectSuccess(objectId, stubObjectView)
            mockRelationListWithValueSuccess(objectId, relationsListWithValues)

            val vm = getViewModel(objectId = objectId, spaceId = spaceId)

            val subscribeParams =  vm.createSearchParams(
                dateId = objectId,
                timestamp = 123,
                space = spaceId,
                itemsLimit = DEFAULT_SEARCH_LIMIT,
                field = ActiveField(
                    key = RelationKey(Relations.MENTIONS),
                    format = RelationFormat.OBJECT
                )
            )

            whenever(storelessSubscriptionContainer.subscribe(subscribeParams))
                .thenReturn(emptyFlow<List<ObjectWrapper.Basic>>())

            // Act
            advanceUntilIdle()

            // Assert
            verifyNoInteractions(storelessSubscriptionContainer)

            // Arrange, list of relations in store
            val relationObjects = listOf(
                StubRelationObject(
                    key = fieldKey.key,
                    name = "Hidden field",
                    format = RelationFormat.OBJECT,
                    isHidden = true
                ),
                StubRelationObject(
                    key = fieldKey2.key,
                    name = "Some date relations 1",
                    format = RelationFormat.DATE
                ),
                StubRelationObject(
                    key = Relations.LINKS,
                    name = "Links",
                    format = RelationFormat.OBJECT
                ),
                StubRelationObject(
                    key = Relations.BACKLINKS,
                    name = "Backlinks",
                    format = RelationFormat.OBJECT
                ),
                StubRelationObject(
                    key = Relations.MENTIONS,
                    name = "Mentions",
                    format = RelationFormat.OBJECT
                ),
                StubRelationObject(
                    key = fieldKey3.key,
                    name = "Some date relations 2",
                    format = RelationFormat.DATE
                ),
            )

            // Act, store of relations get updated
            storeOfRelations.merge(relationObjects)
            advanceUntilIdle()

            // Assert
            verifyNoInteractions(storelessSubscriptionContainer)

            // Act
            vm.onStart()
            advanceUntilIdle()

            // Assert
            verifyBlocking(storelessSubscriptionContainer, times(1)) {
                subscribe(subscribeParams)
            }
        }

    @Test
    fun `should not start the subscription if the store was not updated`() =
        runTest {

            // Arrange
            val objectId = "testObjectId1-${RandomString.make()}"
            val stubObjectView = StubObjectView(
                root = objectId,
                details = mapOf(
                    objectId to mapOf(
                        Relations.TIMESTAMP to 123.0
                    )
                )
            )

            val relationsListWithValues = listOf(
                RelationListWithValueItem(
                    key = RelationKey(Relations.MENTIONS),
                    counter = 5L
                )
            )

            mockGetObjectSuccess(objectId, stubObjectView)
            mockRelationListWithValueSuccess(objectId, relationsListWithValues)

            whenever(dateProvider.formatTimestampToDateAndTime(123 * 1000))
                .thenReturn("01-01-2024" to "12:00")
            whenever(dateProvider.calculateRelativeDates(123))
                .thenReturn(
                    RelativeDate.Other(
                        initialTimeInMillis = 123 * 1000,
                        dayOfWeek = DayOfWeekCustom.MONDAY,
                        formattedDate = "01-01-2024",
                        formattedTime = "12:00"
                    )
                )

            val vm = getViewModel(objectId = objectId, spaceId = spaceId)

            val subscribeParams =  vm.createSearchParams(
                dateId = objectId,
                timestamp = 123,
                space = spaceId,
                itemsLimit = DEFAULT_SEARCH_LIMIT,
                field = ActiveField(
                    key = RelationKey(Relations.MENTIONS),
                    format = RelationFormat.OBJECT
                )
            )

            whenever(storelessSubscriptionContainer.subscribe(subscribeParams))
                .thenReturn(emptyFlow<List<ObjectWrapper.Basic>>())

            // Act waiting for the init to finish
            advanceUntilIdle()

            // Act 2 waiting for the start to finish
            vm.onStart()
            advanceUntilIdle()

            // Assert
            verifyNoInteractions(storelessSubscriptionContainer)
        }

    @Test
    fun `should start the subscription if the store was updated before init`() =
        runTest {

            // Arrange
            val objectId = "testObjectId1-${RandomString.make()}"
            val stubObjectView = StubObjectView(
                root = objectId,
                details = mapOf(
                    objectId to mapOf(
                        Relations.TIMESTAMP to 123.0
                    )
                )
            )

            val relationsListWithValues = listOf(
                RelationListWithValueItem(
                    key = RelationKey(Relations.MENTIONS),
                    counter = 5L
                )
            )

            mockGetObjectSuccess(objectId, stubObjectView)
            mockRelationListWithValueSuccess(objectId, relationsListWithValues)

            whenever(dateProvider.formatTimestampToDateAndTime(123 * 1000))
                .thenReturn("01-01-2024" to "12:00")
            whenever(dateProvider.calculateRelativeDates(123))
                .thenReturn(
                    RelativeDate.Other(
                        initialTimeInMillis = 123 * 1000,
                        dayOfWeek = DayOfWeekCustom.MONDAY,
                        formattedDate = "01-01-2024",
                        formattedTime = "12:00"
                    )
                )

            // Arrange, list of relations in store
            val relationObjects = listOf(
                StubRelationObject(
                    key = Relations.MENTIONS,
                    name = "Mentions",
                    format = RelationFormat.OBJECT
                )
            )

            // Act, store of relations get updated
            storeOfRelations.merge(relationObjects)
            advanceUntilIdle()

            val vm = getViewModel(objectId = objectId, spaceId = spaceId)

            val subscribeParams =  vm.createSearchParams(
                dateId = objectId,
                timestamp = 123,
                space = spaceId,
                itemsLimit = DEFAULT_SEARCH_LIMIT,
                field = ActiveField(
                    key = RelationKey(Relations.MENTIONS),
                    format = RelationFormat.OBJECT
                )
            )

            whenever(storelessSubscriptionContainer.subscribe(subscribeParams))
                .thenReturn(emptyFlow<List<ObjectWrapper.Basic>>())

            // Act waiting for the init to finish
            advanceUntilIdle()

            // Act 2 waiting for the start to finish
            vm.onStart()
            advanceUntilIdle()

            // Assert
            verifyBlocking(storelessSubscriptionContainer, times(1)) {
                subscribe(subscribeParams)
            }
        }

    @Test
    fun `should start the subscription if the store was updated before onStart`() =
        runTest {

            // Arrange
            val objectId = "testObjectId1-${RandomString.make()}"
            val stubObjectView = StubObjectView(
                root = objectId,
                details = mapOf(
                    objectId to mapOf(
                        Relations.TIMESTAMP to 123.0
                    )
                )
            )

            val relationsListWithValues = listOf(
                RelationListWithValueItem(
                    key = RelationKey(Relations.MENTIONS),
                    counter = 5L
                )
            )

            mockGetObjectSuccess(objectId, stubObjectView)
            mockRelationListWithValueSuccess(objectId, relationsListWithValues)

            whenever(dateProvider.formatTimestampToDateAndTime(123 * 1000))
                .thenReturn("01-01-2024" to "12:00")
            whenever(dateProvider.calculateRelativeDates(123))
                .thenReturn(
                    RelativeDate.Other(
                        initialTimeInMillis = 123 * 1000,
                        dayOfWeek = DayOfWeekCustom.MONDAY,
                        formattedDate = "01-01-2024",
                        formattedTime = "12:00"
                    )
                )

            val vm = getViewModel(objectId = objectId, spaceId = spaceId)

            val subscribeParams =  vm.createSearchParams(
                dateId = objectId,
                timestamp = 123,
                space = spaceId,
                itemsLimit = DEFAULT_SEARCH_LIMIT,
                field = ActiveField(
                    key = RelationKey(Relations.MENTIONS),
                    format = RelationFormat.OBJECT
                )
            )

            whenever(storelessSubscriptionContainer.subscribe(subscribeParams))
                .thenReturn(emptyFlow<List<ObjectWrapper.Basic>>())

            // Act waiting for the init to finish
            advanceUntilIdle()

            // Assert
            verifyNoInteractions(storelessSubscriptionContainer)

            // Arrange, list of relations in store
            val relationObjects = listOf(
                StubRelationObject(
                    key = Relations.MENTIONS,
                    name = "Mentions",
                    format = RelationFormat.OBJECT
                )
            )

            // Act, store of relations get updated
            storeOfRelations.merge(relationObjects)
            advanceUntilIdle()

            // Act 2 waiting for the start to finish
            vm.onStart()
            advanceUntilIdle()

            // Assert
            verifyBlocking(storelessSubscriptionContainer, times(1)) {
                subscribe(subscribeParams)
            }
        }

    @Test
    fun `should start the subscription if the store was updated after onStart`() =
        runTest {

            // Arrange
            val objectId = "testObjectId1-${RandomString.make()}"
            val stubObjectView = StubObjectView(
                root = objectId,
                details = mapOf(
                    objectId to mapOf(
                        Relations.TIMESTAMP to 123.0
                    )
                )
            )

            val relationsListWithValues = listOf(
                RelationListWithValueItem(
                    key = RelationKey(Relations.MENTIONS),
                    counter = 5L
                )
            )

            mockGetObjectSuccess(objectId, stubObjectView)
            mockRelationListWithValueSuccess(objectId, relationsListWithValues)

            whenever(dateProvider.formatTimestampToDateAndTime(123 * 1000))
                .thenReturn("01-01-2024" to "12:00")
            whenever(dateProvider.calculateRelativeDates(123))
                .thenReturn(
                    RelativeDate.Other(
                        initialTimeInMillis = 123 * 1000,
                        dayOfWeek = DayOfWeekCustom.MONDAY,
                        formattedDate = "01-01-2024",
                        formattedTime = "12:00"
                    )
                )

            val vm = getViewModel(objectId = objectId, spaceId = spaceId)

            val subscribeParams =  vm.createSearchParams(
                dateId = objectId,
                timestamp = 123,
                space = spaceId,
                itemsLimit = DEFAULT_SEARCH_LIMIT,
                field = ActiveField(
                    key = RelationKey(Relations.MENTIONS),
                    format = RelationFormat.OBJECT
                )
            )

            whenever(storelessSubscriptionContainer.subscribe(subscribeParams))
                .thenReturn(emptyFlow<List<ObjectWrapper.Basic>>())

            // Act waiting for the init to finish
            advanceUntilIdle()

            // Act 2 waiting for the start to finish
            vm.onStart()
            advanceUntilIdle()

            // Assert
            verifyNoInteractions(storelessSubscriptionContainer)

            // Arrange, list of relations in store
            val relationObjects = listOf(
                StubRelationObject(
                    key = Relations.MENTIONS,
                    name = "Mentions",
                    format = RelationFormat.OBJECT
                )
            )

            // Act, store of relations get updated
            storeOfRelations.merge(relationObjects)
            advanceUntilIdle()

            // Assert
            verifyBlocking(storelessSubscriptionContainer, times(1)) {
                subscribe(subscribeParams)
            }
        }

    private fun getViewModel(objectId: Id, spaceId: SpaceId): DateObjectViewModel {
        val vmParams = DateObjectVmParams(
            objectId = objectId,
            spaceId = spaceId
        )
        return DateObjectViewModel(
            getObject = getObject,
            analytics = analytics,
            urlBuilder = urlBuilder,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            userPermissionProvider = userPermissionProvider,
            getObjectRelationListById = relationListWithValue,
            storeOfRelations = storeOfRelations,
            storeOfObjectTypes = storeOfObjectTypes,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            spaceSyncAndP2PStatusProvider = spaceSyncAndP2PStatusProvider,
            fieldParser = fieldParser,
            vmParams = vmParams,
            dateProvider = dateProvider,
            createObject = createObject,
            setObjectListIsArchived = setObjectListIsArchived,
            getDateObjectByTimestamp = getDateObjectByTimestamp
        )
    }
}
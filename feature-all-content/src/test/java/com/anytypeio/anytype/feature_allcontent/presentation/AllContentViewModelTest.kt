package com.anytypeio.anytype.feature_allcontent.presentation

import android.util.Log
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.all_content.RestoreAllContentState
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.feature_allcontent.models.AllContentSort
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.UiContentItem
import com.anytypeio.anytype.feature_allcontent.models.UiContentState
import com.anytypeio.anytype.feature_allcontent.models.createSubscriptionParams
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel.Companion.PREVIOUS_14_DAYS_ID
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel.Companion.PREVIOUS_7_DAYS_ID
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel.Companion.TODAY_ID
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel.Companion.YESTERDAY_ID
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.widgets.collection.DateProviderImpl
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.mockedTime
import com.anytypeio.anytype.test_utils.mockedZone
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.bytebuddy.utility.RandomString
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub


class AllContentViewModelTest {

    private val vmParams = AllContentViewModel.VmParams(
        spaceId = SpaceId("spaceId-${RandomString.make()}"),
        useHistory = true
    )

    private val dispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(false)
        .build()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatchers = AppCoroutineDispatchers(
        io = dispatcher,
        main = dispatcher,
        computation = dispatcher
    ).also { Dispatchers.setMain(dispatcher) }

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var userSettingsRepository: UserSettingsRepository

    @Mock
    lateinit var urlBuilder: UrlBuilder

    lateinit var objectSearch: SearchObjects

    @Mock
    lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate

    private val dateProvider: DateProvider = DateProviderImpl(ZoneId.systemDefault())

    @Mock
    lateinit var localeProvider: LocaleProvider

    @Mock
    lateinit var storeOfRelations: StoreOfRelations

    lateinit var storelessSubscriptionContainer: StorelessSubscriptionContainer

    @Mock
    lateinit var restoreAllContentState: RestoreAllContentState

    @Mock
    lateinit var eventChannel: SubscriptionEventChannel

    lateinit var storeOfObjectTypes: StoreOfObjectTypes

    @Mock
    lateinit var logger: Logger

    lateinit var lockedLocalDateMocked: MockedStatic<LocalDate>
    lateinit var lockedZoneIdMocked: MockedStatic<ZoneId>

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        storeOfObjectTypes = DefaultStoreOfObjectTypes()
        storelessSubscriptionContainer = StorelessSubscriptionContainer.Impl(
            repo = repo,
            dispatchers = dispatchers,
            logger = logger,
            channel = eventChannel
        )
        objectSearch = SearchObjects(repo = repo)
        stubLocale(locale = Locale.GERMANY)
        val zoneId = ZoneId.of("Europe/Berlin")
        val localDate = LocalDate.of(2024, 9, 28)
    }

    @After
    fun after() {
        lockedLocalDateMocked.closeOnDemand()
    }

    @Test
    fun `test1`() = runTest {
        //turbineScope {
        val timestamp =
            1727521600L //Sat Sep 28 2024 13:06:40 GMT+0200 (Central European Summer Time)

        lockedLocalDateMocked =
            Mockito.mockStatic(LocalDate::class.java, Mockito.CALLS_REAL_METHODS)
        val localDate = LocalDate.of(2024, 9, 28)
        lockedLocalDateMocked.`when`<LocalDate> { LocalDate.now(ZoneId.systemDefault()) }
            .thenReturn(localDate)

        lockedZoneIdMocked = Mockito.mockStatic(ZoneId::class.java, Mockito.CALLS_REAL_METHODS)
        val zoneId = ZoneId.of("Europe/Berlin")
        lockedZoneIdMocked.`when`<ZoneId> { ZoneId.systemDefault() }
            .thenReturn(zoneId)

        val objects = listOf(
            createObjectWrapperWithIdAndCreatedDate(
                id = "idObject1",
                timestamp = timestamp
            ),
//            createObjectWrapperWithIdAndCreatedDate(
//                id = "idObject5",
//                timestamp = timestamp + 15
//            ), // Today
//            createObjectWrapperWithIdAndCreatedDate(
//                id = "idObject2",
//                timestamp = timestamp - TimeUnit.DAYS.toSeconds(1)
//            ), // Yesterday
//            createObjectWrapperWithIdAndCreatedDate(
//                id = "idObject3",
//                timestamp = timestamp - TimeUnit.DAYS.toSeconds(5)
//            ), // Previous 7 Days
//            createObjectWrapperWithIdAndCreatedDate(
//                id = "idObject4",
//                timestamp = timestamp - TimeUnit.DAYS.toSeconds(10)
//            ), // Previous 14 Days
        )

        val pageTypeMap = mapOf(
            Relations.ID to ObjectTypeIds.PAGE,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
            Relations.NAME to MockDataFactory.randomString()
        )

        stubStoreOfObjectTypes(ObjectTypeIds.PAGE, pageTypeMap)

        val subscriptionParams = createSubscriptionParams(
            spaceId = vmParams.spaceId.id,
            activeTab = AllContentTab.PAGES,
            activeSort = AllContentSort.ByDateCreated(),
            limitedObjectIds = emptyList(),
            limit = AllContentViewModel.DEFAULT_SEARCH_LIMIT,
            subscriptionId = "all_content_subscription_${vmParams.spaceId.id}"
        )

        stubSearch(
            storeSearchParams = subscriptionParams,
            objects = objects
        )

        restoreAllContentState.stub {
            onBlocking { run(RestoreAllContentState.Params(spaceId = vmParams.spaceId)) } doReturn RestoreAllContentState.Response(
                activeSort = Relations.CREATED_DATE
            )
        }

        val viewModel = buildViewModel()

        val expectedItems = listOf(
            UiContentItem.Group.Today(
                id = TODAY_ID
            ),
            UiContentItem.Item(
                id = "idObject1",
                name = "name-idObject1",
                createdDate = timestamp,
                lastModifiedDate = 0,
                space = vmParams.spaceId,
                type = ObjectTypeIds.PAGE,
                typeName = pageTypeMap[Relations.NAME] as String,
                layout = ObjectType.Layout.BASIC,
                icon = ObjectIcon.Profile.Avatar(name = "name-idObject1")
            ),
//            UiContentItem.Item(
//                id = "idObject5",
//                name = "name-idObject5",
//                createdDate = timestamp + 15,
//                lastModifiedDate = 0,
//                space = vmParams.spaceId,
//                type = ObjectTypeIds.PAGE,
//                typeName = pageTypeMap[Relations.NAME] as String,
//                layout = ObjectType.Layout.BASIC,
//                icon = ObjectIcon.Profile.Avatar(name = "name-idObject5")
//            ),

//            UiContentItem.Group.Yesterday(
//                id = YESTERDAY_ID
//            ),
//            UiContentItem.Item(
//                id = "idObject2",
//                name = "name-idObject2",
//                space = vmParams.spaceId,
//                type = ObjectTypeIds.PAGE,
//                typeName = pageTypeMap[Relations.NAME] as String,
//                layout = ObjectType.Layout.BASIC,
//                icon = ObjectIcon.Profile.Avatar(name = "name-idObject2"),
//                createdDate = timestamp - TimeUnit.DAYS.toSeconds(1)
//
//            ),
//            UiContentItem.Group.Previous7Days(
//                id = PREVIOUS_7_DAYS_ID
//            ),
//            UiContentItem.Item(
//                id = "idObject3",
//                name = "name-idObject3",
//                space = vmParams.spaceId,
//                type = ObjectTypeIds.PAGE,
//                typeName = pageTypeMap[Relations.NAME] as String,
//                layout = ObjectType.Layout.BASIC,
//                icon = ObjectIcon.Profile.Avatar(name = "name-idObject3"),
//                createdDate = timestamp - TimeUnit.DAYS.toSeconds(5)
//
//            ),
//            UiContentItem.Group.Previous14Days(
//                id = PREVIOUS_14_DAYS_ID
//            ),
//            UiContentItem.Item(
//                id = "idObject4",
//                name = "name-idObject4",
//                space = vmParams.spaceId,
//                type = ObjectTypeIds.PAGE,
//                typeName = pageTypeMap[Relations.NAME] as String,
//                layout = ObjectType.Layout.BASIC,
//                icon = ObjectIcon.Profile.Avatar(name = "name-idObject4"),
//                createdDate = timestamp - TimeUnit.DAYS.toSeconds(10)
//            )
        )

        viewModel.uiState.test {
            assertEquals(
                expected = UiContentState.Hidden,
                actual = awaitItem()
            )
            assertEquals(
                expected = UiContentState.Loading,
                actual = awaitItem()
            )
            val latest = awaitItem()
            assertEquals(
                expected = UiContentState.Content(
                    items = expectedItems
                ),
                actual = latest,
                message = "Expected same items"
            )
        }
        //}
    }

    private fun buildViewModel(): AllContentViewModel {
        return AllContentViewModel(
            analytics = analytics,
            localeProvider = localeProvider,
            vmParams = vmParams,
            urlBuilder = urlBuilder,
            storeOfRelations = storeOfRelations,
            searchObjects = objectSearch,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            restoreAllContentState = restoreAllContentState,
            updateAllContentState = mock(),
            storeOfObjectTypes = storeOfObjectTypes,
            storelessSubscriptionContainer = storelessSubscriptionContainer

        )
    }

    private fun stubLocale(locale: Locale) {
        localeProvider.stub {
            on { locale() } doReturn locale
        }
    }

    private fun createObjectWrapperWithIdAndCreatedDate(
        id: String,
        timestamp: Long
    ): ObjectWrapper.Basic {
        val name = "name-$id"
        return ObjectWrapper.Basic(
            map = mapOf(
                Relations.ID to id,
                Relations.CREATED_DATE to timestamp,
                Relations.NAME to name,
                Relations.TYPE to listOf(ObjectTypeIds.PAGE),
                Relations.SPACE_ID to vmParams.spaceId.id
            )
        )
    }

    suspend fun stubSearch(
        storeSearchParams: StoreSearchParams,
        objects: List<ObjectWrapper.Basic>,
        dependencies: List<ObjectWrapper.Basic> = listOf()
    ) {
        doReturn(
            SearchResult(
                results = objects,
                dependencies = dependencies
            )
        ).`when`(repo).searchObjectsWithSubscription(
            offset = 0,
            filters = storeSearchParams.filters,
            sorts = storeSearchParams.sorts,
            keys = storeSearchParams.keys,
            limit = storeSearchParams.limit,
            subscription = storeSearchParams.subscription,
            afterId = null,
            beforeId = null,
            source = storeSearchParams.source,
            ignoreWorkspace = null,
            noDepSubscription = true,
            collection = storeSearchParams.collection
        )
        eventChannel.stub {
            on {
                subscribe(listOf(storeSearchParams.subscription))
            } doReturn flow {
                emit(listOf())
            }
        }
    }

    suspend fun stubStoreOfObjectTypes(id: String = "", map: Map<String, Any?> = emptyMap()) {
        storeOfObjectTypes.set(id, map)
    }
}
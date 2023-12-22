package com.anytypeio.anytype.presentation.collections

import android.util.Log
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertIs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionCreateAndAddObjectTest: ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectCollection: MockCollection

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(false)
        .showTimestamp(false)
        .onlyLogWhenTestFails(false)
        .build()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockObjectCollection = MockCollection(context = root)
        repo = mock(verboseLogging = true)
        dispatchers = AppCoroutineDispatchers(
            io = rule.dispatcher,
            computation = rule.dispatcher,
            main = rule.dispatcher
        )
        spaceConfig = StubConfig()
        spaceManager = SpaceManager.Impl(
            repo = repo,
            dispatchers = dispatchers,
            configStorage = ConfigStorage.CacheStorage(),
            logger = mock()
        )
        dataViewSubscriptionContainer = DataViewSubscriptionContainer(
            repo = repo,
            channel = subscriptionEventChannel,
            store = objectStore,
            dispatchers = dispatchers
        )
        dataViewSubscription = DefaultDataViewSubscription(dataViewSubscriptionContainer)
        storeOfObjectTypes = DefaultStoreOfObjectTypes()
        viewModel = ObjectSetViewModel(
            openObjectSet = openObjectSet,
            closeBlock = closeBlock,
            updateText = updateText,
            interceptEvents = interceptEvents,
            interceptThreadStatus = interceptThreadStatus,
            createDataViewObject = CreateDataViewObject(
                repo = repo,
                spaceManager = spaceManager,
                dispatchers = dispatchers,
                storeOfRelations = storeOfRelations
            ),
            dispatcher = dispatcher,
            delegator = delegator,
            coverImageHashProvider = coverImageHashProvider,
            urlBuilder = urlBuilder,
            session = session,
            analytics = analytics,
            downloadUnsplashImage = downloadUnsplashImage,
            setDocCoverImage = setDocCoverImage,
            createObject = createObject,
            setObjectDetails = setObjectDetails,
            paginator = paginator,
            cancelSearchSubscription = cancelSearchSubscription,
            database = database,
            dataViewSubscriptionContainer = dataViewSubscriptionContainer,
            storeOfRelations = storeOfRelations,
            stateReducer = stateReducer,
            dataViewSubscription = dataViewSubscription,
            objectStore = objectStore,
            addObjectToCollection = addObjectToCollection,
            objectToCollection = objectToCollection,
            setQueryToObjectSet = setQueryToObjectSet,
            storeOfObjectTypes = storeOfObjectTypes,
            templatesContainer = templatesContainer,
            setObjectListIsArchived = setObjectListIsArchived,
            duplicateObjects = duplicateObjects,
            viewerDelegate = viewerDelegate,
            spaceManager = spaceManager,
            createTemplate = createTemplate,
            getObjectTypes = getObjectTypes,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            dispatchers = dispatchers,
            getNetworkMode = getNetworkMode
        )
        stubNetworkMode()
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `create pre-populated record in Collection`() = runTest {
        // SETUP

        val filters = mockObjectCollection.filters

        stubSpaceManager(mockObjectCollection.spaceId)
        stubInterceptEvents()

        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(
                mockObjectCollection.header,
                mockObjectCollection.title,
                mockObjectCollection.dataViewWithFilters
            ),
            details = mockObjectCollection.details
        )
        stubStoreOfRelations(mockObjectCollection)
        stubSubscriptionResults(
            subscription = mockObjectCollection.subscriptionId,
            spaceId = mockObjectCollection.spaceId,
            collection = root,
            storeOfRelations = storeOfRelations,
            keys = mockObjectCollection.dvKeys,
            objects = listOf(mockObjectCollection.obj1, mockObjectCollection.obj2),
            dvSorts = mockObjectCollection.sorts,
            dvFilters = mockObjectCollection.filters,
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Collection.Default>(second)

            val newObjectTypeKey = MockDataFactory.randomString()
            val newObjectTemplate = MockDataFactory.randomString()
            viewModel.proceedWithDataViewObjectCreate(
                typeChosenBy = TypeKey(newObjectTypeKey),
                templateId = newObjectTemplate
            )

            advanceUntilIdle()
            val spaceId = SpaceId(mockObjectCollection.spaceId)
            val command = Command.CreateObject(
                prefilled = mapOf(filters[0].relation to filters[0].value, filters[1].relation to filters[1].value),
                internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
                space = spaceId,
                typeKey = TypeKey(newObjectTypeKey),
                template = newObjectTemplate
            )
            verifyBlocking(repo, times(1)) {
                createObject(command)
            }
        }
    }
}
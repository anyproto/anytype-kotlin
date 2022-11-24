package com.anytypeio.anytype.presentation.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.auth.interactor.GetProfile
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.DebugSettings
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.config.GetDebugSettings
import com.anytypeio.anytype.domain.dashboard.interactor.CloseDashboard
import com.anytypeio.anytype.domain.dashboard.interactor.OpenDashboard
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.page.CreateNewObject
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

open class DashboardTestSetup {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var getProfile: GetProfile

    @Mock
    lateinit var openDashboard: OpenDashboard

    @Mock
    lateinit var getConfig: GetConfig

    @Mock
    lateinit var closeDashboard: CloseDashboard

    @Mock
    lateinit var deleteObjects: DeleteObjects

    @Mock
    lateinit var setObjectListIsArchived: SetObjectListIsArchived

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var getDebugSettings: GetDebugSettings

    @Mock
    lateinit var searchObjects: SearchObjects

    @Mock
    lateinit var move: Move

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var objectTypesProvider: ObjectTypesProvider

    @Mock
    lateinit var cancelSearchSubscription: CancelSearchSubscription

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var subscriptionEventChannel: SubscriptionEventChannel

    @Mock
    lateinit var getTemplates: GetTemplates

    @Mock
    lateinit var createNewObject: CreateNewObject

    private lateinit var objectStore: ObjectStore

    lateinit var vm: HomeDashboardViewModel

    protected val builder: UrlBuilder get() = UrlBuilder(gateway)

    val config = Config(
        home = MockDataFactory.randomUuid(),
        gateway = MockDataFactory.randomUuid(),
        profile = MockDataFactory.randomUuid()
    )

    protected val storeOfObjectTypes = DefaultStoreOfObjectTypes()

    fun buildViewModel() : HomeDashboardViewModel {
        objectStore = DefaultObjectStore()
        return HomeDashboardViewModel(
            getProfile = getProfile,
            openDashboard = openDashboard,
            closeDashboard = closeDashboard,
            getConfig = getConfig,
            move = move,
            interceptEvents = interceptEvents,
            eventConverter = HomeDashboardEventConverter.DefaultConverter(
                builder = builder,
                objectTypesProvider = objectTypesProvider
            ),
            getDebugSettings = getDebugSettings,
            analytics = analytics,
            searchObjects = searchObjects,
            urlBuilder = builder,
            setObjectListIsArchived = setObjectListIsArchived,
            deleteObjects = deleteObjects,
            cancelSearchSubscription = cancelSearchSubscription,
            objectStore = objectStore,
            objectSearchSubscriptionContainer = ObjectSearchSubscriptionContainer(
                repo = repo,
                channel = subscriptionEventChannel,
                store = objectStore,
                dispatchers = AppCoroutineDispatchers(
                    io = coroutineTestRule.testDispatcher,
                    computation = coroutineTestRule.testDispatcher,
                    main = coroutineTestRule.testDispatcher
                )
            ),
            createNewObject = createNewObject,
            featureToggles = mock(),
            storeOfObjectTypes = storeOfObjectTypes
        )
    }

    fun stubGetConfig(response: Either.Right<Config>) {
        getConfig.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Config>) -> Unit>(2)(response)
            }
        }
    }

    fun stubObserveEvents(
        flow: Flow<List<Event>> = flowOf(),
        params: InterceptEvents.Params? = null
    ) {
        interceptEvents.stub {
            onBlocking { build(params) } doReturn flow
        }
    }

    fun stubOpenDashboard(
        payload: Payload = Payload(
            context = MockDataFactory.randomString(),
            events = emptyList()
        )
    ) {
        openDashboard.stub {
            onBlocking { invoke(params = null) } doReturn Either.Right(payload)
        }
    }

    fun stubCloseDashboard() {
        closeDashboard.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(Either.Right(Unit))
            }
        }
    }

    fun stubGetEditorSettings() {
        getDebugSettings.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(DebugSettings(true))
        }
    }

    fun stubGetCurrentAccount(accountResponse: Either.Right<Account>) {
        getProfile.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Account>) -> Unit>(2)(accountResponse)
            }
        }
    }

    fun stubSearchObjects(
        params: SearchObjects.Params,
        objects: List<Map<String, Any?>> = emptyList()
    ) {
        searchObjects.stub {
            onBlocking { invoke(params) } doReturn Either.Right(objects.map { ObjectWrapper.Basic(it) })
        }
    }
}
package com.anytypeio.anytype.presentation.dashboard

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.auth.interactor.GetProfile
import com.anytypeio.anytype.domain.auth.model.Account
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.*
import com.anytypeio.anytype.domain.dashboard.interactor.*
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
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
    lateinit var createPage: CreatePage

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var getDebugSettings: GetDebugSettings

    @Mock
    lateinit var searchArchivedObjects: SearchArchivedObjects

    @Mock
    lateinit var searchObjectSets: SearchObjectSets

    @Mock
    lateinit var searchRecentObjects: SearchRecentObjects

    @Mock
    lateinit var searchInboxObjects: SearchInboxObjects

    @Mock
    lateinit var move: Move

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var getFlavourConfig: GetFlavourConfig

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var objectTypesProvider: ObjectTypesProvider

    lateinit var vm: HomeDashboardViewModel

    val builder: UrlBuilder get() = UrlBuilder(gateway)

    val config = Config(
        home = MockDataFactory.randomUuid(),
        gateway = MockDataFactory.randomUuid(),
        profile = MockDataFactory.randomUuid()
    )

    fun buildViewModel() = HomeDashboardViewModel(
        getProfile = getProfile,
        openDashboard = openDashboard,
        closeDashboard = closeDashboard,
        createPage = createPage,
        getConfig = getConfig,
        move = move,
        interceptEvents = interceptEvents,
        eventConverter = HomeDashboardEventConverter.DefaultConverter(
            builder = builder,
            getFlavourConfig = getFlavourConfig,
            objectTypesProvider = objectTypesProvider
        ),
        getDebugSettings = getDebugSettings,
        analytics = analytics,
        searchArchivedObjects = searchArchivedObjects,
        searchRecentObjects = searchRecentObjects,
        searchInboxObjects = searchInboxObjects,
        searchObjectSets = searchObjectSets,
        getFlavourConfig = getFlavourConfig,
        urlBuilder = builder
    )

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

    fun stubCreatePage(id: String) {
        createPage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, String>) -> Unit>(2)(Either.Right(id))
            }
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
}
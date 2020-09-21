package com.agileburo.anytype.presentation.home

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.auth.interactor.GetProfile
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.Move
import com.agileburo.anytype.domain.config.*
import com.agileburo.anytype.domain.dashboard.interactor.CloseDashboard
import com.agileburo.anytype.domain.dashboard.interactor.OpenDashboard
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.event.model.Payload
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.CreatePage
import com.agileburo.anytype.presentation.desktop.HomeDashboardEventConverter
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.mockito.Mock

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
    lateinit var move: Move

    @Mock
    lateinit var gateway: Gateway

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
        eventConverter = HomeDashboardEventConverter.DefaultConverter(builder),
        getDebugSettings = getDebugSettings
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
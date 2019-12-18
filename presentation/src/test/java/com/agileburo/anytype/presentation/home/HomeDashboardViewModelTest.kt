package com.agileburo.anytype.presentation.home

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.model.Image
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.config.Config
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.dashboard.interactor.CloseDashboard
import com.agileburo.anytype.domain.dashboard.interactor.ObserveHomeDashboard
import com.agileburo.anytype.domain.dashboard.interactor.OpenDashboard
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.image.LoadImage
import com.agileburo.anytype.domain.page.CreatePage
import com.agileburo.anytype.presentation.desktop.DashboardView
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel.ViewState
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.profile.ProfileView
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class HomeDashboardViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var loadImage: LoadImage

    @Mock
    lateinit var getCurrentAccount: GetCurrentAccount

    @Mock
    lateinit var openDashboard: OpenDashboard

    @Mock
    lateinit var observeHomeDashboard: ObserveHomeDashboard

    @Mock
    lateinit var getConfig: GetConfig

    @Mock
    lateinit var closeDashboard: CloseDashboard

    @Mock
    lateinit var createPage: CreatePage

    private lateinit var vm: HomeDashboardViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    private fun buildViewModel(): HomeDashboardViewModel {
        return HomeDashboardViewModel(
            loadImage = loadImage,
            getCurrentAccount = getCurrentAccount,
            openDashboard = openDashboard,
            closeDashboard = closeDashboard,
            createPage = createPage,
            observeHomeDashboard = observeHomeDashboard,
            getConfig = getConfig
        )
    }

    @Test
    fun `should only start getting config when view model is initialized`() = runBlockingTest {

        val config = Config(homeDashboardId = MockDataFactory.randomUuid())
        val response = Either.Right(config)

        stubGetConfig(response)
        stubObserveHomeDashboard()

        vm = buildViewModel()

        verify(getConfig, times(1)).invoke(any(), any(), any())
        verifyZeroInteractions(openDashboard)
        verifyZeroInteractions(loadImage)
        verifyZeroInteractions(getCurrentAccount)
    }

    @Test
    fun `should start observing home dashboard after receiving config`() = runBlockingTest {

        val config = Config(homeDashboardId = MockDataFactory.randomUuid())
        val response = Either.Right(config)
        val param = ObserveHomeDashboard.Param(id = config.homeDashboardId)

        stubGetConfig(response)
        stubObserveHomeDashboard()

        vm = buildViewModel()

        verify(getConfig, times(1)).invoke(any(), any(), any())
        verify(observeHomeDashboard, times(1)).build(param)
        verifyZeroInteractions(openDashboard)
        verifyZeroInteractions(loadImage)
        verifyZeroInteractions(getCurrentAccount)
    }

    @Test
    fun `should update view state as soon as blocks are received`() {

        val config = Config(homeDashboardId = MockDataFactory.randomUuid())

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            )
        )

        val dashboard = HomeDashboard(
            id = config.homeDashboardId,
            fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
            type = Block.Content.Dashboard.Type.MAIN_SCREEN,
            blocks = listOf(page),
            children = listOf(page.id)
        )

        stubGetConfig(Either.Right(config))
        stubObserveHomeDashboard(flowOf(dashboard))

        vm = buildViewModel()

        val expected = ViewState.Success(
            data = listOf(
                DashboardView.Document(
                    id = page.id,
                    title = page.fields.name
                )
            )
        )

        assertEquals(actual = vm.state.value, expected = expected)
    }

    @Test
    fun `should proceed with getting account and opening dashboard when view is created`() {

        val config = Config(homeDashboardId = MockDataFactory.randomUuid())
        stubGetConfig(Either.Right(config))
        stubObserveHomeDashboard()

        vm = buildViewModel()
        vm.onViewCreated()

        verify(getCurrentAccount, times(1)).invoke(any(), any(), any())
        verify(openDashboard, times(1)).invoke(any(), eq(null), any())
    }

    @Test
    fun `should update view state as soon as current account is received`() {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        val response = Either.Right(account)

        stubObserveHomeDashboard()

        getCurrentAccount.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Account>) -> Unit>(2)(response)
            }
        }

        vm = buildViewModel()

        vm.onViewCreated()

        val expected = ProfileView(name = account.name)

        assertEquals(actual = vm.profile.value, expected = expected)
    }

    @Test
    fun `should fetch avatar image and update view state when account is ready`() {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = Image(
                id = MockDataFactory.randomString(),
                sizes = listOf(Image.Size.SMALL)
            ),
            color = null
        )

        val blob = ByteArray(0)

        val accountResponse = Either.Right(account)
        val imageResponse = Either.Right(blob)

        stubObserveHomeDashboard()

        getCurrentAccount.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Account>) -> Unit>(2)(accountResponse)
            }
        }

        loadImage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, ByteArray>) -> Unit>(2)(imageResponse)
            }
        }

        vm = buildViewModel()

        vm.onViewCreated()

        verify(getCurrentAccount, times(1)).invoke(any(), any(), any())
        verify(loadImage, times(1)).invoke(any(), any(), any())

        vm.image.test()
            .assertHasValue()
            .assertValue(blob)
    }

    @Test
    fun `should not fetch the avatar image if given account does not have it`() {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        val accountResponse = Either.Right(account)

        stubObserveHomeDashboard()

        getCurrentAccount.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Account>) -> Unit>(2)(accountResponse)
            }
        }

        vm = buildViewModel()

        vm.onViewCreated()

        verify(getCurrentAccount, times(1)).invoke(any(), any(), any())
        verify(loadImage, never()).invoke(any(), any(), any())
    }

    @Test
    fun `should start creating page when requested from UI`() {

        stubObserveHomeDashboard()

        vm = buildViewModel()

        vm.onAddNewDocumentClicked()

        verify(createPage, times(1)).invoke(any(), any(), any())
    }

    @Test
    fun `should close dashboard and navigate to page screen when page is created`() {

        val id = MockDataFactory.randomUuid()

        stubObserveHomeDashboard()

        closeDashboard.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(Either.Right(Unit))
            }
        }

        createPage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, String>) -> Unit>(2)(Either.Right(id))
            }
        }

        vm = buildViewModel()

        vm.onAddNewDocumentClicked()

        vm.navigation
            .test()
            .assertHasValue()
            .assertValue { value ->
                (value.peekContent() as AppNavigation.Command.OpenPage).id == id
            }
    }

    private fun stubObserveHomeDashboard(
        flow: Flow<HomeDashboard> = flowOf()
    ) {
        observeHomeDashboard.stub {
            onBlocking { build(any()) } doReturn flow
        }
    }

    private fun stubGetConfig(response: Either.Right<Config>) {
        getConfig.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Config>) -> Unit>(2)(response)
            }
        }
    }
}
package com.agileburo.anytype.presentation.home

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.model.Image
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.ObserveDashboardBlocks
import com.agileburo.anytype.domain.block.interactor.OpenDashboard
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.image.LoadImage
import com.agileburo.anytype.presentation.desktop.DashboardView
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModel.ViewState
import com.agileburo.anytype.presentation.profile.ProfileView
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
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
    lateinit var observeDashboardBlocks: ObserveDashboardBlocks

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
            observeDashboardBlocks = observeDashboardBlocks
        )
    }

    @Test
    fun `should only start observing blocks when view model is initialized`() = runBlockingTest {

        observeDashboardBlocks.stub {
            onBlocking { build() } doReturn flowOf()
        }

        vm = buildViewModel()

        verify(observeDashboardBlocks, times(1)).build(eq(null))
        verifyZeroInteractions(openDashboard)
        verifyZeroInteractions(loadImage)
        verifyZeroInteractions(getCurrentAccount)
    }

    @Test
    fun `should update view state as soon as blocks are received`() = runBlockingTest {

        val block = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(MockDataFactory.randomUuid()),
            fields = Block.Fields(
                map = mapOf("name" to MockDataFactory.randomString())
            ),
            content = Block.Content.Dashboard(
                type = Block.Content.Dashboard.Type.MAIN_SCREEN
            )
        )

        val blocks = listOf(block)

        val flow = flowOf(blocks)

        observeDashboardBlocks.stub {
            onBlocking { build() } doReturn flow
        }

        vm = buildViewModel()

        val expected = ViewState.Success(
            data = listOf(
                DashboardView.Document(
                    id = block.id,
                    title = block.fields.name
                )
            )
        )

        assertEquals(actual = vm.state.value, expected = expected)
    }

    @Test
    fun `should proceed with getting account and opening dashboard when view is created`() {

        observeDashboardBlocks.stub {
            onBlocking { build() } doReturn flowOf()
        }

        vm = buildViewModel()
        vm.onViewCreated()

        verify(getCurrentAccount, times(1)).invoke(any(), any(), any())
        verify(openDashboard, times(1)).invoke(any(), any(), any())
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

        observeDashboardBlocks.stub {
            onBlocking { build() } doReturn flowOf()
        }

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

        observeDashboardBlocks.stub {
            onBlocking { build() } doReturn flowOf()
        }

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

        observeDashboardBlocks.stub {
            onBlocking { build() } doReturn flowOf()
        }

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
}
package com.agileburo.anytype.presentation.page

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.ObservePage
import com.agileburo.anytype.domain.page.OpenPage
import com.agileburo.anytype.presentation.navigation.AppNavigation
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

class PageViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var openPage: OpenPage

    @Mock
    lateinit var closePage: ClosePage

    @Mock
    lateinit var observePage: ObservePage

    lateinit var vm: PageViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should start observing page when view model is initialized`() = runBlockingTest {

        observePage.stub {
            onBlocking { build() } doReturn flowOf(emptyList())
        }

        buildViewModel()

        verify(observePage, times(1)).build(eq(null))
    }

    @Test
    fun `should start opening page when view model is initialized`() = runBlockingTest {

        observePage.stub {
            onBlocking { build() } doReturn flowOf(emptyList())
        }

        buildViewModel()

        verify(openPage, times(1)).invoke(any(), any(), any())
    }

    @Test
    fun `should close page when the system back button is pressed`() = runBlockingTest {

        observePage.stub {
            onBlocking { build() } doReturn flowOf(emptyList())
        }

        buildViewModel()

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed()

        verify(closePage, times(1)).invoke(any(), any(), any())
    }

    @Test
    fun `should emit an approprtiate navigation command when the page is closed`() {

        observePage.stub {
            onBlocking { build() } doReturn flowOf(emptyList())
        }

        val response = Either.Right(Unit)

        closePage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(response)
            }
        }

        buildViewModel()

        val testObserver = vm.navigation.test()

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed()

        testObserver
            .assertHasValue()
            .assertValue { value -> value.peekContent() == AppNavigation.Command.Exit }
    }

    @Test
    fun `should not emit any navigation command if there is an error while closing the page`() =
        runBlockingTest {

            observePage.stub {
                onBlocking { build() } doReturn flowOf(emptyList())
            }

            val error = Exception("Error while closing this page")

            val response = Either.Left(error)

            closePage.stub {
                onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                    answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(response)
                }
            }

            buildViewModel()

            val testObserver = vm.navigation.test()

            verifyZeroInteractions(closePage)

            vm.onSystemBackPressed()

            testObserver.assertNoValue()
        }

    private fun buildViewModel() {
        vm = PageViewModel(
            openPage = openPage,
            closePage = closePage,
            observePage = observePage
        )
    }
}
package com.anytypeio.anytype.presentation.keychain

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.domain.auth.interactor.GetMnemonic
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*


class KeychainPhraseViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    lateinit var getMnemonic: GetMnemonic

    @Mock
    lateinit var analytics: Analytics

    lateinit var vm: KeychainPhraseViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should proceed with getting mnemonic when vm is created`() {
        vm = buildViewModel()
        verify(getMnemonic, times(1)).invoke(any(), any(), any())
    }

    @Test
    fun `should emit mnemonic when it is received`() {

        val mnemonic = MockDataFactory.randomString()

        getMnemonic.stub {
            on { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, String>) -> Unit>(2)(Either.Right(mnemonic))
            }
        }

        vm = buildViewModel()

        vm.state.test().assertValue(ViewState.Success(mnemonic))
    }

    @Test
    fun `should emit nothing when error occurs`() {

        val exception = Exception()

        getMnemonic.stub {
            on { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, String>) -> Unit>(2)(Either.Left(exception))
            }
        }

        vm = buildViewModel()

        vm.state.test().assertNoValue()
    }

    private fun buildViewModel() = KeychainPhraseViewModel(getMnemonic = getMnemonic, analytics = analytics)
}
package com.anytypeio.anytype.presentation.keychain

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.GetMnemonic
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify


class KeychainPhraseViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

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
        vm = givenViewModel()
        verify(getMnemonic, times(1)).invoke(any(), any(), any())
    }

    @Test
    fun `should emit blurred - when started`() {
        givenMnemonic()

        vm = givenViewModel()

        vm.state.test().assertValue(KeychainViewState.Blurred)
    }

    @Test
    fun `should emit displayed - when keychain clicked`() {
        val mnemonic = givenMnemonic()

        vm = givenViewModel()
        vm.onKeychainClicked()

        vm.state.test().assertValue(KeychainViewState.Displayed(mnemonic))
    }

    @Test
    fun `should emit blurred - when root clicked`() {
        givenMnemonic()

        vm = givenViewModel()
        vm.onRootClicked()

        vm.state.test().assertValue(KeychainViewState.Blurred)
    }

    private fun givenMnemonic(): String {
        val mnemonic = MockDataFactory.randomString()

        getMnemonic.stub {
            on { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, String>) -> Unit>(2)(Either.Right(mnemonic))
            }
        }
        return mnemonic
    }

    private fun givenViewModel() = KeychainPhraseViewModel(
        getMnemonic = getMnemonic,
        analytics = analytics
    )
}
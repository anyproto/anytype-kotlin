package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.auth.model.Wallet
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class SetupWalletTest {

    @get:Rule
    var rule = DefaultCoroutineTestRule()

    @Mock
    lateinit var repo: AuthRepository

    lateinit var dispatchers: AppCoroutineDispatchers

    lateinit var setupWallet: SetupWallet

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        dispatchers = AppCoroutineDispatchers(
            io = rule.dispatcher,
            computation = rule.dispatcher,
            main = rule.dispatcher
        )
        setupWallet = SetupWallet(repo, dispatchers)
    }

    @Test
    fun `should successfully create wallet and successfully save mnemonic`() = runBlocking {

        val path = "path"
        val mnemonic = "mnemonic"

        val wallet = Wallet(
            mnemonic = mnemonic
        )

        val param = SetupWallet.Params(path)

        repo.stub {
            onBlocking { createWallet(path) } doReturn wallet
        }

        repo.stub {
            onBlocking { saveMnemonic(mnemonic) } doReturn Unit
        }

        setupWallet.run(param)

        verify(repo, times(1)).createWallet(path)
        verify(repo, times(1)).saveMnemonic(mnemonic)
        verifyNoMoreInteractions(repo)
    }
}
package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.auth.model.Wallet
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SetupWalletTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: AuthRepository

    lateinit var setupWallet: SetupWallet

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        setupWallet = SetupWallet(repo)
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
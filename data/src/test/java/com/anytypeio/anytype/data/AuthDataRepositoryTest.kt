package com.anytypeio.anytype.data

import com.anytypeio.anytype.core_models.StubAccount
import com.anytypeio.anytype.core_models.StubAccountSetup
import com.anytypeio.anytype.core_models.StubFeatureConfig
import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.data.auth.model.WalletEntity
import com.anytypeio.anytype.data.auth.repo.AuthCache
import com.anytypeio.anytype.data.auth.repo.AuthCacheDataStore
import com.anytypeio.anytype.data.auth.repo.AuthDataRepository
import com.anytypeio.anytype.data.auth.repo.AuthDataStoreFactory
import com.anytypeio.anytype.data.auth.repo.AuthRemote
import com.anytypeio.anytype.data.auth.repo.AuthRemoteDataStore
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions

class AuthDataRepositoryTest {

    @Mock
    lateinit var authRemote: AuthRemote

    @Mock
    lateinit var authCache: AuthCache

    lateinit var repo: AuthDataRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repo = AuthDataRepository(
            factory = AuthDataStoreFactory(
                cache = AuthCacheDataStore(
                    cache = authCache
                ),
                remote = AuthRemoteDataStore(
                    authRemote = authRemote
                )
            )
        )
    }

    @Test
    fun `should call only remote in order to select account`() = runBlocking {

        val id = MockDataFactory.randomUuid()

        val path = MockDataFactory.randomString()

        val account = StubAccount()

        val features = StubFeatureConfig()

        authRemote.stub {
            onBlocking { selectAccount(id = id, path = path) } doReturn StubAccountSetup(
                account = account,
                features = features
            )
        }

        repo.selectAccount(
            id = id,
            path = path
        )

        verifyNoInteractions(authCache)

        verify(authRemote, times(1)).selectAccount(
            id = id,
            path = path
        )

        verifyNoMoreInteractions(authRemote)
    }

    @Test
    fun `should call only remote in order to create an account`() = runBlocking {

        val path = MockDataFactory.randomString()

        val name = MockDataFactory.randomString()

        val setup = StubAccountSetup()

        val icon = MockDataFactory.randomInt()

        authRemote.stub {
            onBlocking {
                createAccount(
                    name = name,
                    avatarPath = path,
                    invitationCode = "code",
                    iconGradientValue = icon
                )
            } doReturn setup
        }

        repo.createAccount(
            name = name,
            avatarPath = path,
            invitationCode = "code",
            icon = icon
        )

        verifyNoInteractions(authCache)

        verify(authRemote, times(1)).createAccount(
            name = name,
            avatarPath = path,
            invitationCode = "code",
            iconGradientValue = icon
        )

        verifyNoMoreInteractions(authRemote)
    }

    @Test
    fun `should call only remote in order to recover accounts`() = runBlocking {

        authRemote.stub {
            onBlocking { recoverAccount() } doReturn Unit
        }

        repo.startLoadingAccounts()

        verifyNoInteractions(authCache)
        verify(authRemote, times(1)).recoverAccount()
        verifyNoMoreInteractions(authRemote)
    }

    @Test
    fun `should call only cache in order to save account`() = runBlocking {

        val account = StubAccount()

        authCache.stub {
            onBlocking { saveAccount(any()) } doReturn Unit
        }

        repo.saveAccount(account)

        verify(authCache, times(1)).saveAccount(any())
        verifyNoMoreInteractions(authCache)
        verifyNoInteractions(authRemote)
    }

    @Test
    fun `should call only remote in order to create wallet`() = runBlocking {

        val path = MockDataFactory.randomString()

        val wallet = WalletEntity(
            mnemonic = MockDataFactory.randomString()
        )

        authRemote.stub {
            onBlocking { createWallet(path) } doReturn wallet
        }

        repo.createWallet(path)

        verifyNoInteractions(authCache)
        verify(authRemote, times(1)).createWallet(path)
        verifyNoMoreInteractions(authRemote)
    }

    @Test
    fun `should call only cache in order to get current account`() = runBlocking {

        val account = AccountEntity(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            color = null
        )

        authCache.stub {
            onBlocking { getCurrentAccount() } doReturn account
        }

        repo.getCurrentAccount()

        verifyNoInteractions(authRemote)
        verify(authCache, times(1)).getCurrentAccount()
        verifyNoMoreInteractions(authCache)
    }

    @Test
    fun `should call only cache in order to save mnemonic`() = runBlocking {

        val mnemonic = MockDataFactory.randomString()

        authCache.stub {
            onBlocking { saveMnemonic(mnemonic) } doReturn Unit
        }

        repo.saveMnemonic(mnemonic)

        verifyNoInteractions(authRemote)
        verify(authCache, times(1)).saveMnemonic(mnemonic)
        verifyNoMoreInteractions(authCache)
    }

    @Test
    fun `should call only cache in order to get mnemonic`() = runBlocking {

        val mnemonic = MockDataFactory.randomString()

        authCache.stub {
            onBlocking { getMnemonic() } doReturn mnemonic
        }

        repo.getMnemonic()

        verifyNoInteractions(authRemote)
        verify(authCache, times(1)).getMnemonic()
        verifyNoMoreInteractions(authCache)
    }

    @Test
    fun `should call cache and remote in order to logout`() = runBlocking {

        authCache.stub {
            onBlocking { logout() } doReturn Unit
        }

        repo.logout(false)

        verify(authCache, times(1)).logout()
        verifyNoMoreInteractions(authCache)
        verify(authRemote, times(1)).logout(false)
        verifyNoMoreInteractions(authRemote)
    }

    @Test
    fun `should not call logout on cache if remote logout is not succeeded`() {

        authRemote.stub {
            onBlocking { logout(false) } doThrow IllegalStateException()
        }

        runBlocking {
            try {
                repo.logout(false)
            } catch (e: Exception) {
                verify(authRemote, times(1)).logout(false)
                verifyNoInteractions(authCache)
            }
        }
    }

    @Test
    fun `should call only cache in order to get available accounts`() = runBlocking {

        val account = AccountEntity(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            color = null
        )

        val accounts = listOf(account)

        authCache.stub {
            onBlocking { getAccounts() } doReturn accounts
        }

        repo.getAccounts()

        verifyNoInteractions(authRemote)
        verify(authCache, times(1)).getAccounts()
        verifyNoMoreInteractions(authCache)
    }
}
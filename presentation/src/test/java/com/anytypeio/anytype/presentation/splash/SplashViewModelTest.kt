package com.anytypeio.anytype.presentation.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.GetLastOpenedObject
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class SplashViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var checkAuthorizationStatus: CheckAuthorizationStatus

    @Mock
    lateinit var launchAccount: LaunchAccount

    @Mock
    lateinit var launchWallet: LaunchWallet

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var auth: AuthRepository

    private lateinit var getLastOpenedObject: GetLastOpenedObject

    @Mock
    lateinit var createObject: CreateObject

    @Mock
    lateinit var appActionManager: AppActionManager

    @Mock
    private lateinit var getDefaultPageType: GetDefaultPageType

    @Mock
    private lateinit var relationsSubscriptionManager: RelationsSubscriptionManager

    @Mock
    private lateinit var objectTypesSubscriptionManager: ObjectTypesSubscriptionManager

    lateinit var vm: SplashViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getLastOpenedObject = GetLastOpenedObject(
            authRepo = auth,
            blockRepo = repo
        )
    }

    private fun initViewModel() {
        vm = SplashViewModel(
            checkAuthorizationStatus = checkAuthorizationStatus,
            launchAccount = launchAccount,
            launchWallet = launchWallet,
            analytics = analytics,
            getLastOpenedObject = getLastOpenedObject,
            getDefaultPageType = getDefaultPageType,
            createObject = createObject,
            appActionManager = appActionManager,
            relationsSubscriptionManager = relationsSubscriptionManager,
            objectTypesSubscriptionManager = objectTypesSubscriptionManager
        )
    }

    @Test
    fun `should not execute use case when view model is created`() {

        val status = AuthStatus.AUTHORIZED
        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()

        initViewModel()

        runBlocking {
            verify(checkAuthorizationStatus, times(1)).invoke(any())
            verifyNoMoreInteractions(checkAuthorizationStatus)
        }
    }

    @Ignore("https://github.com/mockito/mockito-kotlin/issues/456")
    @Test
    fun `should invoke checkAuthorizationStatus when getDefaultPageType on error `() {
        val status = AuthStatus.AUTHORIZED
        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()
        getDefaultPageType.stub {
            onBlocking { execute(Unit) } doThrow Exception("error")
        }

        initViewModel()

        runBlocking {
            verify(getDefaultPageType, times(1)).execute(any())
            verify(checkAuthorizationStatus, times(1)).invoke(any())
        }
    }

    @Test
    fun `should invoke checkAuthorizationStatus when getDefaultPageType is object type `() {
        val status = AuthStatus.AUTHORIZED
        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()
        stubGetDefaultObjectType(type = ObjectTypeIds.PAGE)

        initViewModel()

        runBlocking {
            verify(getDefaultPageType, times(1)).execute(any())
            verify(checkAuthorizationStatus, times(1)).invoke(any())
        }
    }

    @Test
    fun `should not invoke checkAuthorizationStatus when getDefaultPageType is null`() {
        val status = AuthStatus.AUTHORIZED
        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()
        stubGetDefaultObjectType(type = null)

        runBlocking {
            verify(checkAuthorizationStatus, times(0)).invoke(any())
        }
    }

    @Test
    fun `should start launching wallet if user is authorized`() {

        val status = AuthStatus.AUTHORIZED

        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()
        stubGetDefaultObjectType(type = ObjectTypeIds.PAGE)

        initViewModel()

        runBlocking {
            verify(launchWallet, times(1)).invoke(any())
        }
    }

    @Test
    fun `should start launching account if wallet is launched`() {

        val status = AuthStatus.AUTHORIZED

        val response = Either.Right(status)

        stubCheckAuthStatus(response)
        stubLaunchWallet()
        stubLaunchAccount()
        stubGetLastOpenedObject()
        stubGetDefaultObjectType(type = ObjectTypeIds.PAGE)

        initViewModel()

        runBlocking {
            verify(launchWallet, times(1)).invoke(any())
            verify(launchAccount, times(1)).invoke(any())
        }
    }

    private fun stubCheckAuthStatus(response: Either.Right<AuthStatus>) {
        checkAuthorizationStatus.stub {
            onBlocking { invoke(eq(Unit)) } doReturn response
        }
    }

    private fun stubLaunchWallet(
        response: Either<Throwable, Unit> = Either.Right(Unit)
    ) {
        launchWallet.stub {
            onBlocking { invoke(any()) } doReturn response
        }
    }

    private fun stubLaunchAccount() {
        launchAccount.stub {
            onBlocking { invoke(any()) } doReturn Either.Right("accountId")
        }
    }

    private fun stubGetLastOpenedObject() {
        auth.stub {
            onBlocking {
                getLastOpenedObjectId()
            } doReturn null
        }
    }

    private fun stubGetDefaultObjectType(type: String? = null, name: String? = null) {
        getDefaultPageType.stub {
            onBlocking { execute(Unit) } doReturn Resultat.success(
                GetDefaultPageType.Response(
                    type,
                    name
                )
            )
        }
    }
}
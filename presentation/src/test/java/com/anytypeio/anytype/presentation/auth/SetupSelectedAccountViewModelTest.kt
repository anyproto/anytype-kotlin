package com.anytypeio.anytype.presentation.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.FeaturesConfig
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.domain.auth.interactor.SelectAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class SetupSelectedAccountViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var authRepo: AuthRepository

    @Mock
    lateinit var blockRepo: BlockRepository

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var pathProvider: PathProvider

    @Mock
    lateinit var featuresConfigProvider: FeaturesConfigProvider

    @Mock
    lateinit var configStorage: ConfigStorage

    @Mock
    lateinit var workspaceManager: WorkspaceManager

    @Mock
    lateinit var objectTypesProvider: ObjectTypesProvider

    @Mock
    private lateinit var relationsSubscriptionManager: RelationsSubscriptionManager

    @Mock
    private lateinit var objectTypesSubscriptionManager: ObjectTypesSubscriptionManager

    private lateinit var selectAccount: SelectAccount

    @Mock
    private lateinit var crashReporter: CrashReporter

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        selectAccount = SelectAccount(
            repository = authRepo,
            featuresConfigProvider = featuresConfigProvider,
            configStorage = configStorage,
            workspaceManager = workspaceManager
        )
    }

    @Test
    fun `should not show migration-in-progress msg by default`() {

        // SETUP

        val vm = buildViewModel()

        // TESTING

        assertEquals(
            expected = false,
            actual = vm.isMigrationInProgress.value
        )
        coroutineTestRule.advanceTime(SetupSelectedAccountViewModel.TIMEOUT_DURATION)
    }

    @Test
    fun `should show migration-in-progress msg after timeout`() {

        // SETUP

        val vm = buildViewModel()

        // TESTING

        assertEquals(
            expected = false,
            actual = vm.isMigrationInProgress.value
        )
        coroutineTestRule.advanceTime(SetupSelectedAccountViewModel.TIMEOUT_DURATION)
        assertEquals(
            expected = true,
            actual = vm.isMigrationInProgress.value
        )
    }

    //    @Test
    fun `should hide migration-in-progress msg if succeeded to start account`() {

        // SETUP

        val vm = buildViewModel()
        val path = MockDataFactory.randomString()

        stubProvidePath(path)
        authRepo.stub {
            onBlocking {
                selectAccount(
                    id = any(),
                    path = any()
                )
            } doReturn AccountSetup(
                account = Account(
                    id = MockDataFactory.randomUuid(),
                    name = MockDataFactory.randomString(),
                    avatar = null,
                    color = null
                ),
                features = FeaturesConfig(),
                status = AccountStatus.Active,
                config = StubConfig()
            )
        }


        // TESTING

        vm.selectAccount(MockDataFactory.randomUuid())

        assertEquals(
            expected = false,
            actual = vm.isMigrationInProgress.value
        )

        coroutineTestRule.advanceTime(SetupSelectedAccountViewModel.TIMEOUT_DURATION * 2)

        assertEquals(
            expected = false,
            actual = vm.isMigrationInProgress.value
        )
    }

    //    @Test
    fun `should hide migration-in-progress msg if failed to start account`() {

        // SETUP

        val vm = buildViewModel()

        val path = MockDataFactory.randomString()
        val id = MockDataFactory.randomUuid()

        stubProvidePath(path)
        authRepo.stub {
            onBlocking {
                selectAccount(
                    id = id,
                    path = path
                )
            } doAnswer {
                throw Exception("Could not start account")
            }
        }

        // TESTING

        vm.selectAccount(id)

        assertEquals(
            expected = false,
            actual = vm.isMigrationInProgress.value
        )

        coroutineTestRule.advanceTime(SetupSelectedAccountViewModel.TIMEOUT_DURATION)

//        // TODO Remove this line when you understand what is wrong with this test case
//        Thread.sleep(1)

        assertEquals(
            expected = false,
            actual = vm.isMigrationInProgress.value
        )
    }

    private fun stubProvidePath(path: String) {
        pathProvider.stub {
            onBlocking { providePath() } doReturn path
        }
    }

    private fun buildViewModel(): SetupSelectedAccountViewModel {
        return SetupSelectedAccountViewModel(
            selectAccount = selectAccount,
            analytics = analytics,
            pathProvider = pathProvider,
            objectTypesSubscriptionManager = objectTypesSubscriptionManager,
            relationsSubscriptionManager = relationsSubscriptionManager,
            crashReporter = crashReporter,
            configStorage = configStorage

        )
    }
}
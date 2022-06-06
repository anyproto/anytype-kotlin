package com.anytypeio.anytype.presentation.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.domain.account.DateHelper
import com.anytypeio.anytype.domain.account.RestoreAccount
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.presentation.auth.account.DeletedAccountViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyZeroInteractions
import java.time.Duration
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class DeleteAccountViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val testDispatchers = AppCoroutineDispatchers(
        io = coroutineTestRule.testDispatcher,
        computation = coroutineTestRule.testDispatcher,
        main = coroutineTestRule.testDispatcher
    )

    @Mock
    lateinit var repo: AuthRepository

    @Mock
    lateinit var helper: DateHelper

    @Mock
    lateinit var configStorage: ConfigStorage

    lateinit var restoreAccount: RestoreAccount
    lateinit var logout: Logout

    lateinit var vm: DeletedAccountViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        restoreAccount = RestoreAccount(
            repo = repo,
            dispatchers = testDispatchers
        )
        logout = Logout(
            repo = repo,
            provider = configStorage,
            dispatchers = testDispatchers
        )
        vm = DeletedAccountViewModel(
            restoreAccount = restoreAccount,
            logout = logout,
            dateHelper = helper
        )
    }

    @Test
    fun `progress should be zero - when view model is created`() {
        assertEquals(
            expected = 0f,
            actual = vm.progress.value
        )
    }

    @Test
    fun `progress should be equal to 0,5 - when deadline equals to 15 days`() {
        val nowInMillis = System.currentTimeMillis()
        val deadlineInMillis = nowInMillis + Duration.ofDays(15).toMillis()
        vm.onStart(
            deadlineInMillis = deadlineInMillis,
            nowInMillis = nowInMillis
        )
        assertEquals(
            expected = 0.5f,
            actual = vm.progress.value
        )
    }

    @Test
    fun `progress should be equal to 0,6 - when deadline equals to 15 days`() {
        val nowInMillis = System.currentTimeMillis()
        val deadlineInMillis = nowInMillis + Duration.ofDays(10).toMillis()
        vm.onStart(
            deadlineInMillis = deadlineInMillis,
            nowInMillis = nowInMillis
        )
        assertEquals(
            expected = 1 - 1 / 3f,
            actual = vm.progress.value
        )
    }

    @Test
    fun `progress should be equal to 0,3 - when deadline equals to 15 days`() {
        val nowInMillis = System.currentTimeMillis()
        val deadlineInMillis = nowInMillis + Duration.ofDays(20).toMillis()
        vm.onStart(
            deadlineInMillis = deadlineInMillis,
            nowInMillis = nowInMillis
        )
        assertEquals(
            expected = 1 - 2 / 3f,
            actual = vm.progress.value
        )
    }

    @Test
    fun `progress should be equal to 1,0 - when deadline equals to now`() {
        val nowInMillis = System.currentTimeMillis()
        vm.onStart(
            nowInMillis = nowInMillis,
            deadlineInMillis = nowInMillis
        )
        assertEquals(
            expected = 1f,
            actual = vm.progress.value
        )
    }

    @Test
    fun `progress should be equal to 0 - when deadline is in 30days`() {
        val nowInMillis = System.currentTimeMillis()
        val deadlineInMillis = nowInMillis + Duration.ofDays(30).toMillis()
        vm.onStart(
            deadlineInMillis = deadlineInMillis,
            nowInMillis = nowInMillis
        )
        assertEquals(
            expected = 0f,
            actual = vm.progress.value
        )
    }

    @Test
    fun `should proceed with logout - when deletion date is expired`() {
        val nowInMillis = System.currentTimeMillis()
        val deadlineInMillis = nowInMillis - Duration.ofDays(30).toMillis()
        vm.onStart(
            deadlineInMillis = deadlineInMillis,
            nowInMillis = nowInMillis
        )
        verifyBlocking(repo, times(1)) {
            logout(clearLocalRepositoryData = true)
        }
    }

    @Test
    fun `should display 30 days until deletion - when account is deleted today`() {
        val nowInMillis = System.currentTimeMillis()

        val tenSecondsEllapsed = Duration.ofSeconds(10).toMillis()
        val fiveHoursElapsed = Duration.ofHours(5).toMillis()
        val tenHoursElapsed = Duration.ofHours(10).toMillis()

        val deadlineInMillis = nowInMillis + Duration.ofDays(30).toMillis()

        vm.onStart(
            deadlineInMillis = deadlineInMillis,
            nowInMillis = nowInMillis + tenSecondsEllapsed
        )

        assertEquals(
            expected = DeletedAccountViewModel.DeletionDate.Later(30),
            actual = vm.date.value
        )

        vm.onStart(
            deadlineInMillis = deadlineInMillis,
            nowInMillis = nowInMillis + fiveHoursElapsed
        )

        assertEquals(
            expected = DeletedAccountViewModel.DeletionDate.Later(30),
            actual = vm.date.value
        )

        vm.onStart(
            deadlineInMillis = deadlineInMillis,
            nowInMillis = nowInMillis + tenHoursElapsed
        )

        assertEquals(
            expected = DeletedAccountViewModel.DeletionDate.Later(30),
            actual = vm.date.value
        )

        verifyZeroInteractions(repo)
    }
}
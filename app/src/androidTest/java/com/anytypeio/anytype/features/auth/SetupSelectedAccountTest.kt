package com.anytypeio.anytype.features.auth

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.domain.auth.interactor.StartAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.features.auth.fragments.TestSetupSelectedAccountFragment
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModel
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModelFactory
import com.anytypeio.anytype.ui.auth.Keys
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.stub
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
@LargeTest
class SetupSelectedAccountTest {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private lateinit var startAccount: StartAccount

    private val id = MockDataFactory.randomUuid()

    private val args = bundleOf(Keys.SELECTED_ACCOUNT_ID_KEY to id)

    @Mock
    lateinit var authRepository: AuthRepository

    @Mock
    lateinit var pathProvider: PathProvider

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        startAccount = StartAccount(
            repository = authRepository
        )
        TestSetupSelectedAccountFragment.testViewModelFactory =
            SetupSelectedAccountViewModelFactory(
                startAccount = startAccount,
                pathProvider = pathProvider
            )
    }

    @Test
    fun shouldShowError() {

        // SETUP

        val path = MockDataFactory.randomString()

        pathProvider.stub { on { providePath() } doReturn path }

        authRepository.stub {
            onBlocking {
                startAccount(
                    id = id,
                    path = path
                )
            } doThrow IllegalStateException()
        }

        // TESTING

        launchFragment(args)

        val expected = SetupSelectedAccountViewModel.ERROR_MESSAGE

        onView(withId(R.id.error)).apply {
            check(matches(withText(expected)))
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestSetupSelectedAccountFragment> {
        return launchFragmentInContainer<TestSetupSelectedAccountFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}
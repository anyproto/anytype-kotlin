package com.anytypeio.anytype.features.auth

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.data.auth.types.DefaultObjectTypesProvider
import com.anytypeio.anytype.domain.auth.interactor.StartAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.block.interactor.sets.StoreObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.FlavourConfigProvider
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.features.auth.fragments.TestSetupSelectedAccountFragment
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModel
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModelFactory
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.matchView
import com.anytypeio.anytype.ui.auth.Keys
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub

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
    lateinit var blockRepository: BlockRepository

    @Mock
    lateinit var flavourConfigProvider: FlavourConfigProvider

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var pathProvider: PathProvider

    lateinit var storeObjectTypes: StoreObjectTypes

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        startAccount = StartAccount(
            repository = authRepository,
            flavourConfigProvider = flavourConfigProvider
        )
        storeObjectTypes = StoreObjectTypes(
            repo = blockRepository,
            objectTypesProvider = DefaultObjectTypesProvider()
        )
        TestSetupSelectedAccountFragment.testViewModelFactory =
            SetupSelectedAccountViewModelFactory(
                startAccount = startAccount,
                pathProvider = pathProvider,
                analytics = analytics,
                storeObjectTypes = storeObjectTypes
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

        coroutineTestRule.advanceTime(SetupSelectedAccountViewModel.TIMEOUT_DURATION)

        val expected = "${SetupSelectedAccountViewModel.ERROR_MESSAGE}: Unknown error"

        R.id.tvError.matchView().checkHasText(expected)
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestSetupSelectedAccountFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}
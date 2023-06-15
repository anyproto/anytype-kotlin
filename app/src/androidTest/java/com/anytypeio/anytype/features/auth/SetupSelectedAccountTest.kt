package com.anytypeio.anytype.features.auth

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.SelectAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.platform.MetricsProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.features.auth.fragments.TestSetupSelectedAccountFragment
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModel
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModelFactory
import com.anytypeio.anytype.test_utils.MockDataFactory
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

    private lateinit var selectAccount: SelectAccount

    private val id = MockDataFactory.randomUuid()

    private val args = bundleOf(Keys.SELECTED_ACCOUNT_ID_KEY to id)

    @Mock
    lateinit var authRepository: AuthRepository

    @Mock
    lateinit var blockRepository: BlockRepository

    @Mock
    lateinit var featuresConfigProvider: FeaturesConfigProvider

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var pathProvider: PathProvider

    @Mock
    lateinit var configStorage: ConfigStorage

    @Mock
    lateinit var workspaceManager: WorkspaceManager

    @Mock
    private lateinit var relationsSubscriptionManager: RelationsSubscriptionManager

    @Mock
    private lateinit var objectTypesSubscriptionManager: ObjectTypesSubscriptionManager

    @Mock
    private lateinit var crashReporter: CrashReporter

    @Mock
    private lateinit var metricsProvider: MetricsProvider

    @Mock
    private lateinit var config: ConfigStorage

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        selectAccount = SelectAccount(
            repository = authRepository,
            featuresConfigProvider = featuresConfigProvider,
            configStorage = configStorage,
            workspaceManager = workspaceManager,
            metricsProvider = metricsProvider
        )
        TestSetupSelectedAccountFragment.testViewModelFactory =
            SetupSelectedAccountViewModelFactory(
                selectAccount = selectAccount,
                pathProvider = pathProvider,
                analytics = analytics,
                objectTypesSubscriptionManager = objectTypesSubscriptionManager,
                relationsSubscriptionManager = relationsSubscriptionManager,
                crashReporter = crashReporter,
                configStorage = configStorage
            )
    }

    @Test
    fun shouldShowError() {

        // SETUP

        val path = MockDataFactory.randomString()

        pathProvider.stub { on { providePath() } doReturn path }

        authRepository.stub {
            onBlocking {
                selectAccount(
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
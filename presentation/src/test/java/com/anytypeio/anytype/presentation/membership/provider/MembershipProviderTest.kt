package com.anytypeio.anytype.presentation.membership.provider

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.payments.GetMembershipTiers
import com.anytypeio.anytype.domain.workspace.MembershipChannel
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MembershipProviderTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()
    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatchers = AppCoroutineDispatchers(
        io = coroutineTestRule.dispatcher,
        main = coroutineTestRule.dispatcher,
        computation = coroutineTestRule.dispatcher
    )

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var localeProvider: LocaleProvider

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var membershipChannel: MembershipChannel

    val awaitAccountStartManager: AwaitAccountStartManager = AwaitAccountStartManager.Default

    private lateinit var provider: MembershipProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        provider = MembershipProvider.Default(
            dispatchers = dispatchers,
            membershipChannel = membershipChannel,
            awaitAccountStartManager = awaitAccountStartManager,
            localeProvider = localeProvider,
            repo = repo
        )
    }

//    @Test
//    fun `test membership status and tiers fetched successfully plus updated after event`() = runTest {
//        // Arrange
//        val membership = Membership(
//            tier = 4166,
//            membershipStatusModel = MembershipStatusModel.STATUS_ACTIVE,
//            dateStarted = 1429,
//            dateEnds = 4323,
//            isAutoRenew = false,
//            paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
//            requestedAnyName = "Cruz Brennan",
//            userEmail = "berta.edwards@example.com",
//            subscribeToNewsletter = false
//        )
//        val tierData = MembershipTierData(
//            id = 4166, name = "Gold",
//            description = "antiopam",
//            isTest = false,
//            periodType = MembershipPeriodType.PERIOD_TYPE_DAYS,
//            periodValue = 9197,
//            priceStripeUsdCents = 3957,
//            anyNamesCountIncluded = 1204,
//            anyNameMinLength = 6538,
//            features = listOf(),
//            colorStr = "quam",
//            stripeProductId = null,
//            stripeManageUrl = null,
//            iosProductId = null,
//            iosManageUrl = null,
//            androidProductId = null,
//            androidManageUrl = null
//        )
//        val tierData2 = MembershipTierData(
//            id = 123,
//            name = "Andres McKay",
//            description = "cum",
//            isTest = false,
//            periodType = MembershipPeriodType.PERIOD_TYPE_DAYS,
//            periodValue = 6841,
//            priceStripeUsdCents = 9366,
//            anyNamesCountIncluded = 5244,
//            anyNameMinLength = 2992,
//            features = listOf(),
//            colorStr = "erroribus",
//            stripeProductId = null,
//            stripeManageUrl = null,
//            iosProductId = null,
//            iosManageUrl = null,
//            androidProductId = null,
//            androidManageUrl = null,
//        )
//        val event = Membership.Event.Update(membership.copy(tier = tierData2.id))
//        val eventList = flow {
//            delay(100)
//            emit(listOf(event))
//        }
//
//        whenever(membershipChannel.observe()).thenReturn(eventList)
//        whenever(repo.membershipStatus(any())).thenReturn(membership)
//        whenever(getTiers.async(any())).thenReturn(Resultat.success(listOf(tierData, tierData2)))
//        whenever(localeProvider.language()).thenReturn("en")
//
//        val statusBeforeEvent = MembershipStatus.Active(
//            tier = tierData,
//            status = membership.membershipStatusModel,
//            dateEnds = membership.dateEnds,
//            paymentMethod = membership.paymentMethod,
//            anyName = membership.requestedAnyName
//        )
//
//        val statusAfterEvent = MembershipStatus.Active(
//            tier = tierData2,
//            status = membership.membershipStatusModel,
//            dateEnds = membership.dateEnds,
//            paymentMethod = membership.paymentMethod,
//            anyName = membership.requestedAnyName
//        )
//        // Act & Assert
//        provider.status.test {
//            awaitAccountStartManager.setIsStarted(true)
//            assertEquals(MembershipStatus.Unknown, awaitItem()) // // Initial state
//            assertEquals(statusBeforeEvent, awaitItem())
//            assertEquals(statusAfterEvent, awaitItem())
//            expectNoEvents()
//        }
//    }
//
//    @Test
//    fun `test membership fetch failure`() = runTest {
//        // Arrange
//        whenever(getMembershipStatus.async(any())).thenReturn(Resultat.failure(RuntimeException("Failed to fetch membership")))
//        whenever(localeProvider.language()).thenReturn("en")
//        awaitAccountStartManager.setIsStarted(true)
//
//        // Act & Assert
//        provider.status.test {
//            assertEquals(
//                MembershipStatus.Unknown,
//                awaitItem()
//            ) // Initial state remains since fetch failed
//            expectNoEvents() // No more events should be received
//        }
//    }
//
//    @Test
//    fun `test inactive account start manager`() = runTest {
//        // Arrange
//        awaitAccountStartManager.setIsStarted(false)
//
//        // Act & Assert
//        provider.status.test {
//            assertEquals(MembershipStatus.Unknown, awaitItem()) // Initial state
//            expectNoEvents() // No updates should be emitted
//        }
//    }

//    @Test
//    fun `test locale provider returns null uses default locale`() = runTest {
//        // Arrange
//        whenever(localeProvider.language()).thenReturn(null)
//        whenever(repo.membershipStatus(any())).thenReturn(any())
//        val command = Command.Membership.GetTiers(
//            noCache = false,
//            locale = MembershipProvider.Default.DEFAULT_LOCALE
//        )
//        verify(repo, times(1)).membershipGetTiers(command)
//        awaitAccountStartManager.setIsStarted(true)
//
//        // Act & Assert
//        provider.status().test {
//            assertEquals(MembershipStatus.Unknown, awaitItem()) // Initial state
//            expectNoEvents() // Ensures only the default locale is used
//        }
//    }
}
package com.anytypeio.anytype.payments.provider

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.workspace.MembershipChannel
import com.anytypeio.anytype.payments.DefaultCoroutineTestRule
import com.anytypeio.anytype.payments.StubMembership
import com.anytypeio.anytype.payments.StubMembershipTierData
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider.Default.Companion.DATE_FORMAT
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MembershipProviderTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    private val dispatchers = AppCoroutineDispatchers(
        io = coroutineTestRule.dispatcher,
        main = coroutineTestRule.dispatcher,
        computation = coroutineTestRule.dispatcher
    )

    @Mock
    lateinit var localeProvider: LocaleProvider

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var dateProvider: DateProvider

    @Mock
    lateinit var membershipChannel: MembershipChannel

    private val awaitAccountStartManager: AwaitAccountStartManager =
        AwaitAccountStartManager.Default

    private lateinit var provider: MembershipProvider

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        provider = MembershipProvider.Default(
            dispatchers = dispatchers,
            membershipChannel = membershipChannel,
            awaitAccountStartManager = awaitAccountStartManager,
            localeProvider = localeProvider,
            repo = repo,
            dateProvider = dateProvider
        )
    }

    @Test
    fun `test membership status and tiers fetched successfully plus updated after event`() =
        runTest {
            turbineScope {

                val dateEnds = 432331231L
                val membership = StubMembership(dateEnds = dateEnds)
                val tierData = StubMembershipTierData(androidProductId = "test.product.id")
                val tierData2 = StubMembershipTierData(isTest = true)

                val event1 = Membership.Event(StubMembership(dateEnds = dateEnds))
                val event2 = Membership.Event(StubMembership(dateEnds = dateEnds))
                val eventList = flow {
                    emit(listOf(event1))
                    emit(listOf(event2))
                }

                membershipChannel.stub {
                    on { observe() } doReturn eventList
                }
                whenever(repo.membershipStatus(any())).thenReturn(membership)
                whenever(
                    dateProvider.formatToDateString(
                        dateEnds,
                        DATE_FORMAT
                    )
                ).thenReturn("01-01-1970")
                val command = Command.Membership.GetTiers(
                    noCache = true,
                    locale = "en"
                )
                repo.stub {
                    onBlocking { membershipGetTiers(command) } doReturn listOf(tierData2, tierData)

                }
                whenever(localeProvider.language()).thenReturn("en")

                awaitAccountStartManager.setState(AwaitAccountStartManager.State.Started)
                val membershipProviderFlow = provider.status().testIn(backgroundScope)
                val membershipProviderFlow1 = provider.status().testIn(backgroundScope)
                val membershipProviderFlow2 = provider.activeTier().testIn(backgroundScope)

                val initialStatus = membershipProviderFlow.awaitItem()
                assertEquals(membership.tier, initialStatus.activeTier.value)
                assertEquals(tierData.id, initialStatus.tiers.first().id)

                val status1 = membershipProviderFlow.awaitItem()
                assertEquals(event1.membership.tier, status1.activeTier.value)
                assertEquals(tierData.id, status1.tiers.first().id)

                val status2 = membershipProviderFlow.awaitItem()
                assertEquals(event2.membership.tier, status2.activeTier.value)
                assertEquals(tierData.id, status2.tiers.first().id)

                membershipProviderFlow1.awaitItem()
                membershipProviderFlow1.awaitItem()
                membershipProviderFlow1.awaitItem()
                membershipProviderFlow2.awaitItem()
                membershipProviderFlow2.awaitItem()
                membershipProviderFlow2.awaitItem()

                verify(repo, times(6)).membershipGetTiers(command)
            }
        }
}
package com.anytypeio.anytype.payments

import com.android.billingclient.api.ProductDetails
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.payments.GetMembershipEmailStatus
import com.anytypeio.anytype.domain.payments.GetMembershipPaymentUrl
import com.anytypeio.anytype.domain.payments.IsMembershipNameValid
import com.anytypeio.anytype.domain.payments.SetMembershipEmail
import com.anytypeio.anytype.domain.payments.VerifyMembershipEmailCode
import com.anytypeio.anytype.core_models.membership.MembershipConstants
import com.anytypeio.anytype.payments.models.TierAnyName
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierEmail
import com.anytypeio.anytype.payments.models.Tier
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.payments.playbilling.BillingPurchaseState
import com.anytypeio.anytype.payments.viewmodel.MembershipViewModel
import com.anytypeio.anytype.core_models.membership.MembershipStatus
import com.anytypeio.anytype.core_models.membership.TierId
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.test_utils.MockDataFactory
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.setMain
import net.bytebuddy.utility.RandomString
import org.junit.Before
import org.mockito.kotlin.stub
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn

open class MembershipTestsSetup {

    val dispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatchers = AppCoroutineDispatchers(
        io = dispatcher,
        main = dispatcher,
        computation = dispatcher
    ).also { Dispatchers.setMain(dispatcher) }

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var billingClientLifecycle: BillingClientLifecycle

    @Mock
    lateinit var getAccount: GetAccount

    @Mock
    lateinit var membershipProvider: MembershipProvider

    @Mock
    lateinit var isMembershipNameValid: IsMembershipNameValid

    @Mock
    lateinit var setMembershipEmail: SetMembershipEmail

    @Mock
    lateinit var verifyMembershipEmailCode: VerifyMembershipEmailCode

    @Mock
    lateinit var getMembershipEmailStatus: GetMembershipEmailStatus

    @Mock
    lateinit var getMembershipPaymentUrl: GetMembershipPaymentUrl
    protected val androidProductId = "id_android_builder"
    protected val accountId = "accountId-${RandomString.make()}"

    fun membershipStatus(tiers: List<MembershipTierData>) = MembershipStatus(
        activeTier = TierId(MembershipConstants.STARTER_ID),
        status = Membership.Status.STATUS_ACTIVE,
        dateEnds = 1714199910,
        paymentMethod = MembershipPaymentMethod.METHOD_NONE,
        anyName = "",
        tiers = tiers,
        formattedDateEnds = "formattedDateEnds-${RandomString.make()}"
    )

    @Before
    open fun setUp() {
        MockitoAnnotations.openMocks(this)
        stubAccount()
    }

    protected fun validateTierView(
        expectedId: Int,
        expectedActive: Boolean,
        expectedFeatures: List<String>,
        expectedConditionInfo: TierConditionInfo.Visible,
        expectedAnyName: TierAnyName,
        expectedButtonState: TierButton,
        expectedEmailState: TierEmail,
        tier: Tier
    ) {
        assertEquals(expectedId, tier.id.value)
        assertEquals("is Active", expectedActive, tier.isActive)
        assertEquals("Features", expectedFeatures, tier.features)
        assertEquals("Condition info", expectedConditionInfo, tier.conditionInfo)
        assertEquals("Any name", expectedAnyName, tier.membershipAnyName, )
        assertEquals("Button state", expectedButtonState, tier.buttonState)
        assertEquals("Email state", expectedEmailState, tier.email)
    }

    protected fun stubMembershipProvider(membershipStatus: MembershipStatus?) {
        val flow = if (membershipStatus == null) {
            emptyFlow()
        } else {
            flow {
                emit(membershipStatus)
            }
        }
        membershipProvider.stub {
            onBlocking { status() }.thenReturn(flow)
        }
    }

    protected fun stubBilling() {
        val p = Mockito.mock(ProductDetails::class.java)
        val billingState = BillingClientState.Connected(listOf(p))
        billingClientLifecycle.stub {
            onBlocking { builderSubProductWithProductDetails }.thenReturn(
                MutableStateFlow(billingState)
            )
        }
    }

    protected fun stubBilling(billingClientState: BillingClientState) {
        billingClientLifecycle.stub {
            onBlocking { builderSubProductWithProductDetails }.thenReturn(
                MutableStateFlow(billingClientState)
            )
        }
    }

    protected fun stubPurchaseState(purchaseState: BillingPurchaseState = BillingPurchaseState.NoPurchases) {
        billingClientLifecycle.stub {
            onBlocking { subscriptionPurchases }.thenReturn(MutableStateFlow(purchaseState))
        }
    }

    protected fun buildViewModel() = MembershipViewModel(
        analytics = analytics,
        billingClientLifecycle = billingClientLifecycle,
        getAccount = getAccount,
        membershipProvider = membershipProvider,
        getMembershipPaymentUrl = getMembershipPaymentUrl,
        isMembershipNameValid = isMembershipNameValid,
        setMembershipEmail = setMembershipEmail,
        verifyMembershipEmailCode = verifyMembershipEmailCode,
        getMembershipEmailStatus = getMembershipEmailStatus
    )

    protected fun stubAccount() {
        getAccount.stub {
            onBlocking { async(Unit) } doReturn Resultat.success(
                Account(
                    id = accountId
                )
            )
        }
    }
}
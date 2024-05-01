package com.anytypeio.anytype.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.payments.GetMembershipPaymentUrl
import com.anytypeio.anytype.domain.payments.IsMembershipNameValid
import com.anytypeio.anytype.domain.payments.ResolveMembershipName
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import javax.inject.Inject

class PaymentsViewModelFactory @Inject constructor(
    private val analytics: Analytics,
    private val billingClientLifecycle: BillingClientLifecycle,
    private val getAccount: GetAccount,
    private val membershipProvider: MembershipProvider,
    private val getMembershipPaymentUrl: GetMembershipPaymentUrl,
    private val isMembershipNameValid: IsMembershipNameValid,
    private val resolveMembershipName: ResolveMembershipName
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PaymentsViewModel(
            analytics = analytics,
            billingClientLifecycle = billingClientLifecycle,
            getAccount = getAccount,
            membershipProvider = membershipProvider,
            getMembershipPaymentUrl = getMembershipPaymentUrl,
            isMembershipNameValid = isMembershipNameValid,
            resolveMembershipName = resolveMembershipName
        ) as T
    }
}
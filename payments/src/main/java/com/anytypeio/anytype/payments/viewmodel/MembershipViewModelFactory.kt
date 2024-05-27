package com.anytypeio.anytype.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.payments.GetMembershipEmailStatus
import com.anytypeio.anytype.domain.payments.GetMembershipPaymentUrl
import com.anytypeio.anytype.domain.payments.IsMembershipNameValid
import com.anytypeio.anytype.domain.payments.SetMembershipEmail
import com.anytypeio.anytype.domain.payments.VerifyMembershipEmailCode
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import javax.inject.Inject

class MembershipViewModelFactory @Inject constructor(
    private val analytics: Analytics,
    private val billingClientLifecycle: BillingClientLifecycle,
    private val getAccount: GetAccount,
    private val membershipProvider: MembershipProvider,
    private val getMembershipPaymentUrl: GetMembershipPaymentUrl,
    private val isMembershipNameValid: IsMembershipNameValid,
    private val setMembershipEmail: SetMembershipEmail,
    private val verifyMembershipEmailCode: VerifyMembershipEmailCode,
    private val getMembershipEmailStatus: GetMembershipEmailStatus
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MembershipViewModel(
            analytics = analytics,
            billingClientLifecycle = billingClientLifecycle,
            getAccount = getAccount,
            membershipProvider = membershipProvider,
            getMembershipPaymentUrl = getMembershipPaymentUrl,
            isMembershipNameValid = isMembershipNameValid,
            setMembershipEmail = setMembershipEmail,
            verifyMembershipEmailCode = verifyMembershipEmailCode,
            getMembershipEmailStatus = getMembershipEmailStatus
        ) as T
    }
}
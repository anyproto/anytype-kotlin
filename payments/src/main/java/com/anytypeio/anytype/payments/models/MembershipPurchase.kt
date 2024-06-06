package com.anytypeio.anytype.payments.models

import com.android.billingclient.api.Purchase

data class MembershipPurchase(
    val accountId: String?,
    val products: List<String>,
    val state: PurchaseState
) {

    enum class PurchaseState {
        UNSPECIFIED_STATE,
        PURCHASED,
        PENDING
    }
}

fun Purchase.toMembershipPurchase(): MembershipPurchase {
    return MembershipPurchase(
        accountId = accountIdentifiers?.obfuscatedAccountId,
        products = products,
        state = when (purchaseState) {
            Purchase.PurchaseState.PURCHASED -> MembershipPurchase.PurchaseState.PURCHASED
            Purchase.PurchaseState.PENDING -> MembershipPurchase.PurchaseState.PENDING
            else -> MembershipPurchase.PurchaseState.UNSPECIFIED_STATE
        }
    )
}
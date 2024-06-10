package com.anytypeio.anytype.payments.models

import com.android.billingclient.api.Purchase
import timber.log.Timber

data class MembershipPurchase(
    val accountId: String,
    val products: List<String>,
    val state: PurchaseState
) {

    enum class PurchaseState {
        UNSPECIFIED_STATE,
        PURCHASED,
        PENDING
    }
}

fun Purchase.toMembershipPurchase(): MembershipPurchase? {
    val obfuscatedAccountId = accountIdentifiers?.obfuscatedAccountId
    if (obfuscatedAccountId == null) {
        Timber.e("Billing purchase does not have obfuscatedAccountId")
        return null
    }
    return MembershipPurchase(
        accountId = obfuscatedAccountId,
        products = products,
        state = when (purchaseState) {
            Purchase.PurchaseState.PURCHASED -> MembershipPurchase.PurchaseState.PURCHASED
            Purchase.PurchaseState.PENDING -> MembershipPurchase.PurchaseState.PENDING
            else -> MembershipPurchase.PurchaseState.UNSPECIFIED_STATE
        }
    )
}
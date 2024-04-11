package com.anytypeio.anytype.core_models.membership

data class Membership(
    val tier: Int,
    val status: Status,
    val dateStarted: Long,
    val dateEnds: Long,
    val isAutoRenew: Boolean,
    val paymentMethod: PaymentMethod,
    val requestedAnyName: String,
    val userEmail: String,
    val subscribeToNewsletter: Boolean
)

enum class Status {
    STATUS_UNKNOWN,
    STATUS_PENDING,
    STATUS_ACTIVE,
    STATUS_PENDING_FINALIZATION
}

enum class PaymentMethod {
    METHOD_NONE,
    METHOD_CARD,
    METHOD_CRYPTO,
    METHOD_INAPP_APPLE,
    METHOD_INAPP_GOOGLE
}

data class MembershipTierData(
    val id: Int,
    val name: String,
    val description: String,
    val isTest: Boolean,
    val periodType: PeriodType,
    val periodValue: Int,
    val priceStripeUsdCents: Int,
    val anyNamesCountIncluded: Int,
    val anyNameMinLength: Int,
    val features: List<String>,
    val colorStr: String,
    val stripeProductId: String?,
    val stripeManageUrl: String?,
    val iosProductId: String?,
    val iosManageUrl: String?,
    val androidProductId: String?,
    val androidManageUrl: String?
)

enum class PeriodType {
    PERIOD_TYPE_UNKNOWN,
    PERIOD_TYPE_UNLIMITED,
    PERIOD_TYPE_DAYS,
    PERIOD_TYPE_WEEKS,
    PERIOD_TYPE_MONTHS,
    PERIOD_TYPE_YEARS
}


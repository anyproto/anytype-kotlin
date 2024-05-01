package com.anytypeio.anytype.core_models.membership

data class Membership(
    val tier: Int,
    val membershipStatusModel: Status,
    val dateStarted: Long,
    val dateEnds: Long,
    val isAutoRenew: Boolean,
    val paymentMethod: MembershipPaymentMethod,
    val nameServiceName: String,
    val nameServiceType: NameServiceNameType,
    val userEmail: String,
    val subscribeToNewsletter: Boolean
) {

    data class Event(val membership: Membership)

    enum class Status {
        STATUS_UNKNOWN,
        STATUS_PENDING,
        STATUS_ACTIVE,
        STATUS_PENDING_FINALIZATION
    }
}

enum class MembershipPaymentMethod {
    METHOD_NONE,
    METHOD_STRIPE,
    METHOD_CRYPTO,
    METHOD_INAPP_APPLE,
    METHOD_INAPP_GOOGLE
}

data class MembershipTierData(
    val id: Int,
    val name: String,
    val description: String,
    val isTest: Boolean,
    val periodType: MembershipPeriodType,
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

enum class MembershipPeriodType {
    PERIOD_TYPE_UNKNOWN,
    PERIOD_TYPE_UNLIMITED,
    PERIOD_TYPE_DAYS,
    PERIOD_TYPE_WEEKS,
    PERIOD_TYPE_MONTHS,
    PERIOD_TYPE_YEARS
}

data class GetPaymentUrlResponse(
    val paymentUrl: String,
    val billingId: String
)

enum class NameServiceNameType {
    ANY_NAME
}

enum class EmailVerificationStatus {
    STATUS_NOT_VERIFIED,
    STATUS_CODE_SENT,
    STATUS_VERIFIED
}


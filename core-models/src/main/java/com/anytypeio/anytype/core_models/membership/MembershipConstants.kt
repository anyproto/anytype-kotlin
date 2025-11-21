package com.anytypeio.anytype.core_models.membership

object MembershipConstants {

    const val NONE_ID = 0
    const val OLD_EXPLORER_ID = 1
    const val STARTER_ID = 21
    const val PIONEER_ID = 22
    const val NEW_EXPLORER_ID = 20
    const val BUILDER_ID = 4
    const val CO_CREATOR_ID = 5
    const val ANY_TEAM_ID = 7

    // New tier IDs
    const val FREE_ID = 31
    const val PLUS_ID = 40
    const val PLUS_MONTHLY_ID = 41
    const val PRO_ID = 42
    const val PRO_MONTHLY_ID = 43
    const val ULTRA_ID = 44
    const val ULTRA_MONTHLY_ID = 45

    const val MEMBERSHIP_LEVEL_DETAILS = "https://anytype.io/pricing"
    const val PRIVACY_POLICY = "https://anytype.io/app_privacy"
    const val TERMS_OF_SERVICE = "https://anytype.io/terms_of_use"
    const val MEMBERSHIP_CONTACT_EMAIL = "membership-upgrade@anytype.io"

    val ACTIVE_TIERS_WITH_BANNERS = listOf(NONE_ID, OLD_EXPLORER_ID, STARTER_ID, PIONEER_ID)

    const val ERROR_PRODUCT_NOT_FOUND = "Product not found"
    const val ERROR_PRODUCT_PRICE = "Price of the product is not available"

    const val MEMBERSHIP_NAME_MIN_LENGTH = 7
}
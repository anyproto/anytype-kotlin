package com.anytypeio.anytype.payments.constants

object TiersConstants {

    const val NONE_ID = 0
    const val EXPLORER_ID = 1
    const val BUILDER_ID = 4
    const val CO_CREATOR_ID = 5

    const val MEMBERSHIP_LEVEL_DETAILS = "https://anytype.io/pricing"
    const val PRIVACY_POLICY = "https://anytype.io/app_privacy"
    const val TERMS_OF_SERVICE = "https://anytype.io/terms_of_use"
    const val MEMBERSHIP_CONTACT_EMAIL = "membership-upgrade@anytype.io"

    val ACTIVE_TIERS_WITH_BANNERS = listOf(NONE_ID, EXPLORER_ID)

    const val ERROR_PRODUCT_NOT_FOUND = "Product not found"
    const val ERROR_PRODUCT_PRICE = "Price of the product is not available"
}
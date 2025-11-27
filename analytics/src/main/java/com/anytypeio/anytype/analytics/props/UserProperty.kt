package com.anytypeio.anytype.analytics.props

sealed class UserProperty {
    data class AccountId(val id: String?) : UserProperty()
    data class NetworkId(val id: String) : UserProperty()
    data class InterfaceLanguage(val lang: String) : UserProperty()
    data class Tier(val tierId: Int) : UserProperty() {
        val tier: String
            get() = when (tierId) {
                0 -> "None"
                4 -> "Builder"
                5 -> "Co-Creator"
                20 -> "Explorer"
                21 -> "Starter"
                22 -> "Pioneer"
                31 -> "Free"
                40 -> "Plus"
                41 -> "PlusMonthly"
                42 -> "Pro"
                43 -> "ProMonthly"
                44 -> "Ultra"
                45 -> "UltraMonthly"
                else -> "Custom"
            }
    }

    companion object {
        const val INTERFACE_LANG_KEY = "interfaceLang"
        const val TIER_KEY = "tier"
        const val ACCOUNT_ID_KEY = "accountId"
        const val NETWORK_ID_KEY = "networkId"
    }
}
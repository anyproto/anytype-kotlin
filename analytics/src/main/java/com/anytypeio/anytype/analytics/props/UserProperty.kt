package com.anytypeio.anytype.analytics.props

sealed class UserProperty {
    data class AccountId(val id: String?) : UserProperty()
    data class InterfaceLanguage(val lang: String) : UserProperty()

    companion object {
        const val INTERFACE_LANG_KEY = "interfaceLang"
    }
}
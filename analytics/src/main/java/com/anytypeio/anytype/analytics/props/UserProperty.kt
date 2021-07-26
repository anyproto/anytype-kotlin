package com.anytypeio.anytype.analytics.props

sealed class UserProperty {
    data class AccountId(val id: String?) : UserProperty()
}
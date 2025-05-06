package com.anytypeio.anytype.device

import javax.inject.Inject

class DeviceTokenStore @Inject constructor() {
    private var token: String? = null

    fun saveToken(newToken: String) {
        token = newToken
    }

    fun getToken(): String? = token
} 
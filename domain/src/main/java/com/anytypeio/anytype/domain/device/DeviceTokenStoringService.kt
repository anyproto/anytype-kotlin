package com.anytypeio.anytype.domain.device

interface DeviceTokenStoringService {
    fun saveToken(token: String)
    fun start()
    fun stop()
}
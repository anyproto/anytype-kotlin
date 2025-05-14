package com.anytypeio.anytype.domain.device

import com.anytypeio.anytype.core_models.DeviceNetworkType

interface NetworkConnectionStatus {
    fun start()
    fun stop()
    fun getCurrentNetworkType(): DeviceNetworkType
}
package com.anytypeio.anytype.domain.notifications

import com.anytypeio.anytype.presentation.notifications.PushKey

interface PushKeyProvider {
    fun start()
    fun stop()
    fun getPushKey(): Map<String, PushKey>
}
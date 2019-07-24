package com.agileburo.anytype.feature_desktop

import java.util.*

object DataFactory {
    fun randomUuid(): String {
        return UUID.randomUUID().toString()
    }
    fun randomString(): String {
        return randomUuid()
    }
}
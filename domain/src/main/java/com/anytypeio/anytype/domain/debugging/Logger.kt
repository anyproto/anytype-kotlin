package com.anytypeio.anytype.domain.debugging

interface Logger {
    fun logWarning(msg: String)
    fun logException(e: Throwable)
    fun logException(e: Throwable, msg: String)
    fun logInfo(msg: String)
}
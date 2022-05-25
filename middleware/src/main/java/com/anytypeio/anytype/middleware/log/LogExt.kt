package com.anytypeio.anytype.middleware.log

import timber.log.Timber

fun Any.logResponse() {
    val message = "===> " + this::class.java.canonicalName + ":" + "\n" + this.toString()
    Timber.d(message)
}

fun Any.logRequest() {
    val message = "<=== " + this::class.java.canonicalName + ":" + "\n" + this.toString()
    Timber.d(message)
}

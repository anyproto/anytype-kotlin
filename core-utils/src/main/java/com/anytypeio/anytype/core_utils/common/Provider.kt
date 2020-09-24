package com.anytypeio.anytype.core_utils.common

abstract class Provider<out T> {
    private var original: T? = null

    abstract fun create(): T

    fun get(): T = original ?: create().apply { original = this }
    fun clear() {
        original = null
    }
}

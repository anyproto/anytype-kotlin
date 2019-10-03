package com.agileburo.anytype.core_utils.di

abstract class Provider<T> {
    private var original: T? = null

    abstract fun create(): T

    fun get(): T = original ?: create().apply { original = this }
    fun clear() {
        original = null
    }
}

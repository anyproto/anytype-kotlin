package com.agileburo.anytype.core_utils.common

abstract class ParametrizedProvider<in P, out T> {
    private var original: T? = null

    abstract fun create(param: P): T

    fun get(param: P): T = original ?: create(param).apply { original = this }

    fun clear() {
        original = null
    }
}
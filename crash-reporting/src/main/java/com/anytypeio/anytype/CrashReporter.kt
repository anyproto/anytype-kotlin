package com.anytypeio.anytype

interface CrashReporter {
    fun init()
    fun setUser(userId: String)
}
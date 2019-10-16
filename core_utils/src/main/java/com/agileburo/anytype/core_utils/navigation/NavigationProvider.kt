package com.agileburo.anytype.core_utils.navigation

interface NavigationProvider<T> {
    fun provide(): T
}
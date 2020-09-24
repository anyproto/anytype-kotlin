package com.anytypeio.anytype.analytics.props

data class Props(val map: Map<String?, Any?>) {
    private val default = map.withDefault { null }

    companion object {
        fun empty() = Props(emptyMap())
    }
}
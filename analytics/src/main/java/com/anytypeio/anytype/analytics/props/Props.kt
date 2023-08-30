package com.anytypeio.anytype.analytics.props

data class Props(val map: Map<String?, Any?>) {

    companion object {

        const val OBJ_TYPE_CUSTOM = "custom"
        const val OBJ_LAYOUT_NONE = "none"

        fun empty() = Props(emptyMap())
    }
}
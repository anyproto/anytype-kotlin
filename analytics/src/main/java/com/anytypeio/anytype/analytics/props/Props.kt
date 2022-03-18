package com.anytypeio.anytype.analytics.props

data class Props(val map: Map<String?, Any?>) {
    private val default = map.withDefault { null }

    companion object {

        const val CHAR_TYPE_BUNDLED = '_'
        const val OBJ_TYPE_CUSTOM = "custom"
        const val OBJ_LAYOUT_NONE = "none"

        fun empty() = Props(emptyMap())

        fun mapType(type: String): String {
            return if (type.startsWith(CHAR_TYPE_BUNDLED, ignoreCase = true)) {
                type
            } else {
                OBJ_TYPE_CUSTOM
            }
        }
    }
}
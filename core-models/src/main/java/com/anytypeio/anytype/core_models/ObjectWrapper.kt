package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.ext.typeOf

/**
 * Wrapper for easily parsing object's relations when object is represented as an untyped structure.
 */
sealed class ObjectWrapper {

    /**
     * @property map [map] map with raw data containing relations.
     */
    data class Basic(val map: Map<String, Any?>) : ObjectWrapper() {

        private val default = map.withDefault { null }

        val name: String? by default

        val iconEmoji: String? by default
        val iconImage: String? by default

        val coverId: String? by default

        val coverType: CoverType?
            get() = when (val value = map[Relations.COVER_TYPE]) {
                is Double -> CoverType.values().find { type ->
                    type.code == value.toInt()
                }
                else -> null
            }

        val isArchived: Boolean? by default
        val isDeleted: Boolean? by default

        val type: List<Id>
            get() = when (val value = map[Relations.TYPE]) {
                is Id -> listOf(value)
                is List<*> -> value.typeOf()
                else -> emptyList()
            }

        val setOf: List<Id>
            get() = when (val value = map[Relations.SET_OF]) {
                is Id -> listOf(value)
                is List<*> -> value.typeOf()
                else -> emptyList()
            }

        val layout: ObjectType.Layout?
            get() = when (val value = map[Relations.LAYOUT]) {
                is Double -> ObjectType.Layout.values().singleOrNull { layout ->
                    layout.code == value.toInt()
                }
                else -> null
            }

        val id: Id by default

        val done: Boolean? by default

        val snippet: String? by default

        val fileExt: String? by default

        val fileMimeType: String? by default

        inline fun <reified T> getValue(relation: Id) : T? {
            val value = map.getOrDefault(relation, null)
            return if (value is T)
                value
            else
                null
        }

        val description: String? by default

        val isDraft: Boolean? by default

        val url: String? by default

        fun isEmpty(): Boolean = map.isEmpty()
    }
}
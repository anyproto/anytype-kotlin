package com.anytypeio.anytype.core_models

/**
 * Templates for objects
 * @property [url]
 * @property [name] template's name
 * @property [emoji] template's emoji icon
 * @property [relations] template's relations
 * @property [layout] template's layout
 *
 */
data class ObjectType(
    val url: Url,
    val name: String,
    val relations: List<Relation>,
    val layout: Layout,
    val emoji: String,
    val description: String?,
    val isHidden: Boolean
) {
    enum class Layout {
        BASIC,
        PROFILE,
        TODO,
        SET,
        OBJECT_TYPE,
        RELATION,
        FILE,
        DASHBOARD,
        IMAGE,
        DATABASE,
    }

    /**
     * Template prototype (for creating new templates)
     * @see ObjectType
     */
    data class Prototype(
        val name: String,
        val layout: Layout,
        val emoji: String
    )

    companion object {
        const val PAGE_URL = "_otpage"
    }
}

typealias Template = ObjectType
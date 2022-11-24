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
    @Deprecated("Will be deprecated. Object type will be treated as object")
    val url: Url,
    @Deprecated("Will be deprecated. Object type will be treated as object")
    val name: String,
    @Deprecated("Will be deprecated. Object type will be treated as object")
    val relationLinks: List<RelationLink>,
    @Deprecated("Will be deprecated. Object type will be treated as object")
    val layout: Layout,
    @Deprecated("Will be deprecated. Object type will be treated as object")
    val emoji: String,
    @Deprecated("Will be deprecated. Object type will be treated as object")
    val description: String,
    @Deprecated("Will be deprecated. Object type will be treated as object")
    val isHidden: Boolean,
    @Deprecated("Will be deprecated. Object type will be treated as object")
    val isArchived: Boolean,
    @Deprecated("Will be deprecated. Object type will be treated as object")
    val isReadOnly: Boolean,
    @Deprecated("Will be deprecated. Object type will be treated as object")
    val smartBlockTypes: List<SmartBlockType>
) {
    enum class Layout(val code: Int) {
        BASIC(0),
        PROFILE(1),
        TODO(2),
        SET(3),
        OBJECT_TYPE(4),
        RELATION(5),
        FILE(6),
        DASHBOARD(7),
        IMAGE(8),
        NOTE(9),
        SPACE(10),
        BOOKMARK(11),
        DATABASE(20),
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
}
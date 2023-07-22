package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.Relations.RELATION_FORMAT_OBJECT_TYPES
import com.anytypeio.anytype.core_models.ext.typeOf
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction

/**
 * Wrapper for easily parsing object's relations when object is represented as an untyped structure.
 */
sealed class ObjectWrapper {

    abstract val map: Struct

    /**
     * @property map [map] map with raw data containing relations.
     */
    data class Basic(override val map: Struct) : ObjectWrapper() {

        private val default = map.withDefault { null }

        val lastModifiedDate: Any? by default

        val name: String? by default

        val iconEmoji: String? by default
        val iconImage: String? = getValue(Relations.ICON_IMAGE)
        val iconOption: Double? by default

        val coverId: String? by default

        val coverType: CoverType
            get() = when (val value = map[Relations.COVER_TYPE]) {
                is Double -> CoverType.values().find { type ->
                    type.code == value.toInt()
                } ?: CoverType.NONE
                else -> CoverType.NONE
            }

        val isArchived: Boolean? by default
        val isDeleted: Boolean? by default

        val type: List<Id> get() = getValues(Relations.TYPE)
        val setOf: List<Id> get() = getValues(Relations.SET_OF)
        val links: List<Id> get() = getValues(Relations.LINKS)

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

        val description: String? by default

        val url: String? by default

        val featuredRelations: List<Key> get() = getValues(Relations.FEATURED_RELATIONS)

        val smartBlockTypes: List<Double>
            get() = when (val value = map[Relations.SMARTBLOCKTYPES]) {
                is Double -> listOf(value)
                is List<*> -> value.typeOf()
                else -> emptyList()
            }

        fun isEmpty(): Boolean = map.isEmpty()

        val relationKey: String by default
        val isFavorite: Boolean? by default
        val isHidden: Boolean? by default
        val isReadonly: Boolean? by default

        val relationFormat: RelationFormat?
            get() = when (val value = map[Relations.RELATION_FORMAT]) {
                is Double -> RelationFormat.values().singleOrNull { format ->
                    format.code == value.toInt()
                }
                else -> null
            }

        val restrictions: List<ObjectRestriction>
            get() = when (val value = map[Relations.RESTRICTIONS]) {
                is Double -> buildList {
                    ObjectRestriction.values().firstOrNull { it.code == value.toInt() }
                }
                is List<*> -> value.typeOf<Double>().mapNotNull { code ->
                    ObjectRestriction.values().firstOrNull { it.code == code.toInt() }
                }
                else -> emptyList()
            }

        val relationOptionColor: String? by default
        val relationReadonlyValue: Boolean? by default

        val sizeInBytes: Double? by default

        val internalFlags: List<InternalFlags>
            get() = when (val value = map[Relations.INTERNAL_FLAGS]) {
                is Double -> buildList {
                    when (value.toInt()) {
                        InternalFlags.ShouldSelectType.code -> InternalFlags.ShouldSelectType
                        InternalFlags.ShouldSelectTemplate.code -> InternalFlags.ShouldSelectTemplate
                        InternalFlags.ShouldEmptyDelete.code -> InternalFlags.ShouldEmptyDelete
                    }
                }
                is List<*> -> value.typeOf<Double>().mapNotNull { code ->
                    when (code.toInt()) {
                        InternalFlags.ShouldSelectType.code -> InternalFlags.ShouldSelectType
                        InternalFlags.ShouldSelectTemplate.code -> InternalFlags.ShouldSelectTemplate
                        InternalFlags.ShouldEmptyDelete.code -> InternalFlags.ShouldEmptyDelete
                        else -> null
                    }
                }
                else -> emptyList()
            }
    }

    /**
     * Wrapper for bookmark objects
     */
    data class Bookmark(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }
        val name: String? by default
        val description: String? by default
        val source: String? by default
        val iconEmoji: String? by default
        val iconImage: String? = getValue(Relations.ICON_IMAGE)
        val picture: String? by default
        val isArchived: Boolean? by default
        val isDeleted: Boolean? by default
    }

    /**
     * Wrapper for object types
     */
    data class Type(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }
        val id: Id by default
        val name: String? by default
        val sourceObject: Id? by default
        val description: String? by default
        val isArchived: Boolean? by default
        val iconEmoji: String? by default
        val isDeleted: Boolean? by default
        val recommendedRelations: List<Id> get() = getValues(Relations.RECOMMENDED_RELATIONS)
        val recommendedLayout: ObjectType.Layout?
            get() = when (val value = map[Relations.RECOMMENDED_LAYOUT]) {
                is Double -> ObjectType.Layout.values().singleOrNull { layout ->
                    layout.code == value.toInt()
                }
                else -> null
            }
    }

    data class Relation(override val map: Struct) : ObjectWrapper() {

        private val default = map.withDefault { null }

        private val relationKey : Key by default

        val relationFormat: RelationFormat
            get() {
                val value = map[Relations.RELATION_FORMAT]
                return if (value is Double) {
                    RelationFormat.values().firstOrNull { f ->
                        f.code == value.toInt()
                    } ?: RelationFormat.UNDEFINED
                } else {
                    RelationFormat.UNDEFINED
                }
            }

        private val relationReadonlyValue: Boolean? by default

        val id: Id by default
        val key: Key get() = relationKey
        val workspaceId: Id? by default
        val sourceObject: Id? by default
        val format: RelationFormat get() = relationFormat
        val name: String? by default
        val isHidden: Boolean? by default
        val isReadOnly: Boolean? by default
        val isArchived: Boolean? by default
        val isDeleted: Boolean? by default
        val isReadonlyValue: Boolean = relationReadonlyValue ?: false

        val restrictions: List<ObjectRestriction>
            get() = when (val value = map[Relations.RESTRICTIONS]) {
                is Double -> buildList {
                    ObjectRestriction.values().firstOrNull { it.code == value.toInt() }
                }
                is List<*> -> value.typeOf<Double>().mapNotNull { code ->
                    ObjectRestriction.values().firstOrNull { it.code == code.toInt() }
                }
                else -> emptyList()
            }

        val relationFormatObjectTypes get() = getValues<Id>(RELATION_FORMAT_OBJECT_TYPES)

        val type: List<Id> get() = getValues(Relations.TYPE)

        val isValid get() = map.containsKey(Relations.RELATION_KEY) && map.containsKey(Relations.ID)
    }

    data class Option(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }
        private val relationOptionColor : String? by default

        val id: Id by default
        val name: String? by default
        val color: String = relationOptionColor.orEmpty()
    }

    inline fun <reified T> getValue(relation: Key): T? {
        val value = map.getOrDefault(relation, null)
        return if (value is T)
            value
        else
            null
    }

    inline fun <reified T> getValues(relation: Key): List<T> {
        return when (val value = map.getOrDefault(relation, emptyList<T>())) {
            is T -> listOf(value)
            is List<*> -> value.typeOf()
            else -> emptyList()
        }
    }
}
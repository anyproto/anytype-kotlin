package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.Relations.RELATION_FORMAT_OBJECT_TYPES
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.ext.typeOf
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus

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
        val lastOpenedDate: Any? by default

        val name: String? get() = getSingleValue(Relations.NAME)
        val pluralName: String? get() = getSingleValue(Relations.PLURAL_NAME)

        val iconEmoji: String? get() = getSingleValue(Relations.ICON_EMOJI)
        val iconImage: String? get() = getSingleValue(Relations.ICON_IMAGE)
        val iconOption: Double? get() = getSingleValue(Relations.ICON_OPTION)
        val iconName: String? get() = getSingleValue(Relations.ICON_NAME)

        val coverId: String? get() = getSingleValue(Relations.COVER_ID)

        val coverType: CoverType
            get() = when (val value = map[Relations.COVER_TYPE]) {
                is Double -> CoverType.entries.find { type ->
                    type.code == value.toInt()
                } ?: CoverType.NONE
                else -> CoverType.NONE
            }

        val isArchived: Boolean? get() = getSingleValue(Relations.IS_ARCHIVED)
        val isDeleted: Boolean? get() = getSingleValue(Relations.IS_DELETED)

        val type: List<Id> get() = getValues(Relations.TYPE)
        val setOf: List<Id> get() = getValues(Relations.SET_OF)
        val links: List<Id> get() = getValues(Relations.LINKS)

        val layout: ObjectType.Layout?
            get() {
                // Try legacy layout first, then fallback to resolved layout.
                val layoutValue = getSingleValue<Double>(Relations.LEGACY_LAYOUT)
                    ?: getSingleValue<Double>(Relations.LAYOUT)
                return layoutValue?.let { value ->
                    ObjectType.Layout.entries.singleOrNull { it.code == value.toInt() }
                }
            }

        val id: Id by default

        val uniqueKey: String? get() = getSingleValue(Relations.UNIQUE_KEY)

        val done: Boolean? get() = getSingleValue(Relations.DONE)

        val snippet: String? get() = getSingleValue(Relations.SNIPPET)

        val fileExt: String? get() = getSingleValue(Relations.FILE_EXT)

        val fileMimeType: String? get() = getSingleValue(Relations.FILE_MIME_TYPE)

        val description: String? get() = getSingleValue(Relations.DESCRIPTION)

        val url: String? get() = getSingleValue(Relations.URL)

        val featuredRelations: List<Key> get() = getValues(Relations.FEATURED_RELATIONS)

        fun isEmpty(): Boolean = map.isEmpty()

        val isFavorite: Boolean? get() = getSingleValue(Relations.IS_FAVORITE)
        val isHidden: Boolean? get() = getSingleValue(Relations.IS_HIDDEN)

        val relationFormat: RelationFormat?
            get() = when (val value = map[Relations.RELATION_FORMAT]) {
                is Double -> RelationFormat.entries.singleOrNull { format ->
                    format.code == value.toInt()
                }
                else -> null
            }

        val restrictions: List<ObjectRestriction>
            get() = when (val value = map[Relations.RESTRICTIONS]) {
                is Double -> buildList {
                    ObjectRestriction.entries.firstOrNull { it.code == value.toInt() }
                }
                is List<*> -> value.typeOf<Double>().mapNotNull { code ->
                    ObjectRestriction.entries.firstOrNull { it.code == code.toInt() }
                }
                else -> emptyList()
            }

        val relationOptionColor: String? get() = getSingleValue(Relations.RELATION_OPTION_COLOR)
        val relationReadonlyValue: Boolean? get() = getSingleValue(Relations.RELATION_READ_ONLY_VALUE)

        val sizeInBytes: Double? get() = getSingleValue(Relations.SIZE_IN_BYTES)

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

        val targetObjectType: Id?
            get() = getValues<Id>(Relations.TARGET_OBJECT_TYPE).firstOrNull()

        val isValid get() = map.containsKey(Relations.ID)

        val notDeletedNorArchived get() = (isDeleted != true && isArchived != true)

        val spaceId: Id? get() = getSingleValue(Relations.SPACE_ID)

        // N.B. Only used for space view objects
        val targetSpaceId: Id? get() = getSingleValue(Relations.TARGET_SPACE_ID)

        val backlinks get() = getValues<Id>(Relations.BACKLINKS)
    }

    /**
     * Wrapper for bookmark objects
     */
    data class Bookmark(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }
        val id: Id by default
        val name: String? get() = getSingleValue(Relations.NAME)
        val description: String? get() = getSingleValue(Relations.DESCRIPTION)
        val source: String? get() = getSingleValue(Relations.SOURCE)
        val iconEmoji: String? get() = getSingleValue(Relations.ICON_EMOJI)
        val iconImage: String? get() = getSingleValue(Relations.ICON_IMAGE)
        val picture: String? get() = getSingleValue(Relations.PICTURE)
        val isArchived: Boolean? get() = getSingleValue(Relations.IS_ARCHIVED)
        val isDeleted: Boolean? get() = getSingleValue(Relations.IS_DELETED)
    }

    /**
     * Wrapper for object types
     */
    data class Type(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }
        val id: Id by default
        val uniqueKey: String get() = requireNotNull(getSingleValue(Relations.UNIQUE_KEY)) {
            "uniqueKey is required but missing for object type: ${map[Relations.ID]}"
        }
        val name: String? get() = getSingleValue(Relations.NAME)
        val pluralName: String? get() = getSingleValue(Relations.PLURAL_NAME)
        val sourceObject: Id? get() = getSingleValue(Relations.SOURCE_OBJECT)
        val description: String? get() = getSingleValue(Relations.DESCRIPTION)
        val isArchived: Boolean? get() = getSingleValue(Relations.IS_ARCHIVED)
        val iconEmoji: String? get() = getSingleValue(Relations.ICON_EMOJI)
        val isDeleted: Boolean? get() = getSingleValue(Relations.IS_DELETED)
        val recommendedRelations: List<Id> get() = getValues(Relations.RECOMMENDED_RELATIONS)
        val recommendedFeaturedRelations: List<Id> get() = getValues(Relations.RECOMMENDED_FEATURED_RELATIONS)
        val recommendedHiddenRelations: List<Id> get() = getValues(Relations.RECOMMENDED_HIDDEN_RELATIONS)
        val recommendedFileRelations: List<Id> get() = getValues(Relations.RECOMMENDED_FILE_RELATIONS)
        val recommendedLayout: ObjectType.Layout?
            get() = when (val value = map[Relations.RECOMMENDED_LAYOUT]) {
                is Double -> ObjectType.Layout.entries.singleOrNull { layout ->
                    layout.code == value.toInt()
                }
                else -> ObjectType.Layout.BASIC
            }
        val layout: ObjectType.Layout?
            get() {
                // Try legacy layout first, then fallback to resolved layout.
                val layoutValue = getSingleValue<Double>(Relations.LEGACY_LAYOUT)
                    ?: getSingleValue<Double>(Relations.LAYOUT)
                return layoutValue?.let { value ->
                    ObjectType.Layout.entries.singleOrNull { it.code == value.toInt() }
                }
            }

        val defaultTemplateId: Id? get() = getSingleValue(Relations.DEFAULT_TEMPLATE_ID)

        val restrictions: List<ObjectRestriction>
            get() = when (val value = map[Relations.RESTRICTIONS]) {
                is Double -> buildList {
                    ObjectRestriction.entries.firstOrNull { it.code == value.toInt() }
                }

                is List<*> -> value.typeOf<Double>().mapNotNull { code ->
                    ObjectRestriction.entries.firstOrNull { it.code == code.toInt() }
                }

                else -> emptyList()
            }

        val iconName: String? get() = getSingleValue(Relations.ICON_NAME)
        val iconOption: Double? get() = getSingleValue(Relations.ICON_OPTION)

        val widgetLayout: Block.Content.Widget.Layout?
            get() = when (val value = map[Relations.WIDGET_LAYOUT]) {
                is Double -> Block.Content.Widget.Layout.entries.singleOrNull { layout ->
                    layout.code == value.toInt()
                }
                else -> null
            }

        val widgetLimit: Int?
            get() = when (val value = map[Relations.WIDGET_LIMIT]) {
                is Double -> value.toInt()
                else -> null
            }

        val widgetViewId: String?
            get() = getSingleValue(Relations.WIDGET_VIEW_ID)

        val orderId: String? get() = getSingleValue(Relations.ORDER_ID)

        val allRecommendedRelations: List<Id>
            get() = recommendedFeaturedRelations + recommendedRelations + recommendedFileRelations + recommendedHiddenRelations

        val isValid get() =
            map.containsKey(Relations.UNIQUE_KEY) && map.containsKey(Relations.ID)
    }

    data class Relation(override val map: Struct) : ObjectWrapper() {

        private val default = map.withDefault { null }

        private val relationKey : Key get() = requireNotNull(getSingleValue(Relations.RELATION_KEY)) {
            "relationKey is required but missing for relation object: ${map[Relations.ID]}"
        }

        val relationFormat: RelationFormat
            get() {
                val value = map[Relations.RELATION_FORMAT]
                return if (value is Double) {
                    RelationFormat.entries.firstOrNull { f ->
                        f.code == value.toInt()
                    } ?: RelationFormat.UNDEFINED
                } else {
                    RelationFormat.UNDEFINED
                }
            }

        private val relationReadonlyValue: Boolean? get() = getSingleValue(Relations.RELATION_READ_ONLY_VALUE)

        val id: Id by default
        val uniqueKey: String? get() = getSingleValue(Relations.UNIQUE_KEY)
        val key: Key get() = relationKey
        val spaceId: Id? get() = getSingleValue(Relations.SPACE_ID)
        val sourceObject: Id? get() = getSingleValue(Relations.SOURCE_OBJECT)
        val format: RelationFormat get() = relationFormat
        val name: String? get() = getSingleValue(Relations.NAME)
        val isHidden: Boolean? get() = getSingleValue(Relations.IS_HIDDEN)
        val isReadOnly: Boolean? get() = getSingleValue(Relations.IS_READ_ONLY)
        val isArchived: Boolean? get() = getSingleValue(Relations.IS_ARCHIVED)
        val isDeleted: Boolean? get() = getSingleValue(Relations.IS_DELETED)
        val isReadonlyValue: Boolean = relationReadonlyValue ?: false

        val restrictions: List<ObjectRestriction>
            get() = when (val value = map[Relations.RESTRICTIONS]) {
                is Double -> buildList {
                    ObjectRestriction.entries.firstOrNull { it.code == value.toInt() }
                }
                is List<*> -> value.typeOf<Double>().mapNotNull { code ->
                    ObjectRestriction.entries.firstOrNull { it.code == code.toInt() }
                }
                else -> emptyList()
            }

        val relationFormatObjectTypes get() = getValues<Id>(RELATION_FORMAT_OBJECT_TYPES)

        val type: List<Id> get() = getValues(Relations.TYPE)

        val isValid get() =
            map.containsKey(Relations.RELATION_KEY) && map.containsKey(Relations.ID)

        val isValidToUse get() = isValid && isDeleted != true && isArchived != true && isHidden != true

    }

    data class Option(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }
        private val relationOptionColor : String? get() = getSingleValue(Relations.RELATION_OPTION_COLOR)

        val id: Id by default
        val name: String? get() = getSingleValue(Relations.NAME)
        val color: String = relationOptionColor.orEmpty()
        val isDeleted: Boolean? get() = getSingleValue(Relations.IS_DELETED)
    }

    data class SpaceView(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }

        val id: Id by default
        val name: String? get() = getSingleValue(Relations.NAME)
        val description: String? get() = getSingleValue(Relations.DESCRIPTION)
        val iconImage: String? get() = getSingleValue(Relations.ICON_IMAGE)
        val iconOption: Double? get() = getSingleValue(Relations.ICON_OPTION)

        // N.B. Only used for space view objects
        val targetSpaceId: String? get() = getSingleValue(Relations.TARGET_SPACE_ID)

        val chatId: Id? get() = getSingleValue(Relations.CHAT_ID)

        val creator: Id? get() = getSingleValue(Relations.CREATOR)

        val spaceAccountStatus: SpaceStatus
            get() {
                val code = getValue<Double?>(Relations.SPACE_ACCOUNT_STATUS)
                return SpaceStatus
                    .entries
                    .firstOrNull { it.code == code?.toInt() }
                    ?: SpaceStatus.UNKNOWN
            }

        val spaceLocalStatus: SpaceStatus
            get() {
                val code = getValue<Double?>(Relations.SPACE_LOCAL_STATUS)
                return SpaceStatus
                    .entries
                    .firstOrNull { it.code == code?.toInt() }
                    ?: SpaceStatus.UNKNOWN
            }

        val spaceAccessType: SpaceAccessType?
            get() {
                val code = getValue<Double?>(Relations.SPACE_ACCESS_TYPE)
                return SpaceAccessType
                    .entries
                    .firstOrNull { it.code == code?.toInt() }
            }

        val spaceUxType: SpaceUxType?
            get() {
                val code = getValue<Double?>(Relations.SPACE_UX_TYPE)
                return SpaceUxType
                    .entries
                    .firstOrNull { it.code == code?.toInt() }
            }

        val writersLimit: Double? get() = getSingleValue(Relations.WRITERS_LIMIT)
        val readersLimit: Double? get() = getSingleValue(Relations.READERS_LIMIT)

        val spacePushNotificationEncryptionKey: String? get() = getSingleValue(Relations.SPACE_PUSH_NOTIFICATIONS_KEY)

        val sharedSpaceLimit: Int
            get() {
                val value = getValue<Double?>(Relations.SHARED_SPACES_LIMIT)
                return value?.toInt() ?: 0
            }

        val isLoading: Boolean
            get() {
                return spaceLocalStatus == SpaceStatus.LOADING
                        && spaceAccountStatus != SpaceStatus.SPACE_REMOVING
                        && spaceAccountStatus != SpaceStatus.SPACE_DELETED
                        && spaceAccountStatus != SpaceStatus.SPACE_JOINING
            }

        val isActive: Boolean
            get() {
                return spaceLocalStatus == SpaceStatus.OK
                        && spaceAccountStatus != SpaceStatus.SPACE_REMOVING
                        && spaceAccountStatus != SpaceStatus.SPACE_DELETED
            }

        val isUnknown: Boolean
            get() {
                return spaceLocalStatus == SpaceStatus.UNKNOWN
                        && spaceAccountStatus == SpaceStatus.UNKNOWN
            }

        val isPossibleToShare : Boolean get() {
            return spaceAccessType == SpaceAccessType.PRIVATE
                    || spaceAccessType == SpaceAccessType.SHARED
        }

        val isShared: Boolean get() {
            return spaceAccessType == SpaceAccessType.SHARED
        }

        val spacePushNotificationMode
            get() = getSingleValue<Double>(Relations.SPACE_PUSH_NOTIFICATION_MODE)
                ?.let { code ->
                    NotificationState.entries.firstOrNull { it.code == code.toInt() }
                } ?: NotificationState.ALL

        val spaceOrder: String? get() = getSingleValue(Relations.SPACE_ORDER)

        val spaceJoinDate: Double? get() = getSingleValue(Relations.SPACE_JOIN_DATE)
    }

    inline fun <reified T> getValue(relation: Key): T? {
        val value = map.getOrDefault(relation, null)
        return if (value is T)
            value
        else
            null
    }

    inline fun <reified T> getSingleValue(relation: Key): T? = map.getSingleValue(relation)

    inline fun <reified T> getValues(relation: Key): List<T> {
        return when (val value = map.getOrDefault(relation, emptyList<T>())) {
            is T -> listOf(value)
            is List<*> -> value.typeOf()
            else -> emptyList()
        }
    }

    data class ObjectInternalFlags(override val map: Struct) : ObjectWrapper() {
        val internalFlags: List<InternalFlags>
            get() = when (val value = map[Relations.INTERNAL_FLAGS]) {
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

    data class File(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }
        val id: Id by default
        val name: String? get() = getSingleValue(Relations.NAME)
        val description: String? get() = getSingleValue(Relations.DESCRIPTION)
        val fileExt: String? get() = getSingleValue(Relations.FILE_EXT)
        val fileMimeType: String? get() = getSingleValue(Relations.FILE_MIME_TYPE)
        val url: String? get() = getSingleValue(Relations.URL)
        val isArchived: Boolean? get() = getSingleValue(Relations.IS_ARCHIVED)
        val isDeleted: Boolean? get() = getSingleValue(Relations.IS_DELETED)
    }

    data class SpaceMember(override val map: Struct): ObjectWrapper() {
        private val default = map.withDefault { null }

        val id: Id by default
        val spaceId: Id? get() = getSingleValue(Relations.SPACE_ID)
        val identity: Id by default

        val name: String? get() = getSingleValue(Relations.NAME)
        val iconImage: String? get() = getSingleValue(Relations.ICON_IMAGE)

        val status
            get() = getSingleValue<Double>(Relations.PARTICIPANT_STATUS)
                .let { code ->
                    ParticipantStatus.entries.firstOrNull { it.code == code?.toInt() }
                }

        val permissions
            get() = getSingleValue<Double>(Relations.PARTICIPANT_PERMISSIONS)
                .let { code ->
                    SpaceMemberPermissions.entries.firstOrNull { it.code == code?.toInt() }
                }

        val globalName: String? get() = getSingleValue(Relations.GLOBAL_NAME)
    }

    data class Date(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }
        val id: Id by default
        val name: String? get() = getSingleValue(Relations.NAME)
        val timestamp: Double?
            get() = when (val value = map[Relations.TIMESTAMP]) {
                is Double -> value
                is Int -> value.toDouble()
                else -> null
            }
    }
}

inline fun <reified T> Struct.getSingleValue(relation: Key): T? =
    when (val value = getOrDefault(relation, null)) {
        is T -> value
        is List<*> -> value.typeOf<T>().firstOrNull()
        else -> null
    }
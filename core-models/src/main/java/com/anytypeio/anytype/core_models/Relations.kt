package com.anytypeio.anytype.core_models

/**
 * Keys for predefined, bundled relations.
 * // TODO remove outdated or deprecated relations
 */
object Relations {

    const val ID = "id"
    const val COVER = "cover"
    const val COVER_TYPE = "coverType"
    const val COVER_ID = "coverId"
    const val DESCRIPTION = "description"
    const val LAYOUT = "layout"
    const val NAME = "name"
    const val ICON_EMOJI = "iconEmoji"
    const val ICON_OPTION = "iconOption"
    const val ICON_IMAGE = "iconImage"
    const val RELATION_FORMAT = "relationFormat"
    const val IS_ARCHIVED = "isArchived"
    const val IS_DELETED = "isDeleted"
    const val IS_FAVORITE = "isFavorite"
    const val IS_READ_ONLY = "isReadonly"
    const val IS_HIDDEN = "isHidden"
    const val LAST_OPENED_DATE = "lastOpenedDate"
    const val LAST_MODIFIED_DATE = "lastModifiedDate"
    const val LAST_USED_DATE = "lastUsedDate"
    const val TYPE = "type"
    const val LINKS = "links"
    const val TARGET_OBJECT_TYPE = "targetObjectType"
    const val DONE = "done"
    const val FEATURED_RELATIONS = "featuredRelations"
    const val SNIPPET = "snippet"
    const val SPACE_ID = "spaceId"
    const val TARGET_SPACE_ID = "targetSpaceId"
    const val SET_OF = "setOf"
    const val URL = "url"
    const val SOURCE = "source"
    const val SMARTBLOCKTYPES = "smartblockTypes"
    const val RELATION_KEY = "relationKey"
    const val RELATION_OPTION_COLOR = "relationOptionColor"
    const val RELATION_OPTION_DICT = "relationOptionsDict"
    const val SCOPE = "scope"
    const val RESTRICTIONS = "restrictions"
    const val MAX_COUNT = "relationMaxCount"
    const val RELATION_READ_ONLY_VALUE = "relationReadonlyValue"
    const val RELATION_DEFAULT_VALUE = "relationDefaultValue"
    const val RELATION_FORMAT_OBJECT_TYPES = "relationFormatObjectTypes"
    const val SOURCE_OBJECT = "sourceObject"
    const val INTERNAL_FLAGS = "internalFlags"
    const val SIZE_IN_BYTES = "sizeInBytes"
    const val FILE_SYNC_STATUS = "fileSyncStatus"
    const val CREATED_DATE = "createdDate"
    const val CREATOR = "creator"
    const val SYNC_DATE = "syncDate"
    const val SYNC_STATUS = "syncStatus"

    const val PAGE_COVER = "pageCover"

    const val NUMBER_DEFAULT_VALUE = "0"
    const val RELATION_NAME_EMPTY = "Untitled"

    const val FILE_EXT = "fileExt"
    const val FILE_MIME_TYPE = "fileMimeType"

    const val RECOMMENDED_LAYOUT = "recommendedLayout"
    const val RECOMMENDED_RELATIONS = "recommendedRelations"
    const val DEFAULT_TEMPLATE_ID = "defaultTemplateId"

    const val UNIQUE_KEY = "uniqueKey"

    const val BACKLINKS = "backlinks"

    const val ORIGIN = "origin"

    /**
     * Transitive relation key.
     */
    const val TYPE_UNIQUE_KEY = "type.uniqueKey"

    const val SPACE_ACCOUNT_STATUS = "spaceAccountStatus"
    const val SPACE_LOCAL_STATUS = "spaceLocalStatus"

    const val IDENTITY_PROFILE_LINK = "identityProfileLink"

    const val PARTICIPANT_STATUS = "participantStatus"
    const val PARTICIPANT_PERMISSIONS = "participantPermissions"
    const val SPACE_ACCESS_TYPE = "spaceAccessType"
    const val IDENTITY = "identity"
    const val GLOBAL_NAME = "globalName"
    const val READERS_LIMIT = "readersLimit"
    const val WRITERS_LIMIT = "writersLimit"

    const val SHARED_SPACES_LIMIT = "sharedSpacesLimit"

    val systemRelationKeys = listOf(
        "id",
        "name",
        "description",
        "snippet",
        "type",
        "featuredRelations",
        "workspaceId",
        "done",
        "links",
        "internalFlags",
        "restrictions",

        "source",
        "sourceObject",

        "setOf",
        "smartblockTypes",
        "targetObjectType",
        "recommendedRelations",
        "recommendedLayout",
        "templateIsBundled",

        "layout",
        "layoutAlign",

        "creator",
        "createdDate",
        "lastOpenedDate",
        "lastModifiedBy",
        "lastModifiedDate",
        "addedDate",

        "iconEmoji",
        "iconImage",

        "coverId",
        "coverType",
        "coverScale",
        "coverX",
        "coverY",

        "fileExt",
        "fileMimeType",
        "sizeInBytes",

        "isHidden",
        "isArchived",
        "isFavorite",
        "isReadonly",

        "relationKey",
        "relationFormat",
        "relationMaxCount",
        "relationReadonlyValue",
        "relationDefaultValue",
        "relationFormatObjectTypes",
        "relationOptionColor",
        "sharedSpacesLimit"
    )
}
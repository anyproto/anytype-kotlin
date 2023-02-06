package com.anytypeio.anytype.core_models

/**
 * Keys for predefined, bundled relations.
 */
object Relations {

    const val ID = "id"
    const val COVER_TYPE = "coverType"
    const val COVER_ID = "coverId"
    const val DESCRIPTION = "description"
    const val LAYOUT = "layout"
    const val NAME = "name"
    const val ICON_EMOJI = "iconEmoji"
    const val ICON_IMAGE = "iconImage"
    const val RELATION_FORMAT = "relationFormat"
    const val IS_ARCHIVED = "isArchived"
    const val IS_DELETED = "isDeleted"
    const val IS_FAVORITE = "isFavorite"
    const val IS_READ_ONLY = "isReadonly"
    const val IS_HIDDEN = "isHidden"
    const val LAST_OPENED_DATE = "lastOpenedDate"
    const val LAST_MODIFIED_DATE = "lastModifiedDate"
    const val TYPE = "type"
    const val TARGET_OBJECT_TYPE = "targetObjectType"
    const val DONE = "done"
    const val FEATURED_RELATIONS = "featuredRelations"
    const val FILE_EXT = "fileExt"
    const val FILE_MIME_TYPE = "fileMimeType"
    const val SNIPPET = "snippet"
    const val IS_DRAFT = "isDraft"
    const val WORKSPACE_ID = "workspaceId"
    const val SET_OF = "setOf"
    const val IS_HIGHLIGHTED = "isHighlighted"
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


    const val PAGE_COVER = "pageCover"

    const val NUMBER_DEFAULT_VALUE = "0"
    const val RELATION_NAME_EMPTY = "Untitled"

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
        "relationOptionColor"
    )
}
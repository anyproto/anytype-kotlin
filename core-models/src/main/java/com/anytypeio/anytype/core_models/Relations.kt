package com.anytypeio.anytype.core_models

/**
 * Keys for predefined, bundled relations.
 */
object Relations {

    const val ID = "id"
    const val COVER_TYPE = "coverType"
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
    const val RELATION_OPTION_TEXT = "relationOptionText"
    const val RELATION_OPTION_COLOR = "relationOptionColor"
    const val RELATION_OPTION_DICT = "relationOptionsDict"
    const val SCOPE = "scope"
    const val RESTRICTIONS = "restrictions"
    const val MAX_COUNT = "relationMaxCount"
    const val RELATION_IS_READ_ONLY = "relationReadonly"
    const val RELATION_DEFAULT_VALUE = "relationDefaultValue"
    const val RELATION_FORMAT_OBJECT_TYPES = "relationFormatObjectTypes"


    const val PAGE_COVER = "pageCover"

    val defaultRelations = listOf(
        ID,
        NAME,
        DESCRIPTION,
        ICON_EMOJI,
        ICON_IMAGE,
        RELATION_FORMAT,
        TYPE,
        LAYOUT,
        IS_HIDDEN,
        IS_ARCHIVED,
        IS_READ_ONLY,
        DONE,
        FILE_EXT,
        FILE_MIME_TYPE
    )

    const val NUMBER_DEFAULT_VALUE = "0"
    const val RELATION_NAME_EMPTY = "Untitled"
}
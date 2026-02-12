package com.anytypeio.anytype.core_models

/**
 * Keys for predefined, bundled relations.
 * // TODO remove outdated or deprecated relations
 */
object Relations {

    const val ID = "id"
    const val CHAT_ID = "chatId"
    const val COVER = "cover"
    const val COVER_TYPE = "coverType"
    const val COVER_ID = "coverId"
    const val DESCRIPTION = "description"
    const val LAYOUT = "resolvedLayout"
    const val LEGACY_LAYOUT = "layout"
    const val NAME = "name"
    const val PLURAL_NAME = "pluralName"
    const val ICON_EMOJI = "iconEmoji"
    const val ICON_OPTION = "iconOption"
    const val ICON_NAME = "iconName"
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
    const val LAST_MESSAGE_DATE = "lastMessageDate"
    const val TYPE = "type"
    const val LINKS = "links"
    const val TARGET_OBJECT_TYPE = "targetObjectType"
    const val DONE = "done"
    const val FEATURED_RELATIONS = "featuredRelations"
    const val SNIPPET = "snippet"
    const val SPACE_ID = "spaceId"
    const val SPACE_UX_TYPE = "spaceUxType"
    const val TARGET_SPACE_ID = "targetSpaceId"
    const val SET_OF = "setOf"
    const val URL = "url"
    const val SOURCE = "source"
    const val PICTURE = "picture"
    const val SMARTBLOCKTYPES = "smartblockTypes"
    const val RELATION_KEY = "relationKey"
    const val RELATION_OPTION_COLOR = "relationOptionColor"
    const val RELATION_OPTION_DICT = "relationOptionsDict"
    const val RELATION_OPTION_ORDER = "relationOptionOrder"
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
    const val IS_HIDDEN_DISCOVERY = "isHiddenDiscovery"
    const val MENTIONS = "mentions"
    const val TIMESTAMP = "timestamp"

    const val PAGE_COVER = "pageCover"

    const val NUMBER_DEFAULT_VALUE = "0"
    const val RELATION_NAME_EMPTY = "Untitled"

    const val FILE_EXT = "fileExt"
    const val FILE_MIME_TYPE = "fileMimeType"

    const val RECOMMENDED_LAYOUT = "recommendedLayout"
    const val RECOMMENDED_RELATIONS = "recommendedRelations"
    const val RECOMMENDED_FEATURED_RELATIONS = "recommendedFeaturedRelations"
    const val RECOMMENDED_HIDDEN_RELATIONS = "recommendedHiddenRelations"
    const val RECOMMENDED_FILE_RELATIONS = "recommendedFileRelations"
    const val DEFAULT_TEMPLATE_ID = "defaultTemplateId"
    const val TEMPLATE_NAME_PREFILL_TYPE = "templateNamePrefillType"

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
    const val PROFILE_OWNER_IDENTITY = "profileOwnerIdentity"

    const val PARTICIPANT_STATUS = "participantStatus"
    const val PARTICIPANT_PERMISSIONS = "participantPermissions"
    const val SPACE_ACCESS_TYPE = "spaceAccessType"
    const val SPACE_DASHBOARD_ID = "spaceDashboardId"
    const val ONE_TO_ONE_IDENTITY = "oneToOneIdentity"
    const val ONE_TO_ONE_REQUEST_METADATA = "oneToOneRequestMetadataKey"
    const val IDENTITY = "identity"
    const val GLOBAL_NAME = "globalName"
    const val READERS_LIMIT = "readersLimit"
    const val WRITERS_LIMIT = "writersLimit"

    const val SHARED_SPACES_LIMIT = "sharedSpacesLimit"
    const val FILE_ID = "fileId"

    const val LAYOUT_ALIGN = "layoutAlign"

    const val AUTO_WIDGET_DISABLED = "autoWidgetDisabled"

    const val SPACE_PUSH_NOTIFICATIONS_KEY = "spacePushNotificationEncryptionKey"
    const val SPACE_PUSH_NOTIFICATIONS_TOPIC = "spacePushNotificationsTopics"
    const val SPACE_PUSH_NOTIFICATION_MODE = "spacePushNotificationMode"
    const val SPACE_ORDER = "spaceOrder"
    const val SPACE_JOIN_DATE = "spaceJoinDate"

    const val WIDGET_LAYOUT = "widgetLayout"
    const val WIDGET_LIMIT = "widgetLimit"
    const val WIDGET_VIEW_ID = "widgetViewId"

    const val PUSH_NOTIFICATION_FORCE_ALL_IDS = "spacePushNotificationForceAllIds"
    const val PUSH_NOTIFICATION_FORCE_MUTE_IDS = "spacePushNotificationForceMuteIds"
    const val PUSH_NOTIFICATION_FORCE_MENTION_IDS = "spacePushNotificationForceMentionIds"

    const val ORDER_ID = "orderId"

    const val CREATED_IN_CONTEXT = "createdInContext"
    const val CREATED_IN_CONTEXT_REF = "createdInContextRef"

    val systemRelationKeys = listOf(
        "id",
        "name",
        "description",
        "snippet",
        "iconEmoji",
        "iconImage",
        "type",
        "layout",
        "layoutAlign",
        "coverId",
        "coverScale",
        "coverType",
        "coverX",
        "coverY",
        "createdDate",
        "creator",
        "lastModifiedDate",
        "lastModifiedBy",
        "lastOpenedDate",
        "featuredRelations",
        "isFavorite",
        "workspaceId",
        "spaceId",
        "links",
        "internalFlags",
        "restrictions",
        "addedDate",
        "source",
        "sourceObject",
        "setOf",
        "relationFormat",
        "relationKey",
        "relationReadonlyValue",
        "relationDefaultValue",
        "relationMaxCount",
        "relationOptionColor",
        "relationFormatObjectTypes",
        "isReadonly",
        "isDeleted",
        "isHidden",
        "spaceShareableStatus",
        "isAclShared",
        "isHiddenDiscovery",
        "done",
        "isArchived",
        "templateIsBundled",
        "smartblockTypes",
        "targetObjectType",
        "recommendedLayout",
        "fileExt",
        "fileMimeType",
        "sizeInBytes",
        "oldAnytypeID",
        "spaceDashboardId",
        "recommendedRelations",
        "iconOption",
        "widthInPixels",
        "heightInPixels",
        "sourceFilePath",
        "fileSyncStatus",
        "defaultTemplateId",
        "templateNamePrefillType",
        "uniqueKey",
        "backlinks",
        "profileOwnerIdentity",
        "fileBackupStatus",
        "fileId",
        "fileIndexingStatus",
        "origin",
        "revision",
        "imageKind",
        "importType",
        "spaceAccessType",
        "spaceInviteFileCid",
        "spaceInviteFileKey",
        "readersLimit",
        "writersLimit",
        "sharedSpacesLimit",
        "participantPermissions",
        "participantStatus",
        "latestAclHeadId",
        "identity",
        "globalName",
        "syncDate",
        "syncStatus",
        "syncError",
        "lastUsedDate",
        "mentions",
        "chatId",
        "hasChat",
        "timestamp",
        "recommendedFeaturedRelations",
        "recommendedHiddenRelations",
        "recommendedFileRelations",
        "layoutWidth",
        "defaultViewType",
        "defaultTypeId",
        "resolvedLayout",
        "fileId",
    )
}
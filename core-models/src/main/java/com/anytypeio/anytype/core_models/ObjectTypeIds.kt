package com.anytypeio.anytype.core_models

/**
 * Keys for predefined, bundled object types.
 */
object ObjectTypeIds {
    const val PAGE = "ot-page"
    const val OBJECT_TYPE = "ot-objectType"
    const val RELATION = "ot-relation"
    const val TEMPLATE = "ot-template"
    const val IMAGE = "ot-image"
    const val FILE = "ot-file"
    const val VIDEO = "ot-video"
    const val AUDIO = "ot-audio"
    const val SET = "ot-set"
    const val COLLECTION = "ot-collection"
    const val TASK = "ot-task"
    const val DATE = "ot-date"
    const val PROFILE = "ot-profile" //contains User Profile page and Anytype Person page
    const val NOTE = "ot-note"
    const val WORKSPACE = "ot-space"
    const val DASHBOARD = "ot-dashboard"
    const val BOOKMARK = "ot-bookmark"
    const val RELATION_OPTION = "ot-relationOption"
    const val SPACE = "ot-space"

    const val DEFAULT_OBJECT_TYPE_PREFIX = "ot-"

    fun getTypesWithoutTemplates(): List<String> =
        listOf(BOOKMARK, NOTE).plus(getFileTypes()).plus(getSetTypes())
            .plus(getSystemTypes())

    fun getFileTypes(): List<String> = listOf(FILE, IMAGE, AUDIO, VIDEO)

    fun getSystemTypes(): List<String> = listOf(
        OBJECT_TYPE,
        TEMPLATE,
        RELATION,
        RELATION_OPTION,
        DASHBOARD,
        DATE,
        MarketplaceObjectTypeIds.OBJECT_TYPE,
        MarketplaceObjectTypeIds.RELATION
    )

    fun getSetTypes(): List<String> = listOf(SET, COLLECTION)
}

object MarketplaceObjectTypeIds {
    const val OBJECT_TYPE = "_otobjectType"
    const val PAGE = "_otpage"
    const val RELATION = "_otrelation"
    const val TEMPLATE = "_ottemplate"
    const val IMAGE = "_otimage"
    const val FILE = "_otfile"
    const val VIDEO = "_otvideo"
    const val AUDIO = "_otaudio"
    const val SET = "_otset"
    const val TASK = "_ottask"
    const val DATE = "_otdate"
    const val PROFILE = "_otprofile"
    const val NOTE = "_otnote"
    const val DASHBOARD = "_otdashboard"
    const val BOOKMARK = "_otbookmark"

    const val MARKETPLACE_OBJECT_TYPE_PREFIX = "_ot"
}

object Marketplace {
    const val MARKETPLACE_SPACE_ID = "_anytype_marketplace"
}
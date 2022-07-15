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
    val description: String,
    val isHidden: Boolean,
    val isArchived: Boolean,
    val isReadOnly: Boolean,
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

    /**
     * Keys for predefined, bundled object types.
     */
    companion object {
        const val PAGE_URL = "_otpage"
        const val OBJECT_TYPE_URL = "_otobjectType"
        const val RELATION_URL = "_otrelation"
        const val TEMPLATE_URL = "_ottemplate"
        const val IMAGE_URL = "_otimage"
        const val FILE_URL = "_otfile"
        const val VIDEO_URL = "_otvideo"
        const val AUDIO_URL = "_otaudio"
        const val SET_URL = "_otset"
        const val TASK_URL = "_ottask"
        const val DATE_URL = "_otdate"
        const val PROFILE_URL = "_otprofile" //contains User Profile page and Anytype Person page
        const val NOTE_URL = "_otnote"
        const val WORKSPACE_URL = "_otspace"
        const val DASHBOARD_TYPE = "_otdashboard"
        const val BOOKMARK_TYPE = "_otbookmark"

        const val MAX_SNIPPET_SIZE = 30
    }
}

class ObjectTypeComparator : Comparator<ObjectType> {

    override fun compare(o1: ObjectType, o2: ObjectType): Int {
        val o1Url = o1.url
        val o2Url = o2.url
        if (o1Url == o2Url) return 0

        if (o1Url == ObjectType.PAGE_URL && o2Url != ObjectType.PAGE_URL) return -1
        if (o1Url != ObjectType.PAGE_URL && o2Url == ObjectType.PAGE_URL) return 1

        if (o1Url == ObjectType.NOTE_URL && o2Url != ObjectType.NOTE_URL) return -1
        if (o1Url != ObjectType.NOTE_URL && o2Url == ObjectType.NOTE_URL) return 1

        if (o1Url == ObjectType.SET_URL && o2Url != ObjectType.SET_URL) return -1
        if (o1Url != ObjectType.SET_URL && o2Url == ObjectType.SET_URL) return 1

        if (o1Url == ObjectType.TASK_URL && o2Url != ObjectType.TASK_URL) return -1
        if (o1Url != ObjectType.TASK_URL && o2Url == ObjectType.TASK_URL) return 1

        val o1Name = o1.name
        val o2Name = o2.name

        return o1Name.compareTo(o2Name)
    }
}
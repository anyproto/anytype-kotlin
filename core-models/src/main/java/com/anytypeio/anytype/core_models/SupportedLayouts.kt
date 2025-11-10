package com.anytypeio.anytype.core_models

object SupportedLayouts {
    val layouts = listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.FILE,
        ObjectType.Layout.IMAGE,
        ObjectType.Layout.VIDEO,
        ObjectType.Layout.PROFILE,
        ObjectType.Layout.PARTICIPANT,
        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION,
        ObjectType.Layout.TODO,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.BOOKMARK,
        ObjectType.Layout.AUDIO,
        ObjectType.Layout.PDF,
        ObjectType.Layout.OBJECT_TYPE,
        ObjectType.Layout.CHAT_DERIVED
    )
    val editorLayouts = listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.PROFILE,
        ObjectType.Layout.PARTICIPANT,
        ObjectType.Layout.TODO,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.BOOKMARK
    )
    val fileLayouts = listOf(
        ObjectType.Layout.FILE,
        ObjectType.Layout.IMAGE,
        ObjectType.Layout.VIDEO,
        ObjectType.Layout.AUDIO,
        ObjectType.Layout.PDF
    )

    val systemLayouts = listOf(
        ObjectType.Layout.RELATION,
        ObjectType.Layout.RELATION_OPTION,
        ObjectType.Layout.DASHBOARD,
        ObjectType.Layout.SPACE,
        ObjectType.Layout.SPACE_VIEW,
        ObjectType.Layout.TAG
    )

    val createObjectLayouts = listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.PROFILE,
        ObjectType.Layout.PARTICIPANT,
        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION,
        ObjectType.Layout.TODO,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.BOOKMARK,
        ObjectType.Layout.CHAT_DERIVED
    )

    val dateLayouts = listOf(
        ObjectType.Layout.DATE
    )

    val addAsLinkToLayouts = editorLayouts + listOf(
        ObjectType.Layout.COLLECTION
    )

    val globalSearchLayouts = createObjectLayouts + fileLayouts + dateLayouts + listOf(ObjectType.Layout.OBJECT_TYPE)

    val widgetsLayouts = layouts + dateLayouts

    val lastOpenObjectLayouts = layouts + dateLayouts

    /**
     * Layouts that are excluded from being shown as space types in the widget section.
     * This includes system layouts, date layouts, object type layout, and participant layout.
     */
    val excludedSpaceTypeLayouts = systemLayouts + dateLayouts + listOf(
        ObjectType.Layout.OBJECT_TYPE,
        ObjectType.Layout.PARTICIPANT
    )

    fun isSupportedForWidgets(layout: ObjectType.Layout?) : Boolean {
        return widgetsLayouts.contains(layout)
    }

    fun isFileLayout(layout: ObjectType.Layout?) : Boolean {
        return fileLayouts.contains(layout)
    }
}
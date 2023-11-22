package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.ObjectType

object SupportedLayouts {
    val layouts = listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.FILE,
        ObjectType.Layout.IMAGE,
        ObjectType.Layout.PROFILE,
        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION,
        ObjectType.Layout.TODO,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.BOOKMARK
    )
    val editorLayouts = listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.PROFILE,
        ObjectType.Layout.TODO,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.BOOKMARK
    )
    val fileLayouts = listOf(
        ObjectType.Layout.FILE,
        ObjectType.Layout.IMAGE,
        ObjectType.Layout.VIDEO,
        ObjectType.Layout.AUDIO
    )

    val systemLayouts = listOf(
        ObjectType.Layout.OBJECT_TYPE,
        ObjectType.Layout.RELATION,
        ObjectType.Layout.RELATION_OPTION,
        ObjectType.Layout.DASHBOARD,
        ObjectType.Layout.SPACE,
        ObjectType.Layout.SPACE_VIEW,
    )

    val createObjectLayouts = listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.PROFILE,
        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION,
        ObjectType.Layout.TODO,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.BOOKMARK
    )

    fun isSupported(layout: ObjectType.Layout?) : Boolean {
        return layouts.contains(layout)
    }
}
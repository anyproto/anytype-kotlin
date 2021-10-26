package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.ObjectType

object SupportedLayouts {

    val layouts = listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.FILE,
        ObjectType.Layout.IMAGE,
        ObjectType.Layout.PROFILE,
        ObjectType.Layout.SET,
        ObjectType.Layout.TODO,
        ObjectType.Layout.NOTE
    )
}
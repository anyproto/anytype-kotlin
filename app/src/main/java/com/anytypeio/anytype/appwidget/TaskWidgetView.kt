package com.anytypeio.anytype.appwidget

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper

data class TaskWidgetView(
    val id: Id,
    val name: String?,
    val spaceId: Id?,
    val isDone: Boolean
)

fun ObjectWrapper.Basic.toTaskWidgetView(): TaskWidgetView {
    return TaskWidgetView(
        id = id,
        name = name,
        isDone = done == true,
        spaceId = spaceId
    )
}
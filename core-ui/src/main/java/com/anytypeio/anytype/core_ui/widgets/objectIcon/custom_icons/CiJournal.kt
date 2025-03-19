package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiJournal: ImageVector
    get() {
        if (_CiJournal != null) {
            return _CiJournal!!
        }
        _CiJournal = ImageVector.Builder(
            name = "CiJournal",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(290f, 32f)
                horizontalLineTo(144f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80f, 96f)
                verticalLineTo(416f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, 64f)
                horizontalLineTo(290f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(368f, 32f)
                horizontalLineTo(350f)
                verticalLineTo(480f)
                horizontalLineToRelative(18f)
                arcToRelative(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 64f, -64f)
                verticalLineTo(96f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 368f, 32f)
                close()
            }
        }.build()

        return _CiJournal!!
    }

@Suppress("ObjectPropertyName")
private var _CiJournal: ImageVector? = null

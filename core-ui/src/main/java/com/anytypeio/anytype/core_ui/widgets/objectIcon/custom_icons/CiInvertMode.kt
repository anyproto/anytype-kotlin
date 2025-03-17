package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiInvertMode: ImageVector
    get() {
        if (_CiInvertMode != null) {
            return _CiInvertMode!!
        }
        _CiInvertMode = ImageVector.Builder(
            name = "CiInvertMode",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 32f
            ) {
                moveTo(256f, 256f)
                moveToRelative(-208f, 0f)
                arcToRelative(208f, 208f, 0f, isMoreThanHalf = true, isPositiveArc = true, 416f, 0f)
                arcToRelative(208f, 208f, 0f, isMoreThanHalf = true, isPositiveArc = true, -416f, 0f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 176f)
                verticalLineTo(336f)
                arcToRelative(80f, 80f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -160f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 48f)
                verticalLineTo(176f)
                arcToRelative(80f, 80f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 160f)
                verticalLineTo(464f)
                curveTo(141.12f, 464f, 48f, 370.88f, 48f, 256f)
                reflectiveCurveTo(141.12f, 48f, 256f, 48f)
                close()
            }
        }.build()

        return _CiInvertMode!!
    }

@Suppress("ObjectPropertyName")
private var _CiInvertMode: ImageVector? = null

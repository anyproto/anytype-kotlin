package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiArrowUndo: ImageVector
    get() {
        if (_CiArrowUndo != null) {
            return _CiArrowUndo!!
        }
        _CiArrowUndo = ImageVector.Builder(
            name = "CiArrowUndo",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(448f, 440f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -12.61f, -6.15f)
                curveToRelative(-22.86f, -29.27f, -44.07f, -51.86f, -73.32f, -67f)
                curveTo(335f, 352.88f, 301f, 345.59f, 256f, 344.23f)
                verticalLineTo(424f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 229f, 435.57f)
                lineToRelative(-176f, -168f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -23.14f)
                lineToRelative(176f, -168f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 88f)
                verticalLineToRelative(80.36f)
                curveToRelative(74.14f, 3.41f, 129.38f, 30.91f, 164.35f, 81.87f)
                curveTo(449.32f, 292.44f, 464f, 350.9f, 464f, 424f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16f, 16f)
                close()
            }
        }.build()

        return _CiArrowUndo!!
    }

@Suppress("ObjectPropertyName")
private var _CiArrowUndo: ImageVector? = null

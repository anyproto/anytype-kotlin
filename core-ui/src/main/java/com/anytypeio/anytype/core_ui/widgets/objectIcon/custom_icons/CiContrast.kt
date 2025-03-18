package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiContrast: ImageVector
    get() {
        if (_CiContrast != null) {
            return _CiContrast!!
        }
        _CiContrast = ImageVector.Builder(
            name = "CiContrast",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 32f)
                arcTo(224f, 224f, 0f, isMoreThanHalf = false, isPositiveArc = false, 97.61f, 414.39f)
                arcTo(224f, 224f, 0f, isMoreThanHalf = true, isPositiveArc = false, 414.39f, 97.61f)
                arcTo(222.53f, 222.53f, 0f, isMoreThanHalf = false, isPositiveArc = false, 256f, 32f)
                close()
                moveTo(64f, 256f)
                curveTo(64f, 150.13f, 150.13f, 64f, 256f, 64f)
                verticalLineTo(448f)
                curveTo(150.13f, 448f, 64f, 361.87f, 64f, 256f)
                close()
            }
        }.build()

        return _CiContrast!!
    }

@Suppress("ObjectPropertyName")
private var _CiContrast: ImageVector? = null

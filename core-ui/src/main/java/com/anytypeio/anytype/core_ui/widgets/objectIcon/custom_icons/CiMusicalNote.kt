package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiMusicalNote: ImageVector
    get() {
        if (_CiMusicalNote != null) {
            return _CiMusicalNote!!
        }
        _CiMusicalNote = ImageVector.Builder(
            name = "CiMusicalNote",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(183.83f, 480f)
                arcToRelative(55.2f, 55.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32.36f, -10.55f)
                arcTo(56.64f, 56.64f, 0f, isMoreThanHalf = false, isPositiveArc = true, 128f, 423.58f)
                arcToRelative(50.26f, 50.26f, 0f, isMoreThanHalf = false, isPositiveArc = true, 34.14f, -47.73f)
                lineTo(213f, 358.73f)
                arcToRelative(16.25f, 16.25f, 0f, isMoreThanHalf = false, isPositiveArc = false, 11f, -15.49f)
                verticalLineTo(92f)
                arcToRelative(32.1f, 32.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24.09f, -31.15f)
                lineTo(356.48f, 32.71f)
                arcTo(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = true, 384f, 54f)
                verticalLineToRelative(57.75f)
                arcToRelative(32.09f, 32.09f, 0f, isMoreThanHalf = false, isPositiveArc = true, -24.2f, 31.19f)
                lineToRelative(-91.65f, 23.13f)
                arcTo(16.24f, 16.24f, 0f, isMoreThanHalf = false, isPositiveArc = false, 256f, 181.91f)
                verticalLineTo(424f)
                arcToRelative(48.22f, 48.22f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32.78f, 45.81f)
                lineToRelative(-21.47f, 7.23f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = true, 183.83f, 480f)
                close()
            }
        }.build()

        return _CiMusicalNote!!
    }

@Suppress("ObjectPropertyName")
private var _CiMusicalNote: ImageVector? = null

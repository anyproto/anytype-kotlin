package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiGitCommit: ImageVector
    get() {
        if (_CiGitCommit != null) {
            return _CiGitCommit!!
        }
        _CiGitCommit = ImageVector.Builder(
            name = "CiGitCommit",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(448f, 224f)
                horizontalLineTo(380f)
                arcToRelative(128f, 128f, 0f, isMoreThanHalf = false, isPositiveArc = false, -247.9f, 0f)
                horizontalLineTo(64f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 64f)
                horizontalLineToRelative(68.05f)
                arcTo(128f, 128f, 0f, isMoreThanHalf = false, isPositiveArc = false, 380f, 288f)
                horizontalLineTo(448f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -64f)
                close()
                moveTo(256f, 320f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = true, 64f, -64f)
                arcTo(64.07f, 64.07f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 320f)
                close()
            }
        }.build()

        return _CiGitCommit!!
    }

@Suppress("ObjectPropertyName")
private var _CiGitCommit: ImageVector? = null

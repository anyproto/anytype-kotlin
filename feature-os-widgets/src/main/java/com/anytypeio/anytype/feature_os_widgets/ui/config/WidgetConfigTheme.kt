package com.anytypeio.anytype.feature_os_widgets.ui.config

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun WidgetConfigTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = Color(0xFF1F1E1D),
            surface = Color(0xFF2B2A29),
            onBackground = Color.White,
            onSurface = Color.White,
            primary = Color(0xFFFFC940)
        ),
        content = content
    )
}

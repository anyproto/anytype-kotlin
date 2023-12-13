package com.anytypeio.anytype.core_ui.foundation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R

@Preview
@Composable
fun EmptyStatePreview() {
    EmptyState(
        title = "Nothing found",
        description = "Object type not found. Please try changing your request",
        icon = AlertConfig.Icon(
            GRADIENT_TYPE_RED,
            icon = R.drawable.ic_alert_error
        )
    )
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: AlertConfig.Icon
) {
    Column(modifier = modifier) {
        AlertIcon(icon)
        Spacer(modifier = Modifier.height(16.dp))
        AlertTitle(title)
        Spacer(modifier = Modifier.height(8.dp))
        AlertDescription(description)
        Spacer(modifier = Modifier.height(20.dp))
    }
}
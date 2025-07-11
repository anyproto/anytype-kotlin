package com.anytypeio.anytype.core_ui.foundation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews

@DefaultPreviews
@Composable
fun EmptyStatePreview() {
    EmptyState(
        title = "Nothing found",
        description = "Object type not found. Please try changing your request",
        icon = R.drawable.ic_popup_duck_56
    )
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: Int
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
package com.anytypeio.anytype.feature_date.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.feature_date.R

@Composable
fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = stringResource(id = R.string.all_content_error_title),
            color = colorResource(id = R.color.text_primary),
            style = UXBody,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = message,
            color = colorResource(id = R.color.palette_system_red),
            style = UXBody,
            textAlign = TextAlign.Center,
            maxLines = 3
        )
    }
}

@Composable
fun EmptyState() {
    val title = stringResource(R.string.date_layout_empty_items)
    Box(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.ime)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = title,
            color = colorResource(id = R.color.text_primary),
            style = UXBody,
            textAlign = TextAlign.Center
        )
    }
}
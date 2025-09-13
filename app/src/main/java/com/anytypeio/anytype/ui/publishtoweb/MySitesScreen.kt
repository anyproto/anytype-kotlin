package com.anytypeio.anytype.ui.publishtoweb

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.presentation.publishtoweb.MySitesViewState

@Composable
fun MySitesScreen(
    viewState: MySitesViewState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "My Sites - Coming Soon",
            style = HeadlineSubheading,
            color = colorResource(R.color.text_primary),
            textAlign = TextAlign.Center
        )
    }
}

@DefaultPreviews
@Composable
fun MySitesScreenPreview() {
    MaterialTheme {
        MySitesScreen(
            viewState = MySitesViewState.Init
        )
    }
}
package com.anytypeio.anytype.gallery_experience.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationState

@Composable
fun GalleryInstallationScreen(state: GalleryInstallationState) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .height(300.dp)
            .background(color = Color.Red)
    ) {
        Text(text = "GalleryInstallationScreen")
    }
}


@Preview
@Composable
private fun GalleryInstallationScreenPreview() {
    GalleryInstallationScreen(GalleryInstallationState.Success)
}
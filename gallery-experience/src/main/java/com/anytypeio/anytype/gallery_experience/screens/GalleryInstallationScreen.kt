package com.anytypeio.anytype.gallery_experience.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationState

@Composable
fun GalleryInstallationScreen(state: GalleryInstallationState) {
}


@Preview
@Composable
private fun GalleryInstallationScreenPreview() {
    GalleryInstallationScreen(GalleryInstallationState.Success)
}
package com.anytypeio.anytype.gallery_experience.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.anytypeio.anytype.gallery_experience.models.GalleryInstallationSpacesState
import com.anytypeio.anytype.gallery_experience.models.SpaceView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryInstallationSpacesScreen(
    state: GalleryInstallationSpacesState,
    onNewSpaceClick: () -> Unit,
    onSpaceClick: (SpaceView) -> Unit,
    onDismiss: () -> Unit
) {

}
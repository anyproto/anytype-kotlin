package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.views.animations.LoadingIndicator
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageIconView(
    modifier: Modifier = Modifier,
    icon: ObjectIcon.Basic.Image,
    backgroundSize: Dp
) {

    val painter = rememberAsyncImagePainter(icon.hash)
    val state by painter.state.collectAsState()

    when (state) {
        AsyncImagePainter.State.Empty,
        is AsyncImagePainter.State.Loading -> {
            LoadingIndicator(
                containerSize = backgroundSize
            )
        }

        is AsyncImagePainter.State.Error -> {
            TypeIconView(
                modifier = modifier,
                icon = icon.fallback,
                backgroundSize = backgroundSize
            )
        }

        is AsyncImagePainter.State.Success -> {
            Image(
                painter = painter,
                contentDescription = "Icon from URI",
                modifier = modifier
                    .size(backgroundSize)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
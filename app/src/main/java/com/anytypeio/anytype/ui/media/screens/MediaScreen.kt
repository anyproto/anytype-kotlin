package com.anytypeio.anytype.ui.media.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.anytypeio.anytype.ui.media.MediaActivity

@Composable
fun MediaScreen(
    url: String,
    mediaType: Int,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (mediaType) {
            MediaActivity.TYPE_IMAGE -> ImageViewer(url = url)
            MediaActivity.TYPE_VIDEO -> VideoPlayer(url = url)
            else -> UnknownMediaType()
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ImageViewer(url: String) {
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun VideoPlayer(url: String) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(Uri.parse(url))
                setMediaController(MediaController(ctx).also { it.setAnchorView(this) })
                setOnPreparedListener { start() }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun UnknownMediaType() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Unsupported media type",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
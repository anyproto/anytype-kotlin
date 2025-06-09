package com.anytypeio.anytype.ui.media.screens

import android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END
import android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.ui.media.MediaActivity
import kotlinx.coroutines.delay

@Composable
fun MediaScreen(
    url: String,
    mediaType: Int,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (mediaType) {
            MediaActivity.TYPE_IMAGE -> ImageViewer(url = url)
            MediaActivity.TYPE_VIDEO -> VideoPlayer2(url = url)
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
private fun VideoPlayer2(url: String) {
    val context = LocalContext.current
    val videoViewRef = remember { mutableStateOf<VideoView?>(null) }

    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var videoDuration by remember { mutableStateOf(0) }
    var currentPosition by remember { mutableStateOf(0) }
    var userSeeking by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    // Poll playback progress
    LaunchedEffect(isPlaying, userSeeking) {
        while (isPlaying && !userSeeking) {
            videoViewRef.value?.let {
                currentPosition = it.currentPosition
            }
            delay(500)
        }
    }

    // Auto-hide controls after n seconds
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(1500)
            showControls = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            videoViewRef.value?.apply {
                stopPlayback()
                videoViewRef.value = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { showControls = !showControls }
            }
    ) {
        // VideoView rendering
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(Uri.parse(url))
                    setOnPreparedListener {
                        videoDuration = it.duration
                        isBuffering = false
                        start()
                        isPlaying = true
                    }
                    setOnInfoListener { _, what, _ ->
                        when (what) {
                            MEDIA_INFO_BUFFERING_START -> isBuffering = true
                            MEDIA_INFO_BUFFERING_END -> isBuffering = false
                        }
                        false
                    }
                    setOnCompletionListener {
                        isPlaying = false
                        showControls = true
                        currentPosition = videoDuration
                    }
                    isFocusable = false
                    isFocusableInTouchMode = false
                    videoViewRef.value = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator
        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                color = Color.White,
                strokeWidth = 4.dp
            )
        }

        // Overlay controls with fade-in/out
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // SeekBar + time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMillis(currentPosition),
                        color = Color.White,
                        style = BodyCallout,
                        modifier = Modifier.alpha(0.5f)
                    )
                    Slider(
                        value = currentPosition.toFloat(),
                        onValueChange = {
                            userSeeking = true
                            currentPosition = it.toInt()
                        },
                        onValueChangeFinished = {
                            videoViewRef.value?.seekTo(currentPosition)
                            userSeeking = false
                        },
                        valueRange = 0f..(videoDuration.coerceAtLeast(1).toFloat()),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .alpha(0.5f)
                        ,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    Text(
                        text = formatMillis(videoDuration),
                        color = Color.White,
                        style = BodyCallout,
                        modifier = Modifier.alpha(0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!isPlaying) {
                    Image(
                        painter = painterResource(R.drawable.ic_chat_attachment_play),
                        contentDescription = "Play",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                val player = videoViewRef.value ?: return@clickable
                                if (player.isPlaying) {
                                    player.pause()
                                    isPlaying = false
                                    showControls = true
                                } else {
                                    player.start()
                                    isPlaying = true
                                }
                            }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .background(color = Color.White)
                            .size(18.dp)
                            .clickable {
                                val player = videoViewRef.value ?: return@clickable
                                if (player.isPlaying) {
                                    player.pause()
                                    isPlaying = false
                                    showControls = true
                                } else {
                                    player.start()
                                    isPlaying = true
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Format milliseconds to mm:ss
private fun formatMillis(millis: Int): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
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
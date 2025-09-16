package com.anytypeio.anytype.ui.media.screens

import android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END
import android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START
import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import kotlinx.coroutines.delay
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import timber.log.Timber

@Composable
fun ImageGallery(
    urls: List<String>,
    index: Int = 0,
    onBackClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onOpenClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = index) { urls.size }
    var chromeVisible by remember { mutableStateOf(true) }

    LaunchedEffect(pagerState.settledPage) {
        chromeVisible = true
    }

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            val url = urls[page]

            ImageViewer(
                url = url,
                onClick = {
                    chromeVisible = !chromeVisible
                    Timber.d("onClick, chrome visible: $chromeVisible")
                },
            )
        }

        // Page counter chip (top-center)

        if (urls.size > 1) {
            AnimatedVisibility(
                visible = chromeVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .systemBarsPadding()
                        .padding(top = 48.dp)
                        .background(
                            color = colorResource(R.color.home_screen_toolbar_button),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${pagerState.settledPage + 1}/${urls.size}",
                        style = Caption1Medium,
                        color = colorResource(R.color.glyph_active)
                    )
                }
            }
        }

        // Toolbar
        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn(),
            exit  = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            MediaActionToolbar(
                modifier = Modifier.padding(bottom = 32.dp),
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun ImageViewer(
    url: String,
    onClick: () -> Unit = {}
) {

    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ZoomableAsyncImage(
            model = ImageRequest.Builder(context)
                .data(url)
                .crossfade(1_000)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .listener(
                    onStart = {
                        isLoading = true
                        hasError = false
                    },
                    onSuccess = { _, _ ->
                        isLoading = false
                    },
                    onError = { _, _ ->
                        isLoading = false
                        hasError = true
                    }
                )
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize(),
            onClick = {
                onClick()
            }
        )

        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .size(48.dp),
                    color = colorResource(R.color.glyph_active),
                    trackColor = colorResource(R.color.glyph_active).copy(alpha = 0.5f),
                    strokeWidth = 4.dp
                )
            }
            hasError -> {
                Text(
                    text = stringResource(R.string.failed_to_load_image),
                    color = colorResource(R.color.palette_system_red),
                    modifier = Modifier.align(Alignment.Center),
                    style = Caption1Medium
                )
            }
        }
    }
}

@Composable
fun AudioPlayerBox(
    name: String,
    url: String
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AudioPlayer(
            url = url,
            name = name
        )
    }
}

@Composable
fun VideoPlayerBox(
    url: String
) {
    Box(modifier = Modifier.fillMaxSize()) {
        VideoPlayer(
            url = url
        )
    }
}

@Composable
fun ImageGalleryBox(
    urls: List<String> =  emptyList(),
    index: Int = 0,
    onBackClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onOpenClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ImageGallery(
            urls = urls,
            index = index,
            onBackClick = onBackClick,
            onDownloadClick = onDownloadClick,
            onDeleteClick = onDeleteClick,
            onOpenClick = onOpenClick
        )
    }
}

@Composable
private fun VideoPlayer(url: String) {
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
            delay(DELAY_BEFORE_HIDING_CONTROLS)
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // SeekBar + time
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                    ,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMillis(currentPosition),
                        color = Color.White,
                        style = BodyCallout,
                        modifier = Modifier.alpha(0.5f)
                    )
                    DotScrubberSlider(
                        value = currentPosition.toFloat(),
                        onValueChange = {
                            userSeeking = true
                            currentPosition = it.toInt()
                            videoViewRef.value?.seekTo(currentPosition)
                            userSeeking = false
                        },
                        valueRange = 0f..videoDuration.coerceAtLeast(1).toFloat(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = formatMillis(videoDuration),
                        color = Color.White,
                        style = BodyCallout,
                        modifier = Modifier.alpha(0.5f)
                    )
                }

                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = colorResource(R.color.transparent_active),
                                shape = CircleShape
                            )
                            .align(
                                Alignment.Center
                            )
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
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_player_play),
                            contentDescription = "Play button",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(12.dp)
                                .size(24.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(
                                color = colorResource(R.color.transparent_active),
                                shape = CircleShape
                            )
                            .align(
                                Alignment.Center
                            )
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
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_player_pause),
                            contentDescription = "Pause button",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(12.dp)
                                .size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioPlayer(
    url: String,
    name: String
) {
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
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                    text = name,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    style = Caption1Medium
                )

                // SeekBar + time
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                    ,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMillis(currentPosition),
                        color = Color.White,
                        style = BodyCallout,
                        modifier = Modifier.alpha(0.5f)
                    )
                    DotScrubberSlider(
                        value = currentPosition.toFloat(),
                        onValueChange = {
                            userSeeking = true
                            currentPosition = it.toInt()
                            videoViewRef.value?.seekTo(currentPosition)
                            userSeeking = false
                        },
                        valueRange = 0f..videoDuration.coerceAtLeast(1).toFloat(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = formatMillis(videoDuration),
                        color = Color.White,
                        style = BodyCallout,
                        modifier = Modifier.alpha(0.5f)
                    )
                }

                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = colorResource(R.color.transparent_active),
                                shape = CircleShape
                            )
                            .align(
                                Alignment.Center
                            )
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
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_player_play),
                            contentDescription = "Play button",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(12.dp)
                                .size(24.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(
                                color = colorResource(R.color.transparent_active),
                                shape = CircleShape
                            )
                            .align(
                                Alignment.Center
                            )
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
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_player_pause),
                            contentDescription = "Pause button",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(12.dp)
                                .size(24.dp)
                        )
                    }
                }
            }
        }
    }
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

@Composable
fun DotScrubberSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    trackHeight: Dp = 4.dp,
    dotRadius: Dp = 6.dp
) {
    val density = LocalDensity.current
    val trackHeightPx = with(density) { trackHeight.toPx() }
    val dotRadiusPx = with(density) { dotRadius.toPx() }

    var sliderWidth by remember { mutableStateOf(1f) }

    BoxWithConstraints(
        modifier = modifier
            .height(dotRadius * 2)
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val ratio = offset.x / size.width
                    val newValue =
                        (valueRange.start + ratio * (valueRange.endInclusive - valueRange.start))
                            .coerceIn(valueRange)
                    onValueChange(newValue)
                }
            }
    ) {
        sliderWidth = constraints.maxWidth.toFloat()

        val valueRatio = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
        val thumbCenterX = sliderWidth * valueRatio

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Inactive track
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(0f, size.height / 2 - trackHeightPx / 2),
                size = Size(size.width, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2, trackHeightPx / 2)
            )

            // Active track
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(0f, size.height / 2 - trackHeightPx / 2),
                size = Size(thumbCenterX, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2, trackHeightPx / 2)
            )

            // Thumb dot
            drawCircle(
                color = Color.White,
                radius = dotRadiusPx,
                center = Offset(thumbCenterX, size.height / 2)
            )
        }

        // Drag support
        Modifier
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val x = change.position.x.coerceIn(0f, sliderWidth)
                    val newRatio = x / sliderWidth
                    val newValue = valueRange.start + newRatio * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue.coerceIn(valueRange))
                }
            }
    }
}

@DefaultPreviews
@Composable
private fun MediaScreenVideoPreview() {
    VideoPlayerBox(
        url = "https://www.youtube.com/watch?v=I-oAtRUEcPM"
    )
}

@DefaultPreviews
@Composable
private fun MediaScreenAudioPreview() {
    AudioPlayerBox(
        name = "Clara Luciani - Nue",
        url = "https://www.youtube.com/watch?v=I-oAtRUEcPM"
    )
}

// Format milliseconds to mm:ss
private fun formatMillis(millis: Int): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

const val DELAY_BEFORE_HIDING_CONTROLS = 1000L